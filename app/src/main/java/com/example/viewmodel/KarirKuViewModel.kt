package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ApplicationStatus
import com.example.data.AvailabilityStatus
import com.example.data.CareerDocument
import com.example.data.ContractType
import com.example.data.DocumentCategory
import com.example.data.InterviewSchedule
import com.example.data.JobApplication
import com.example.data.KarirKuRepository
import com.example.data.MeetingFormat
import com.example.data.UserProfile
import com.example.data.WorkMode
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class SortOrder(val label: String) {
    NEWEST("Terbaru"),
    HIGHEST_SALARY("Gaji Tertinggi"),
    STARRED_FIRST("Prioritas")
}

data class PipelineMetrics(
    val totalCount: Int = 0,
    val savedCount: Int = 0,
    val appliedCount: Int = 0,
    val screeningCount: Int = 0,
    val interviewCount: Int = 0,
    val offerCount: Int = 0,
    val rejectedCount: Int = 0,
    val positiveResponseRate: Int = 0,
    val offerRatePercent: Float = 0f,
    val winRatePercent: Int = 0,
    val averageSalaryMidpoint: Long = 0L,
    val upcomingInterviewsThisWeek: Int = 0,
    val interviewCompaniesPreview: String = "",
    val offerCompaniesPreview: String = ""
)

