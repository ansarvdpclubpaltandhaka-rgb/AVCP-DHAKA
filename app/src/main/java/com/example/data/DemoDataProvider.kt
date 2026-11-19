package com.example.data

import com.example.core.AppConstants
import com.example.model.*

object DemoDataProvider {

    const val DEMO_TOTAL_MEMBERS = 48
    const val DEMO_PRESENT_TODAY = 34
    const val DEMO_ON_FIELD = 28
    const val DEMO_WORKING_NOW = 22
    const val DEMO_ALLOWANCE_BDT = 17000L

    val demoGuestProfile = UserProfile(
        id = "demo_guest",
        uid = "demo_guest",
        name = "অতিথি সদস্য (ডেমো)",
        email = "",
        phone = "",
        photoURL = "",
        organizationId = AppConstants.DEFAULT_ORG_ID,
        role = AppConstants.ROLE_MEMBER,
        permissions = emptyList(),
        status = "active",
        bloodGroup = "O+",
        positionTitle = "সদস্য (ডেমো)"
    )

    val demoOrganizationConfig = OrganizationConfig(
        id = AppConstants.DEFAULT_ORG_ID,
        name = AppConstants.ORG_NAME_BN,
        tagline = AppConstants.ORG_TAGLINE_BN,
        clubName = AppConstants.CLUB_NAME_BN,
        address = "পল্টন, ঢাকা-১০০০"
    )

    val demoDirectory = listOf(
        DirectoryEntry(
            id = "d1", linkedUid = "demo_1", organizationId = AppConstants.DEFAULT_ORG_ID,
            name = "মো. রুহুল আমিন", bloodGroup = "O+", position = "ক্লাব সভাপতি",
            platoon = "পল্টন প্লাটুন-১", trade = "দলনেতা",
            status = "active", volunteer = true, skills = "জরুরি ত্রাণ ও সমন্বয়",
            availability = "সর্বদা প্রস্তুত", bloodDonor = true, donorAvailable = true,
            donationCount = 8, lastDonation = "১০-০৯-২০২৬"
        ),
        DirectoryEntry(
            id = "d2", linkedUid = "demo_2", organizationId = AppConstants.DEFAULT_ORG_ID,
            name = "তানভীর আহমেদ", bloodGroup = "A+", position = "সাধারণ সম্পাদক",
            platoon = "সদর প্লাটুন", trade = "পুরুষ টিডিপি",
            status = "active", volunteer = true, skills = "চিকিৎসা ক্যাম্প ব্যবস্থাপনা",
            availability = "বিকাল ও সপ্তাহান্ত", bloodDonor = true, donorAvailable = true,
            donationCount = 5, lastDonation = "০২-১০-২০২৬"
        ),
        DirectoryEntry(
            id = "d3", linkedUid = "demo_3", organizationId = AppConstants.DEFAULT_ORG_ID,
            name = "সাদিয়া ইসলাম", bloodGroup = "B+", position = "স্বেচ্ছাসেবক সমন্বয়ক",
            platoon = "জরুরি সেবা প্লাটুন", trade = "মহিলা টিডিপি",
            status = "active", volunteer = true, skills = "প্রাথমিক চিকিৎসা ও শিক্ষা",
            availability = "সপ্তাহান্তে", bloodDonor = true, donorAvailable = false,
            donationCount = 3, lastDonation = "১৫-১০-২০২৬"
        )
    )

