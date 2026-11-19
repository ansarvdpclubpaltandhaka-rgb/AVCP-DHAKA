package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class OrganizationConfig(
    @DocumentId val id: String = "",
    val name: String = "বাংলাদেশ সেবামূলক সংগঠন",
    val tagline: String = "মানুষের পাশে, মানবতার জন্য",
    val clubName: String = "আনসার ভিডিপি ক্লাব পল্টন ঢাকা",
    val address: String = "পল্টন, ঢাকা",
    val phone: String = "",
    val email: String = "",
    val imageUploadProvider: String = "none", // "none", "cloudinary", "custom"
    val cloudinaryCloudName: String = "",
    val cloudinaryUploadPreset: String = "",
    val customUploadUrl: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val photoURL: String = "",
    val organizationId: String = "",
    val role: String = "member",
    val permissions: List<String> = emptyList(),
    val status: String = "active",
    val bloodGroup: String = "",
    val positionTitle: String = "",
    val platoon: String = "",
    val trade: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DirectoryEntry(
    @DocumentId val id: String = "",
    val linkedUid: String = "",
    val organizationId: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val bloodGroup: String = "",
    val position: String = "",
    val platoon: String = "",
    val trade: String = "",
    val joiningDate: String = "",
    val status: String = "active",
    val volunteer: Boolean = false,
    val skills: String = "",
    val availability: String = "",
    val bloodDonor: Boolean = false,
    val donorAvailable: Boolean = false,
    val donationCount: Long = 0L,
    val lastDonation: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class MemberRecord(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val linkedUid: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val phone: String = "",
    val email: String = "",
    val bloodGroup: String = "",
    val position: String = "",
    val platoon: String = "",
    val trade: String = "",
    val joiningDate: String = "",
    val address: String = "",
    val status: String = "active",
    val bloodDonor: Boolean = false,
    val donorAvailable: Boolean = false,
    val donationCount: Long = 0L,
    val lastDonation: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class VolunteerRecord(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val linkedUid: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val phone: String = "",
    val skills: String = "",
    val availability: String = "",
    val bloodGroup: String = "",
    val joiningDate: String = "",
    val status: String = "active",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PositionRecord(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val title: String = "",
    val rankOrder: Long = 0L,
    val holderName: String = "",
    val holderPhotoUrl: String = "",
    val description: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ServiceCategory(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val title: String = "",
    val description: String = "",
    val iconKey: String = "humanitarian",
    val active: Boolean = true,
    val sortOrder: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ServiceRequest(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val userId: String = "",
    val requesterName: String = "",
    val serviceId: String = "",
    val serviceTitle: String = "",
    val details: String = "",
    val location: String = "",
    val contactPhone: String = "",
    val urgency: String = "normal",
    val status: String = "pending",
    val adminNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class MedicalCase(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val userId: String = "",
    val patientName: String = "",
    val conditionSummary: String = "",
    val hospitalName: String = "",
    val estimatedCost: Double = 0.0,
    val contactPhone: String = "",
    val status: String = "pending",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BloodRequest(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val createdByUid: String = "",
    val patientName: String = "",
    val bloodGroup: String = "",
    val hospital: String = "",
    val location: String = "",
    val requiredDate: String = "",
    val units: Long = 1L,
    val contact: String = "",
    val urgency: String = "urgent",
    val status: String = "open",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ActivityRecord(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val date: String = "",
    val location: String = "",
    val participants: Long = 0L,
    val beneficiaries: Long = 0L,
    val budget: Double = 0.0,
    val status: String = "ongoing",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ContributionRecord(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val memberUid: String = "",
    val memberName: String = "",
    val expectedAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val month: String = "",
    val paymentDate: String = "",
    val paymentMethod: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DonationRecord(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val donorName: String = "",
    val amount: Double = 0.0,
    val purpose: String = "",
    val donationDate: String = "",
    val paymentMethod: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ExpenseRecord(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val title: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val expenseDate: String = "",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class FinancialSummary(
    val totalContributions: Double = 0.0,
    val totalDonations: Double = 0.0,
    val totalExpenses: Double = 0.0
) {
    val currentBalance: Double get() = totalContributions + totalDonations - totalExpenses
}

@Keep
@IgnoreExtraProperties
data class AllowanceRecord(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val memberUid: String = "",
    val memberPhone: String = "",
    val memberName: String = "",
    val platoon: String = "",
    val areaOrDuty: String = "",
    val date: String = "",
    val dailyAmount: Long = 0L,
    val monthlyTotal: Long = 0L,
    val paid: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class NoticeRecord(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val title: String = "",
    val body: String = "",
    val noticeType: String = "notice",
    val pinned: Boolean = false,
    val createdByUid: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class OrgEventRecord(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val title: String = "",
    val description: String = "",
    val eventType: String = "event",
    val eventDate: String = "",
    val eventTime: String = "",
    val location: String = "",
    val createdByUid: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ConversationRecord(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantPhotos: Map<String, String> = emptyMap(),
    val lastMessageText: String = "",
    val lastSenderId: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ChatMessageRecord(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val participantIds: List<String> = emptyList(),
    val text: String = "",
    val imageUrl: String = "",
    val seen: Boolean = false,
    @ServerTimestamp val seenAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class NotificationRecord(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val targetScope: String = "all",
    val targetUserId: String = "",
    val senderUid: String = "",
    val category: String = "notice",
    val title: String = "",
    val body: String = "",
    val readBy: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)
