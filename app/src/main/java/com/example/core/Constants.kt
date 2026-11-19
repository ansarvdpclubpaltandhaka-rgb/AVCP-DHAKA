package com.example.core

object AppConstants {
    const val DEFAULT_ORG_ID = "ansar_vdp_paltan_dhaka"
    const val SUPER_ADMIN_UID = "GpfMc9GLItN0NRDoeHW9OnCPNre2"
    const val ADMIN_EMAIL = "ansarvdpclubpaltandhaka@gmail.com"
    const val ADMIN_PASSWORD = "Paltan@2026"

    fun isSuperAdminUser(uid: String?, email: String?): Boolean =
        uid == SUPER_ADMIN_UID ||
            (!email.isNullOrBlank() && email.trim().equals(ADMIN_EMAIL, ignoreCase = true))

    fun verifyAdminCredentials(email: String, password: String): Boolean =
        email.trim().equals(ADMIN_EMAIL, ignoreCase = true) && password == ADMIN_PASSWORD

    fun normalizePhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return if (digits.startsWith("88") && digits.length > 11) digits.removePrefix("88") else digits
    }

    fun hashPassword(password: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    const val CLUB_NAME_BN = "আনসার ভিডিপি ক্লাব পল্টন ঢাকা"
    const val ORG_NAME_BN = "বাংলাদেশ সেবামূলক সংগঠন"
    const val ORG_TAGLINE_BN = "মানুষের পাশে, মানবতার জন্য"
    const val ORG_SHORT_TAGLINE_BN = "মানবতার জন্য"

    const val ROLE_MEMBER = "member"
    const val ROLE_ADMIN = "admin"
    const val ROLE_SUPER_ADMIN = "super_admin"

    const val STATUS_PENDING = "pending"
    const val STATUS_APPROVED = "approved"
    const val STATUS_ACTIVE = "active"
    const val STATUS_REJECTED = "rejected"

    const val MSG_MEMBER_PENDING = "আপনার আবেদন পেন্ডিং আছে, অ্যাডমিন Approve করলে লগইন করতে পারবেন"
    const val MSG_MEMBER_REJECTED = "আপনার আবেদন অ্যাডমিন কর্তৃক বাতিল (Reject) করা হয়েছে।"

    fun isApprovedMemberStatus(status: String?): Boolean =
        status.equals(STATUS_APPROVED, ignoreCase = true) ||
            status.equals(STATUS_ACTIVE, ignoreCase = true)

    val MEMBER_TRADES = listOf(
        "পুরুষ টিডিপি",
        "মহিলা টিডিপি",
        "ড্রাইভার",
        "থানা আনসার",
        "দলনেতা",
        "দলনেত্রী",
        "সদস্য"
    )

    const val PERM_MEMBERS = "manage_members"
    const val PERM_SERVICES = "manage_services"
    const val PERM_ACTIVITIES = "manage_activities"
    const val PERM_BLOOD = "manage_blood"
    const val PERM_FINANCE = "manage_finance"
    const val PERM_NOTICES = "manage_notices"
    const val PERM_REQUESTS = "manage_requests"
    const val PERM_SETTINGS = "manage_settings"

    val ALL_PERMISSIONS: List<Pair<String, String>> = listOf(
        PERM_MEMBERS to "সদস্য ও স্বেচ্ছাসেবক ব্যবস্থাপনা",
        PERM_SERVICES to "সেবার ধরন ব্যবস্থাপনা",
        PERM_ACTIVITIES to "মানবিক কার্যক্রম ব্যবস্থাপনা",
        PERM_BLOOD to "রক্তদান ও আবেদন ব্যবস্থাপনা",
        PERM_FINANCE to "অর্থ ও হিসাব ব্যবস্থাপনা",
        PERM_NOTICES to "নোটিশ ও ইভেন্ট ব্যবস্থাপনা",
        PERM_REQUESTS to "সেবা ও চিকিৎসা আবেদন পর্যালোচনা",
        PERM_SETTINGS to "সংগঠন ও ইমেজ হোস্টিং সেটিংস"
    )

    val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    val SERVICE_ICON_KEYS: List<Pair<String, String>> = listOf(
        "blood" to "রক্তদান",
        "medical" to "চিকিৎসা সহায়তা",
        "food" to "খাদ্য বিতরণ",
        "education" to "শিক্ষা সহায়তা",
        "winter" to "শীতবস্ত্র বিতরণ",
        "relief" to "ত্রাণ বিতরণ",
        "emergency" to "জরুরি সহায়তা",
        "family" to "দরিদ্র পরিবার সহায়তা",
        "construction" to "নির্মাণ/সেবা সহায়তা",
        "environment" to "পরিবেশ কার্যক্রম",
        "volunteer" to "স্বেচ্ছাসেবী কার্যক্রম",
        "humanitarian" to "অন্যান্য মানবিক সেবা"
    )

    val PAYMENT_METHODS = listOf("নগদ", "বিকাশ", "নগদ (অ্যাপ)", "রকেট", "ব্যাংক ট্রান্সফার")
}
