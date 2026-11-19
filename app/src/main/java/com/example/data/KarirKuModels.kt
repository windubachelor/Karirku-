package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

enum class ApplicationStatus(
    val label: String,
    val shortLabel: String,
    val badgeText: String
) {
    DISIMPAN("Disimpan", "Wishlist", "Disimpan di Wishlist"),
    DILAMAR("Dilamar", "Terkirim", "Lamaran Terkirim"),
    ADMINISTRASI("Administrasi", "Screening", "Lolos Administrasi"),
    INTERVIEW("Interview Aktif", "Interview", "Interview HR & User"),
    OFFER("Offer Disetujui", "Offer", "Offer Diterima"),
    DITOLAK("Ditolak", "Ditolak", "Belum Cocok");

    companion object {
        fun fromRaw(raw: String): ApplicationStatus =
            entries.firstOrNull { it.name == raw } ?: DISIMPAN
    }
}

enum class WorkMode(val label: String) {
    HYBRID("Hybrid"),
    REMOTE("Remote"),
    ONSITE("On-site");

    companion object {
        fun fromRaw(raw: String): WorkMode =
            entries.firstOrNull { it.name == raw } ?: HYBRID
    }
}

enum class ContractType(val label: String) {
    FULL_TIME("Full-time"),
    KONTRAK("Kontrak"),
    FREELANCE("Freelance"),
    MAGANG("Magang");

    companion object {
        fun fromRaw(raw: String): ContractType =
            entries.firstOrNull { it.name == raw } ?: FULL_TIME
    }
}

enum class AvailabilityStatus(val label: String) {
    OPEN_TO_WORK("Open to Work"),
    ACTIVELY_LOOKING("Aktif Mencari"),
    PASSIVE("Terbuka Penawaran"),
    NOT_LOOKING("Tidak Mencari");

    companion object {
        fun fromRaw(raw: String): AvailabilityStatus =
            entries.firstOrNull { it.name == raw } ?: OPEN_TO_WORK
    }
}

enum class MeetingFormat(val label: String) {
    ONLINE("Online Meet"),
    ONSITE("Kantor / On-site"),
    PHONE("Telepon");

    companion object {
        fun fromRaw(raw: String): MeetingFormat =
            entries.firstOrNull { it.name == raw } ?: ONLINE
    }
}

enum class DocumentCategory(val label: String) {
    CV("CV / Resume"),
    PORTFOLIO("Portofolio"),
    CERTIFICATION("Sertifikasi"),
    OTHER("Dokumen Lain");

    companion object {
        fun fromRaw(raw: String): DocumentCategory =
            entries.firstOrNull { it.name == raw } ?: CV
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val headline: String = "",
    val location: String = "",
    val availabilityStatus: String = AvailabilityStatus.OPEN_TO_WORK.name,
    val photoUrl: String? = null,
    val targetRoles: List<String> = emptyList(),
    val expectedSalaryMin: Long = 0L,
    val expectedSalaryMax: Long = 0L,
    val preferredWorkTypes: List<String> = emptyList(),
    val followUpRemindersEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val availabilityEnum: AvailabilityStatus
        get() = AvailabilityStatus.fromRaw(availabilityStatus)
}

@Keep
@IgnoreExtraProperties
data class JobApplication(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dossierCode: String = "",
    val roleTitle: String = "",
    val companyName: String = "",
    val location: String = "",
    val status: String = ApplicationStatus.DILAMAR.name,
    val workMode: String = WorkMode.HYBRID.name,
    val contractType: String = ContractType.FULL_TIME.name,
    val salaryMin: Long = 0L,
    val salaryMax: Long = 0L,
    val appliedDateMillis: Long = 0L,
    val targetResponseMillis: Long = 0L,
    val notes: String = "",
    val hrContact: String = "",
    val attachments: List<String> = emptyList(),
    val starred: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val statusEnum: ApplicationStatus
        get() = ApplicationStatus.fromRaw(status)

    val workModeEnum: WorkMode
        get() = WorkMode.fromRaw(workMode)

    val contractTypeEnum: ContractType
        get() = ContractType.fromRaw(contractType)
}

@Keep
@IgnoreExtraProperties
data class InterviewSchedule(
    @DocumentId val id: String = "",
    val userId: String = "",
    val applicationId: String = "",
    val companyName: String = "",
    val roleTitle: String = "",
    val stageTitle: String = "",
    val scheduledAtMillis: Long = 0L,
    val durationMinutes: Long = 60L,
    val meetingFormat: String = MeetingFormat.ONLINE.name,
    val meetingLocation: String = "",
    val interviewerName: String = "",
    val prepNotes: String = "",
    val completed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val formatEnum: MeetingFormat
        get() = MeetingFormat.fromRaw(meetingFormat)
}

@Keep
@IgnoreExtraProperties
data class CareerDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = DocumentCategory.CV.name,
    val subtitle: String = "",
    val externalUrl: String = "",
    val verified: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val categoryEnum: DocumentCategory
        get() = DocumentCategory.fromRaw(category)
}