    val demoMembers = listOf(
        MemberRecord(
            id = "d1", organizationId = AppConstants.DEFAULT_ORG_ID, linkedUid = "demo_1",
            name = "মো. রুহুল আমিন", phone = "01711-000001", email = "ruhul@paltanclub.org",
            bloodGroup = "O+", position = "ক্লাব সভাপতি", platoon = "পল্টন প্লাটুন-১", trade = "দলনেতা",
            joiningDate = "০১-০১-২০২৪", address = "পল্টন, ঢাকা", status = "active",
            bloodDonor = true, donorAvailable = true, donationCount = 8, lastDonation = "১০-০৯-২০২৬"
        ),
        MemberRecord(
            id = "d2", organizationId = AppConstants.DEFAULT_ORG_ID, linkedUid = "demo_2",
            name = "তানভীর আহমেদ", phone = "01711-000002", email = "tanvir@paltanclub.org",
            bloodGroup = "A+", position = "সাধারণ সম্পাদক", platoon = "সদর প্লাটুন", trade = "পুরুষ টিডিপি",
            joiningDate = "১৫-০২-২০২৪", address = "নয়াপল্টন, ঢাকা", status = "active",
            bloodDonor = true, donorAvailable = true, donationCount = 5, lastDonation = "০২-১০-২০২৬"
        ),
        MemberRecord(
            id = "d3", organizationId = AppConstants.DEFAULT_ORG_ID, linkedUid = "demo_3",
            name = "সাদিয়া ইসলাম", phone = "01711-000003", email = "sadia@paltanclub.org",
            bloodGroup = "B+", position = "স্বেচ্ছাসেবক সমন্বয়ক", platoon = "জরুরি সেবা প্লাটুন", trade = "মহিলা টিডিপি",
            joiningDate = "১০-০৪-২০২৪", address = "পুরানা পল্টন, ঢাকা", status = "active",
            bloodDonor = true, donorAvailable = false, donationCount = 3, lastDonation = "১৫-১০-২০২৬"
        )
    )

    val demoVolunteers = listOf(
        VolunteerRecord(
            id = "v1", organizationId = AppConstants.DEFAULT_ORG_ID, linkedUid = "demo_1",
            name = "মো. রুহুল আমিন", phone = "01711-000001", skills = "জরুরি ত্রাণ ও সমন্বয়",
            availability = "সর্বদা প্রস্তুত", bloodGroup = "O+", joiningDate = "০১-০১-২০২৪", status = "active"
        ),
        VolunteerRecord(
            id = "v2", organizationId = AppConstants.DEFAULT_ORG_ID, linkedUid = "demo_2",
            name = "তানভীর আহমেদ", phone = "01711-000002", skills = "চিকিৎসা ক্যাম্প ব্যবস্থাপনা",
            availability = "বিকাল ও সপ্তাহান্ত", bloodGroup = "A+", joiningDate = "১৫-০২-২০২৪", status = "active"
        ),
        VolunteerRecord(
            id = "v3", organizationId = AppConstants.DEFAULT_ORG_ID, linkedUid = "demo_3",
            name = "সাদিয়া ইসলাম", phone = "01711-000003", skills = "প্রাথমিক চিকিৎসা ও শিক্ষা",
            availability = "সপ্তাহান্তে", bloodGroup = "B+", joiningDate = "১০-০৪-২০২৪", status = "active"
        )
    )

    val demoPositions = listOf(
        PositionRecord(id = "p1", organizationId = AppConstants.DEFAULT_ORG_ID, title = "সভাপতি", rankOrder = 1, holderName = "মো. রুহুল আমিন", description = "সার্বিক নেতৃত্ব ও মানবিক কার্যক্রম পরিচালনা"),
        PositionRecord(id = "p2", organizationId = AppConstants.DEFAULT_ORG_ID, title = "সাধারণ সম্পাদক", rankOrder = 2, holderName = "তানভীর আহমেদ", description = "প্রশাসনিক সমন্বয় ও সদস্য পরিচালনা"),
        PositionRecord(id = "p3", organizationId = AppConstants.DEFAULT_ORG_ID, title = "স্বেচ্ছাসেবক সমন্বয়ক", rankOrder = 3, holderName = "সাদিয়া ইসলাম", description = "মাঠ পর্যায়ের স্বেচ্ছাসেবী দল পরিচালনা")
    )