data class KarirKuDashboardState(
    val profile: UserProfile? = null,
    val applications: List<JobApplication> = emptyList(),
    val schedules: List<InterviewSchedule> = emptyList(),
    val documents: List<CareerDocument> = emptyList(),
    val metrics: PipelineMetrics = PipelineMetrics(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class KarirKuViewModel(
    private val repository: KarirKuRepository,
    private val currentUserId: String,
    private val initialDisplayName: String,
    private val initialPhotoUrl: String?
) : ViewModel() {

    private val _selectedStatusFilter = MutableStateFlow<ApplicationStatus?>(null)
    val selectedStatusFilter: StateFlow<ApplicationStatus?> = _selectedStatusFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureUserProfile(
                userId = currentUserId,
                defaultName = initialDisplayName,
                photoUrl = initialPhotoUrl
            ).onFailure { err ->
                Log.w(TAG, "Profile bootstrap failed: ${err.message}")
            }
        }
    }

    private fun <T> safeUiFlow(
        source: kotlinx.coroutines.flow.Flow<T>,
        fallbackMessage: String
    ): StateFlow<UiState<T>> = source
        .map<T, UiState<T>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, fallbackMessage, rootError)
            emit(UiState.Error(rootError.localizedMessage ?: fallbackMessage))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val profileFlow: StateFlow<UiState<UserProfile?>> =
        safeUiFlow(repository.observeUserProfile(currentUserId), "Gagal memuat profil dossier")

    private val applicationsFlow: StateFlow<UiState<List<JobApplication>>> =
        safeUiFlow(repository.observeApplications(currentUserId), "Gagal memuat daftar lamaran")

    private val schedulesFlow: StateFlow<UiState<List<InterviewSchedule>>> =
        safeUiFlow(repository.observeSchedules(currentUserId), "Gagal memuat jadwal interview")

    private val documentsFlow: StateFlow<UiState<List<CareerDocument>>> =
        safeUiFlow(repository.observeDocuments(currentUserId), "Gagal memuat dokumen dossier")

    val dashboardState: StateFlow<KarirKuDashboardState> = combine(
        profileFlow,
        applicationsFlow,
        schedulesFlow,
        documentsFlow
    ) { profState, appsState, schedState, docsState ->
        val errors = listOfNotNull(
            (profState as? UiState.Error)?.message,
            (appsState as? UiState.Error)?.message,
            (schedState as? UiState.Error)?.message,
            (docsState as? UiState.Error)?.message
        )
        val loading = profState is UiState.Loading && appsState is UiState.Loading
        val apps = (appsState as? UiState.Success)?.data ?: emptyList()
        val scheds = (schedState as? UiState.Success)?.data ?: emptyList()
        val docs = (docsState as? UiState.Success)?.data ?: emptyList()
        val prof = (profState as? UiState.Success)?.data

        KarirKuDashboardState(
            profile = prof,
            applications = apps,
            schedules = scheds,
            documents = docs,
            metrics = computeMetrics(apps, scheds),
            isLoading = loading,
            errorMessage = errors.firstOrNull()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = KarirKuDashboardState()
    )

    fun setStatusFilter(status: ApplicationStatus?) {
        _selectedStatusFilter.value = status
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun cycleSortOrder() {
        val values = SortOrder.entries
        val nextIndex = (values.indexOf(_sortOrder.value) + 1) % values.size
        _sortOrder.value = values[nextIndex]
        _snackbarEvents.tryEmit("Urutan: ${values[nextIndex].label}")
    }

    // --- MUTATIONS ---

    fun saveApplication(
        existingId: String?,
        existingDossierCode: String?,
        roleTitle: String,
        companyName: String,
        location: String,
        status: ApplicationStatus,
        workMode: WorkMode,
        contractType: ContractType,
        salaryMin: Long,
        salaryMax: Long,
        appliedDateMillis: Long,
        targetResponseMillis: Long,
        notes: String,
        hrContact: String,
        attachments: List<String>,
        starred: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.saveApplication(
                userId = currentUserId,
                existingId = existingId,
                existingDossierCode = existingDossierCode,
                roleTitle = roleTitle,
                companyName = companyName,
                location = location,
                status = status,
                workMode = workMode,
                contractType = contractType,
                salaryMin = salaryMin,
                salaryMax = salaryMax,
                appliedDateMillis = appliedDateMillis,
                targetResponseMillis = targetResponseMillis,
                notes = notes,
                hrContact = hrContact,
                attachments = attachments,
                starred = starred
            )
            _isSaving.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit(
                    if (existingId.isNullOrBlank()) "Lamaran berhasil disimpan ke Dossier"
                    else "Perubahan lamaran tersimpan"
                )
                onSuccess()
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "Gagal menyimpan lamaran")
            }
        }
    }

    fun toggleApplicationStar(app: JobApplication) {
        viewModelScope.launch {
            repository.toggleApplicationStar(currentUserId, app.id, !app.starred)
                .onFailure { err ->
                    _snackbarEvents.tryEmit(err.localizedMessage ?: "Gagal memperbarui prioritas")
                }
        }
    }

    fun updateApplicationStatus(applicationId: String, status: ApplicationStatus) {
        viewModelScope.launch {
            repository.updateApplicationStatus(currentUserId, applicationId, status)
                .onSuccess {
                    _snackbarEvents.tryEmit("Status diperbarui ke ${status.label}")
                }
                .onFailure { err ->
                    _snackbarEvents.tryEmit(err.localizedMessage ?: "Gagal memperbarui status")
                }
        }
    }

    fun deleteApplication(applicationId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteApplication(currentUserId, applicationId)
                .onSuccess {
                    _snackbarEvents.tryEmit("Lamaran dihapus dari dossier")
                    onSuccess()
                }
                .onFailure { err ->
                    _snackbarEvents.tryEmit(err.localizedMessage ?: "Gagal menghapus lamaran")
                }
        }
    }

    fun saveSchedule(
        existingId: String?,
        applicationId: String,
        companyName: String,
        roleTitle: String,
        stageTitle: String,
        scheduledAtMillis: Long,
        durationMinutes: Long,
        meetingFormat: MeetingFormat,
        meetingLocation: String,
        interviewerName: String,
        prepNotes: String,
        completed: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.saveSchedule(
                userId = currentUserId,
                existingId = existingId,
                applicationId = applicationId,
                companyName = companyName,
                roleTitle = roleTitle,
                stageTitle = stageTitle,
                scheduledAtMillis = scheduledAtMillis,
                durationMinutes = durationMinutes,
                meetingFormat = meetingFormat,
                meetingLocation = meetingLocation,
                interviewerName = interviewerName,
                prepNotes = prepNotes,
                completed = completed
            )
            _isSaving.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit(
                    if (existingId.isNullOrBlank()) "Jadwal wawancara ditambahkan"
                    else "Jadwal wawancara diperbarui"
                )
                onSuccess()
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "Gagal menyimpan jadwal")
            }
        }
    }

    fun toggleScheduleCompleted(schedule: InterviewSchedule) {
        viewModelScope.launch {
            repository.toggleScheduleCompleted(currentUserId, schedule.id, !schedule.completed)
                .onSuccess {
                    _snackbarEvents.tryEmit(
                        if (!schedule.completed) "Agenda ditandai selesai" else "Agenda dibuka kembali"
                    )
                }
                .onFailure { err ->
                    _snackbarEvents.tryEmit(err.localizedMessage ?: "Gagal memperbarui agenda")
                }
        }
    }

    fun deleteSchedule(scheduleId: String) {
        viewModelScope.launch {
            repository.deleteSchedule(currentUserId, scheduleId)
                .onSuccess { _snackbarEvents.tryEmit("Jadwal dihapus") }
                .onFailure { err ->
                    _snackbarEvents.tryEmit(err.localizedMessage ?: "Gagal menghapus jadwal")
                }
        }
    }

    fun saveDocument(
        existingId: String?,
        title: String,
        category: DocumentCategory,
        subtitle: String,
        externalUrl: String,
        verified: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.saveDocument(
                userId = currentUserId,
                existingId = existingId,
                title = title,
                category = category,
                subtitle = subtitle,
                externalUrl = externalUrl,
                verified = verified
            )
            _isSaving.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit("Dokumen dossier tersimpan")
                onSuccess()
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "Gagal menyimpan dokumen")
            }
        }
    }

    fun deleteDocument(documentId: String) {
        viewModelScope.launch {
            repository.deleteDocument(currentUserId, documentId)
                .onSuccess { _snackbarEvents.tryEmit("Dokumen dihapus") }
                .onFailure { err ->
                    _snackbarEvents.tryEmit(err.localizedMessage ?: "Gagal menghapus dokumen")
                }
        }
    }

    fun updateProfileIdentity(
        displayName: String,
        headline: String,
        location: String,
        availabilityStatus: AvailabilityStatus,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.updateProfileIdentity(
                userId = currentUserId,
                displayName = displayName,
                headline = headline,
                location = location,
                availabilityStatus = availabilityStatus
            )
            _isSaving.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit("Profil dossier diperbarui")
                onSuccess()
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "Gagal memperbarui profil")
            }
        }
    }

    fun updateCareerPreferences(
        targetRoles: List<String>,
        expectedSalaryMin: Long,
        expectedSalaryMax: Long,
        preferredWorkTypes: List<String>,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.updateCareerPreferences(
                userId = currentUserId,
                targetRoles = targetRoles,
                expectedSalaryMin = expectedSalaryMin,
                expectedSalaryMax = expectedSalaryMax,
                preferredWorkTypes = preferredWorkTypes
            )
            _isSaving.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit("Preferensi karier diperbarui")
                onSuccess()
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "Gagal menyimpan preferensi")
            }
        }
    }

    fun updateSettings(followUpRemindersEnabled: Boolean, darkModeEnabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings(
                userId = currentUserId,
                followUpRemindersEnabled = followUpRemindersEnabled,
                darkModeEnabled = darkModeEnabled
            ).onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "Gagal mengubah pengaturan")
            }
        }
    }

    private fun computeMetrics(
        apps: List<JobApplication>,
        schedules: List<InterviewSchedule>
    ): PipelineMetrics {
        val total = apps.size
        val saved = apps.count { it.statusEnum == ApplicationStatus.DISIMPAN }
        val applied = apps.count { it.statusEnum == ApplicationStatus.DILAMAR }
        val screening = apps.count { it.statusEnum == ApplicationStatus.ADMINISTRASI }
        val interview = apps.count { it.statusEnum == ApplicationStatus.INTERVIEW }
        val offer = apps.count { it.statusEnum == ApplicationStatus.OFFER }
        val rejected = apps.count { it.statusEnum == ApplicationStatus.DITOLAK }

        val submittedTotal = (total - saved).coerceAtLeast(0)
        val positiveStages = screening + interview + offer
        val positiveRate = if (submittedTotal > 0) ((positiveStages * 100) / submittedTotal) else 0
        val offerRate = if (total > 0) (offer * 100f / total) else 0f
        val completedDecisions = offer + rejected
        val winRate = when {
            completedDecisions > 0 -> (offer * 100) / completedDecisions
            offer > 0 -> 100
            else -> 0
        }

        val salarySamples = apps.mapNotNull { app ->
            when {
                app.salaryMin > 0L && app.salaryMax > 0L -> (app.salaryMin + app.salaryMax) / 2L
                app.salaryMax > 0L -> app.salaryMax
                app.salaryMin > 0L -> app.salaryMin
                else -> null
            }
        }
        val avgSalary = if (salarySamples.isNotEmpty()) salarySamples.sum() / salarySamples.size else 0L

        val now = System.currentTimeMillis()
        val sevenDaysLater = now + 7L * 24L * 60L * 60L * 1000L
        val upcomingThisWeek = schedules.count { !it.completed && it.scheduledAtMillis in now..sevenDaysLater }

        val interviewCompanies = apps
            .filter { it.statusEnum == ApplicationStatus.INTERVIEW }
            .map { it.companyName }
            .distinct()
            .take(2)
            .joinToString(" • ")

        val offerCompanies = apps
            .filter { it.statusEnum == ApplicationStatus.OFFER }
            .map { it.companyName }
            .distinct()
            .let { list ->
                when {
                    list.isEmpty() -> "Belum ada offer"
                    list.size == 1 -> list.first()
                    else -> "${list.first()} + ${list.size - 1} lainnya"
                }
            }

        return PipelineMetrics(
            totalCount = total,
            savedCount = saved,
            appliedCount = applied,
            screeningCount = screening,
            interviewCount = interview,
            offerCount = offer,
            rejectedCount = rejected,
            positiveResponseRate = positiveRate,
            offerRatePercent = offerRate,
            winRatePercent = winRate,
            averageSalaryMidpoint = avgSalary,
            upcomingInterviewsThisWeek = upcomingThisWeek,
            interviewCompaniesPreview = interviewCompanies.ifBlank { "Jadwalkan sesi baru" },
            offerCompaniesPreview = offerCompanies
        )
    }

    private companion object {
        const val TAG = "KarirKuVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
