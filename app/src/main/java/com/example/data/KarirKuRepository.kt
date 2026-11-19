package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class KarirKuRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireVerifiedUid(expectedUserId: String): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus masuk dengan Google terlebih dahulu.")
        check(uid == expectedUserId) { "Sesi akun berubah; menolak operasi lintas pengguna." }
        return uid
    }

    // --- REALTIME OBSERVATION FLOWS ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    logStreamErrorIfActive(error, userId, OperationType.GET, path)
                    throw error
                }
        )
    }

    fun observeApplications(userId: String): Flow<List<JobApplication>> = flow {
        val path = "users/$userId/applications"
        emitAll(
            db.collection("users").document(userId).collection("applications")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        JobApplication::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt?.seconds ?: it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logStreamErrorIfActive(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeSchedules(userId: String): Flow<List<InterviewSchedule>> = flow {
        val path = "users/$userId/schedules"
        emitAll(
            db.collection("users").document(userId).collection("schedules")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        InterviewSchedule::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.scheduledAtMillis }
                }
                .catch { error ->
                    logStreamErrorIfActive(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeDocuments(userId: String): Flow<List<CareerDocument>> = flow {
        val path = "users/$userId/documents"
        emitAll(
            db.collection("users").document(userId).collection("documents")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CareerDocument::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt?.seconds ?: it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logStreamErrorIfActive(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    private fun logStreamErrorIfActive(
        error: Throwable,
        userId: String,
        operationType: OperationType,
        path: String
    ) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val toLog = firestoreError ?: error
        if (auth.currentUser?.uid == userId) {
            handleFirestoreError(toLog, operationType, path)
        }
    }

    // --- USER PROFILE OPERATIONS ---

    suspend fun ensureUserProfile(
        userId: String,
        defaultName: String,
        photoUrl: String?
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireVerifiedUid(userId)
            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val safeName = defaultName.trim().ifBlank { "Pengguna KarirKu" }.take(100)
                val payload = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "displayName" to safeName,
                    "headline" to "Profesional Karier",
                    "location" to "Indonesia",
                    "availabilityStatus" to AvailabilityStatus.OPEN_TO_WORK.name,
                    "targetRoles" to emptyList<String>(),
                    "expectedSalaryMin" to 0L,
                    "expectedSalaryMax" to 0L,
                    "preferredWorkTypes" to listOf("Hybrid", "Remote"),
                    "followUpRemindersEnabled" to true,
                    "darkModeEnabled" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (!photoUrl.isNullOrBlank()) {
                    payload["photoUrl"] = photoUrl.take(500)
                }
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    private suspend fun ensureProfileDocumentExists(uid: String) {
        val docRef = db.collection("users").document(uid)
        val existing = docRef.get().await()
        if (!existing.exists()) {
            val user = auth.currentUser
            val safeName = user?.displayName?.trim()?.ifBlank { "Pengguna KarirKu" }?.take(100)
                ?: "Pengguna KarirKu"
            val payload = mutableMapOf<String, Any>(
                "userId" to uid,
                "displayName" to safeName,
                "headline" to "Profesional Karier",
                "location" to "Indonesia",
                "availabilityStatus" to AvailabilityStatus.OPEN_TO_WORK.name,
                "targetRoles" to emptyList<String>(),
                "expectedSalaryMin" to 0L,
                "expectedSalaryMax" to 0L,
                "preferredWorkTypes" to listOf("Hybrid", "Remote"),
                "followUpRemindersEnabled" to true,
                "darkModeEnabled" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val photo = user?.photoUrl?.toString()
            if (!photo.isNullOrBlank()) {
                payload["photoUrl"] = photo.take(500)
            }
            docRef.set(payload).await()
        }
    }

    suspend fun updateProfileIdentity(
        userId: String,
        displayName: String,
        headline: String,
        location: String,
        availabilityStatus: AvailabilityStatus
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireVerifiedUid(userId)
            val cleanName = displayName.trim().take(100)
            require(cleanName.isNotEmpty()) { "Nama lengkap tidak boleh kosong." }
            ensureProfileDocumentExists(uid)
            db.collection("users").document(uid).update(
                mapOf(
                    "displayName" to cleanName,
                    "headline" to headline.trim().take(120),
                    "location" to location.trim().take(100),
                    "availabilityStatus" to availabilityStatus.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateCareerPreferences(
        userId: String,
        targetRoles: List<String>,
        expectedSalaryMin: Long,
        expectedSalaryMax: Long,
        preferredWorkTypes: List<String>
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireVerifiedUid(userId)
            val cleanRoles = targetRoles.map { it.trim().take(200) }.filter { it.isNotEmpty() }.take(10)
            val cleanWorkTypes = preferredWorkTypes.map { it.trim().take(200) }.filter { it.isNotEmpty() }.take(10)
            val safeMin = expectedSalaryMin.coerceIn(0L, 10_000_000_000L)
            val safeMax = expectedSalaryMax.coerceIn(0L, 10_000_000_000L)
            ensureProfileDocumentExists(uid)
            db.collection("users").document(uid).update(
                mapOf(
                    "targetRoles" to cleanRoles,
                    "expectedSalaryMin" to safeMin,
                    "expectedSalaryMax" to maxOf(safeMin, safeMax),
                    "preferredWorkTypes" to cleanWorkTypes,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateSettings(
        userId: String,
        followUpRemindersEnabled: Boolean,
        darkModeEnabled: Boolean
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireVerifiedUid(userId)
            ensureProfileDocumentExists(uid)
            db.collection("users").document(uid).update(
                mapOf(
                    "followUpRemindersEnabled" to followUpRemindersEnabled,
                    "darkModeEnabled" to darkModeEnabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- JOB APPLICATION OPERATIONS ---

    suspend fun saveApplication(
        userId: String,
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
        existingHistory: List<String> = emptyList(),
        previousStatus: ApplicationStatus? = null
    ): Result<String> {
        var docPath = "users/$userId/applications"
        return try {
            val uid = requireVerifiedUid(userId)
            val cleanRole = roleTitle.trim().take(120)
            val cleanCompany = companyName.trim().take(120)
            require(cleanRole.isNotEmpty()) { "Judul posisi wajib diisi." }
            require(cleanCompany.isNotEmpty()) { "Nama perusahaan wajib diisi." }

            val safeMin = salaryMin.coerceIn(0L, 10_000_000_000L)
            val safeMax = salaryMax.coerceIn(0L, 10_000_000_000L)
            val cleanAttachments = attachments.map { it.trim().take(200) }.filter { it.isNotEmpty() }.take(10)
            val colRef = db.collection("users").document(uid).collection("applications")
            val nowMs = System.currentTimeMillis()

            if (existingId.isNullOrBlank()) {
                val docRef = colRef.document()
                docPath = docRef.path
                val code = existingDossierCode?.takeIf { it.isNotBlank() }?.take(20)
                    ?: KarirKuFormatters.generateDossierCode()
                val initialHistory = listOf(
                    StageHistoryEvent(
                        status = status,
                        timestampMillis = nowMs,
                        note = "Lamaran dicatat (${status.shortLabel})"
                    ).encode()
                )
                val payload = mapOf(
                    "userId" to uid,
                    "dossierCode" to code,
                    "roleTitle" to cleanRole,
                    "companyName" to cleanCompany,
                    "location" to location.trim().take(100),
                    "status" to status.name,
                    "workMode" to workMode.name,
                    "contractType" to contractType.name,
                    "salaryMin" to safeMin,
                    "salaryMax" to safeMax,
                    "appliedDateMillis" to appliedDateMillis.coerceAtLeast(0L),
                    "targetResponseMillis" to targetResponseMillis.coerceAtLeast(0L),
                    "notes" to notes.trim().take(3000),
                    "hrContact" to hrContact.trim().take(200),
                    "attachments" to cleanAttachments,
                    "stageHistory" to initialHistory,
                    "starred" to starred,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                Result.success(docRef.id)
            } else {
                val docRef = colRef.document(existingId)
                docPath = docRef.path
                val updatedHistory = existingHistory.toMutableList()
                if (previousStatus == null || previousStatus != status) {
                    updatedHistory.add(
                        StageHistoryEvent(
                            status = status,
                            timestampMillis = nowMs,
                            note = "Status diubah ke ${status.shortLabel}"
                        ).encode()
                    )
                } else if (updatedHistory.isEmpty()) {
                    updatedHistory.add(
                        StageHistoryEvent(
                            status = status,
                            timestampMillis = nowMs,
                            note = "Data lamaran diperbarui"
                        ).encode()
                    )
                }
                val trimmedHistory = updatedHistory.takeLast(30)
                val updates = mapOf(
                    "roleTitle" to cleanRole,
                    "companyName" to cleanCompany,
                    "location" to location.trim().take(100),
                    "status" to status.name,
                    "workMode" to workMode.name,
                    "contractType" to contractType.name,
                    "salaryMin" to safeMin,
                    "salaryMax" to safeMax,
                    "appliedDateMillis" to appliedDateMillis.coerceAtLeast(0L),
                    "targetResponseMillis" to targetResponseMillis.coerceAtLeast(0L),
                    "notes" to notes.trim().take(3000),
                    "hrContact" to hrContact.trim().take(200),
                    "attachments" to cleanAttachments,
                    "stageHistory" to trimmedHistory,
                    "starred" to starred,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
                Result.success(existingId)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(
                e,
                if (existingId.isNullOrBlank()) OperationType.CREATE else OperationType.UPDATE,
                docPath
            )
            Result.failure(e)
        }
    }

    suspend fun toggleApplicationStar(
        userId: String,
        applicationId: String,
        starred: Boolean
    ): Result<Unit> {
        val docPath = "users/$userId/applications/$applicationId"
        return try {
            val uid = requireVerifiedUid(userId)
            require(applicationId.isNotBlank()) { "ID lamaran tidak valid." }
            db.collection("users").document(uid).collection("applications")
                .document(applicationId)
                .update(
                    mapOf(
                        "starred" to starred,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateApplicationStatus(
        userId: String,
        applicationId: String,
        status: ApplicationStatus,
        existingHistory: List<String> = emptyList()
    ): Result<Unit> {
        val docPath = "users/$userId/applications/$applicationId"
        return try {
            val uid = requireVerifiedUid(userId)
            require(applicationId.isNotBlank()) { "ID lamaran tidak valid." }
            val nextHistory = (existingHistory + StageHistoryEvent(
                status = status,
                timestampMillis = System.currentTimeMillis(),
                note = "Pindah ke tahap ${status.shortLabel}"
            ).encode()).takeLast(30)
            db.collection("users").document(uid).collection("applications")
                .document(applicationId)
                .update(
                    mapOf(
                        "status" to status.name,
                        "stageHistory" to nextHistory,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteApplication(userId: String, applicationId: String): Result<Unit> {
        val docPath = "users/$userId/applications/$applicationId"
        return try {
            val uid = requireVerifiedUid(userId)
            require(applicationId.isNotBlank()) { "ID lamaran tidak valid." }
            db.collection("users").document(uid).collection("applications")
                .document(applicationId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- INTERVIEW SCHEDULE OPERATIONS ---

    suspend fun saveSchedule(
        userId: String,
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
        completed: Boolean
    ): Result<String> {
        var docPath = "users/$userId/schedules"
        return try {
            val uid = requireVerifiedUid(userId)
            val cleanCompany = companyName.trim().take(120)
            val cleanRole = roleTitle.trim().take(120)
            val cleanStage = stageTitle.trim().take(80)
            require(cleanCompany.isNotEmpty()) { "Nama perusahaan wajib diisi." }
            require(cleanRole.isNotEmpty()) { "Judul posisi wajib diisi." }
            require(cleanStage.isNotEmpty()) { "Nama tahap wawancara wajib diisi." }

            val colRef = db.collection("users").document(uid).collection("schedules")
            val safeDuration = durationMinutes.coerceIn(5L, 480L)

            if (existingId.isNullOrBlank()) {
                val docRef = colRef.document()
                docPath = docRef.path
                val payload = mapOf(
                    "userId" to uid,
                    "applicationId" to applicationId.trim().take(128),
                    "companyName" to cleanCompany,
                    "roleTitle" to cleanRole,
                    "stageTitle" to cleanStage,
                    "scheduledAtMillis" to scheduledAtMillis.coerceAtLeast(0L),
                    "durationMinutes" to safeDuration,
                    "meetingFormat" to meetingFormat.name,
                    "meetingLocation" to meetingLocation.trim().take(300),
                    "interviewerName" to interviewerName.trim().take(120),
                    "prepNotes" to prepNotes.trim().take(2000),
                    "completed" to completed,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                Result.success(docRef.id)
            } else {
                val docRef = colRef.document(existingId)
                docPath = docRef.path
                docRef.update(
                    mapOf(
                        "applicationId" to applicationId.trim().take(128),
                        "companyName" to cleanCompany,
                        "roleTitle" to cleanRole,
                        "stageTitle" to cleanStage,
                        "scheduledAtMillis" to scheduledAtMillis.coerceAtLeast(0L),
                        "durationMinutes" to safeDuration,
                        "meetingFormat" to meetingFormat.name,
                        "meetingLocation" to meetingLocation.trim().take(300),
                        "interviewerName" to interviewerName.trim().take(120),
                        "prepNotes" to prepNotes.trim().take(2000),
                        "completed" to completed,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                Result.success(existingId)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(
                e,
                if (existingId.isNullOrBlank()) OperationType.CREATE else OperationType.UPDATE,
                docPath
            )
            Result.failure(e)
        }
    }

    suspend fun toggleScheduleCompleted(
        userId: String,
        scheduleId: String,
        completed: Boolean
    ): Result<Unit> {
        val docPath = "users/$userId/schedules/$scheduleId"
        return try {
            val uid = requireVerifiedUid(userId)
            require(scheduleId.isNotBlank()) { "ID jadwal tidak valid." }
            db.collection("users").document(uid).collection("schedules")
                .document(scheduleId)
                .update(
                    mapOf(
                        "completed" to completed,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteSchedule(userId: String, scheduleId: String): Result<Unit> {
        val docPath = "users/$userId/schedules/$scheduleId"
        return try {
            val uid = requireVerifiedUid(userId)
            require(scheduleId.isNotBlank()) { "ID jadwal tidak valid." }
            db.collection("users").document(uid).collection("schedules")
                .document(scheduleId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- CAREER DOCUMENT OPERATIONS ---

    suspend fun saveDocument(
        userId: String,
        existingId: String?,
        title: String,
        category: DocumentCategory,
        subtitle: String,
        externalUrl: String,
        verified: Boolean
    ): Result<String> {
        var docPath = "users/$userId/documents"
        return try {
            val uid = requireVerifiedUid(userId)
            val cleanTitle = title.trim().take(120)
            require(cleanTitle.isNotEmpty()) { "Judul dokumen wajib diisi." }
            val colRef = db.collection("users").document(uid).collection("documents")

            if (existingId.isNullOrBlank()) {
                val docRef = colRef.document()
                docPath = docRef.path
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "title" to cleanTitle,
                        "category" to category.name,
                        "subtitle" to subtitle.trim().take(160),
                        "externalUrl" to externalUrl.trim().take(500),
                        "verified" to verified,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                Result.success(docRef.id)
            } else {
                val docRef = colRef.document(existingId)
                docPath = docRef.path
                docRef.update(
                    mapOf(
                        "title" to cleanTitle,
                        "category" to category.name,
                        "subtitle" to subtitle.trim().take(160),
                        "externalUrl" to externalUrl.trim().take(500),
                        "verified" to verified,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                Result.success(existingId)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(
                e,
                if (existingId.isNullOrBlank()) OperationType.CREATE else OperationType.UPDATE,
                docPath
            )
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(userId: String, documentId: String): Result<Unit> {
        val docPath = "users/$userId/documents/$documentId"
        return try {
            val uid = requireVerifiedUid(userId)
            require(documentId.isNotBlank()) { "ID dokumen tidak valid." }
            db.collection("users").document(uid).collection("documents")
                .document(documentId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }
}