    val demoServices = listOf(
        ServiceCategory(id = "s1", organizationId = AppConstants.DEFAULT_ORG_ID, title = "রক্তদান সহায়তা", description = "জরুরি রোগীদের জন্য দ্রুত রক্তদাতা সমন্বয়", iconKey = "blood", active = true, sortOrder = 1),
        ServiceCategory(id = "s2", organizationId = AppConstants.DEFAULT_ORG_ID, title = "চিকিৎসা সহায়তা", description = "অসহায় রোগীদের চিকিৎসা ও ওষুধ সহায়তা", iconKey = "medical", active = true, sortOrder = 2),
        ServiceCategory(id = "s3", organizationId = AppConstants.DEFAULT_ORG_ID, title = "খাদ্য ও ত্রাণ বিতরণ", description = "দরিদ্র পরিবার ও দুর্যোগকবলিতদের খাদ্য সহায়তা", iconKey = "food", active = true, sortOrder = 3),
        ServiceCategory(id = "s4", organizationId = AppConstants.DEFAULT_ORG_ID, title = "শীতবস্ত্র বিতরণ", description = "শীতার্ত মানুষের মাঝে কম্বল ও শীতবস্ত্র বিতরণ", iconKey = "winter", active = true, sortOrder = 4)
    )

    val demoActivities = listOf(
        ActivityRecord(
            id = "a1", organizationId = AppConstants.DEFAULT_ORG_ID,
            title = "পল্টন এলাকায় স্বেচ্ছায় রক্তদান কর্মসূচি",
            description = "ক্লাব সদস্যদের অংশগ্রহণে জরুরি ব্লাড ডোনেশন ও বিনামূল্যে রক্তের গ্রুপ নির্ণয় ক্যাম্প।",
            category = "রক্তদান", date = "১৮-১১-২০২৬", location = "পল্টন ময়দান, ঢাকা",
            participants = 34, beneficiaries = 120, budget = 15000.0, status = "completed"
        ),
        ActivityRecord(
            id = "a2", organizationId = AppConstants.DEFAULT_ORG_ID,
            title = "শীতবস্ত্র ও জরুরি খাদ্য সহায়তা প্রস্তুতি",
            description = "আসন্ন শীত মৌসুমে অসহায় পরিবারের জন্য কম্বল ও খাদ্য সামগ্রী বিতরণ কার্যক্রম চলছে।",
            category = "শীতবস্ত্র বিতরণ", date = "১৯-১১-২০২৬", location = "পল্টন, ঢাকা",
            participants = 24, beneficiaries = 85, budget = 25000.0, status = "ongoing"
        )
    )

    val demoBloodRequests = listOf(
        BloodRequest(
            id = "br1", organizationId = AppConstants.DEFAULT_ORG_ID, createdByUid = "demo_1",
            patientName = "আবদুল করিম", bloodGroup = "O+", hospital = "ঢাকা মেডিকেল কলেজ হাসপাতাল",
            location = "শাহবাগ, ঢাকা", requiredDate = "১৯-১১-২০২৬", units = 2,
            contact = "লগইন করে যোগাযোগ করুন", urgency = "urgent", status = "open",
            notes = "অপারেশনের জন্য জরুরি ভিত্তিতে প্রয়োজন"
        )
    )

    val demoNotices = listOf(
        NoticeRecord(
            id = "n1", organizationId = AppConstants.DEFAULT_ORG_ID,
            title = "মাসিক মানবিক সমন্বয় সভা",
            body = "সকল সদস্য ও স্বেচ্ছাসেবকদের আগামী শুক্রবার বিকাল ৪টায় ক্লাব কার্যালয়ে উপস্থিত থাকার জন্য অনুরোধ করা হলো।",
            noticeType = "notice", pinned = true, createdByUid = "demo_1"
        )
    )

    val demoEvents = listOf(
        OrgEventRecord(
            id = "e1", organizationId = AppConstants.DEFAULT_ORG_ID,
            title = "শীতবস্ত্র বিতরণ ক্যাম্প",
            description = "পল্টন ও মতিঝিল এলাকায় অসহায় মানুষদের মাঝে কম্বল বিতরণ।",
            eventType = "event", eventDate = "২২-১১-২০২৬", eventTime = "বিকাল ৪:০০",
            location = "পল্টন ক্লাব প্রাঙ্গণ", createdByUid = "demo_1"
        )
    )