// --- FORMATTING UTILITIES ---

object KarirKuFormatters {
    private val indonesiaLocale = Locale("in", "ID")

    fun formatIdrFull(amount: Long): String {
        if (amount <= 0L) return "Rp 0"
        val nf = NumberFormat.getNumberInstance(indonesiaLocale)
        return "Rp ${nf.format(amount)}"
    }

    fun formatIdrCompactRange(min: Long, max: Long): String {
        if (min <= 0L && max <= 0L) return "Gaji belum diisi"
        val minStr = formatShortMillions(min)
        val maxStr = formatShortMillions(max)
        return when {
            min > 0L && max > 0L && min != max -> "Rp $minStr - $maxStr / bln"
            max > 0L -> "Rp $maxStr / bln"
            else -> "Rp $minStr / bln"
        }
    }

    fun formatShortMillions(amount: Long): String {
        if (amount <= 0L) return "0"
        return if (amount >= 1_000_000L) {
            val millions = amount / 1_000_000.0
            if (millions % 1.0 == 0.0) {
                "${millions.toLong()}jt"
            } else {
                String.format(Locale.US, "%.1fjt", millions)
            }
        } else if (amount >= 1_000L) {
            "${amount / 1_000L}rb"
        } else {
            amount.toString()
        }
    }

    fun formatStatAverageMillions(avg: Long): String {
        if (avg <= 0L) return "-"
        val millions = avg / 1_000_000.0
        return if (millions >= 1.0) {
            if (millions % 1.0 == 0.0) "${millions.toLong()}M"
            else String.format(Locale.US, "%.1fM", millions)
        } else {
            "${avg / 1_000L}K"
        }
    }

    fun formatDateShort(millis: Long): String {
        if (millis <= 0L) return "-"
        return SimpleDateFormat("dd/MM/yyyy", indonesiaLocale).format(Date(millis))
    }

    fun formatDateEditorial(millis: Long): String {
        if (millis <= 0L) return "-"
        return SimpleDateFormat("EEE, dd MMM yyyy", indonesiaLocale).format(Date(millis))
    }

    fun formatTime(millis: Long): String {
        if (millis <= 0L) return "--:--"
        return SimpleDateFormat("HH:mm", indonesiaLocale).format(Date(millis))
    }

    fun formatRelativeTime(timestamp: Timestamp?): String {
        val ms = timestamp?.toDate()?.time ?: return "Baru saja"
        val diff = abs(System.currentTimeMillis() - ms)
        val minutes = diff / 60_000L
        val hours = minutes / 60L
        val days = hours / 24L
        return when {
            minutes < 2 -> "Baru saja"
            minutes < 60 -> "$minutes mnt lalu"
            hours < 24 -> "$hours jam lalu"
            days < 7 -> "$days hr lalu"
            days < 30 -> "${days / 7} mgg lalu"
            else -> "${days / 30} bln lalu"
        }
    }

    fun companyMonogram(companyName: String): String {
        val cleaned = companyName.trim().uppercase(Locale.US)
        if (cleaned.isBlank()) return "KRK"
        val words = cleaned.split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            words.size == 1 -> words[0].take(4)
            words.size >= 2 && words[0].length <= 4 -> words[0]
            else -> words.take(3).joinToString("") { it.take(1) }
        }
    }

    fun generateDossierCode(): String {
        val code = (1000..9999).random()
        return "#ID-$code"
    }
}