    val demoAllowances = listOf(
        AllowanceRecord(
            id = "alw_1", organizationId = AppConstants.DEFAULT_ORG_ID,
            memberName = "মো. রুহুল আমিন", areaOrDuty = "পল্টন ময়দান ফিল্ড টিম",
            date = "১৯-১১-২০২৬", dailyAmount = 500L, monthlyTotal = 10500L, paid = true
        ),
        AllowanceRecord(
            id = "alw_2", organizationId = AppConstants.DEFAULT_ORG_ID,
            memberName = "তানভীর আহমেদ", areaOrDuty = "মেডিকেল ও রক্তদান সহায়তা",
            date = "১৯-১১-২০২৬", dailyAmount = 500L, monthlyTotal = 9500L, paid = true
        ),
        AllowanceRecord(
            id = "alw_3", organizationId = AppConstants.DEFAULT_ORG_ID,
            memberName = "সাদিয়া ইসলাম", areaOrDuty = "স্বেচ্ছাসেবক সমন্বয়",
            date = "১৯-১১-২০২৬", dailyAmount = 500L, monthlyTotal = 9000L, paid = false
        ),
        AllowanceRecord(
            id = "alw_4", organizationId = AppConstants.DEFAULT_ORG_ID,
            memberName = "মো. কামরুল হাসান", areaOrDuty = "নিরাপত্তা ও ট্রাফিক সহায়তা",
            date = "১৯-১১-২০২৬", dailyAmount = 500L, monthlyTotal = 10000L, paid = true
        ),
        AllowanceRecord(
            id = "alw_5", organizationId = AppConstants.DEFAULT_ORG_ID,
            memberName = "মো. শফিকুল ইসলাম", areaOrDuty = "খাদ্য বিতরণ ইউনিট",
            date = "১৯-১১-২০২৬", dailyAmount = 500L, monthlyTotal = 8500L, paid = false
        ),
        AllowanceRecord(
            id = "alw_6", organizationId = AppConstants.DEFAULT_ORG_ID,
            memberName = "ফারহানা আক্তার", areaOrDuty = "শিক্ষা ও সমাজসেবা ডেস্ক",
            date = "১৯-১১-২০২৬", dailyAmount = 500L, monthlyTotal = 8000L, paid = true
        ),
        AllowanceRecord(
            id = "alw_7", organizationId = AppConstants.DEFAULT_ORG_ID,
            memberName = "নাজমুল হুদা", areaOrDuty = "রাত্রীকালীন শীতবস্ত্র টহল",
            date = "১৯-১১-২০২৬", dailyAmount = 500L, monthlyTotal = 9500L, paid = false
        ),
        AllowanceRecord(
            id = "alw_8", organizationId = AppConstants.DEFAULT_ORG_ID,
            memberName = "আরিফুল ইসলাম", areaOrDuty = "জরুরি ত্রাণ ও উদ্ধার দল",
            date = "১৯-১১-২০২৬", dailyAmount = 500L, monthlyTotal = 11000L, paid = true
        ),
        AllowanceRecord(
            id = "alw_9", organizationId = AppConstants.DEFAULT_ORG_ID,
            memberName = "মাহমুদুল হাসান", areaOrDuty = "পল্টন ক্লাব দপ্তর ডিউটি",
            date = "১৯-১১-২০২৬", dailyAmount = 500L, monthlyTotal = 9000L, paid = true
        ),
        AllowanceRecord(
            id = "alw_10", organizationId = AppConstants.DEFAULT_ORG_ID,
            memberName = "মো. জহিরুল হক", areaOrDuty = "ফিল্ড তদারকি ও লজিস্টিকস",
            date = "১৯-১১-২০২৬", dailyAmount = 500L, monthlyTotal = 10000L, paid = false
        )
    )
}
