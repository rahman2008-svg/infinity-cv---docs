package com.example.data

data class TemplateField(
    val key: String,
    val labelEn: String,
    val labelBn: String,
    val defaultValue: String = "",
    val isMultiline: Boolean = false
)

data class DocumentTemplate(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val category: String, // "Identity", "Career", "Education", "Office", "Personal"
    val fields: List<TemplateField>,
    val isOfficialWatermarked: Boolean = false
)

object Templates {
    val list = listOf(
        // IDENTITY CATEGORY
        DocumentTemplate(
            id = "nid_front",
            titleEn = "NID Card (Front)",
            titleBn = "জাতীয় পরিচয়পত্র (সামনে)",
            category = "Identity",
            isOfficialWatermarked = true,
            fields = listOf(
                TemplateField("name_en", "Full Name (English)", "নাম (ইংরেজী)", "Abdur Rahman"),
                TemplateField("name_bn", "Full Name (Bangla)", "নাম (বাংলা)", "আবদুর রহমান"),
                TemplateField("father", "Father's Name", "পিতার নাম", "Abul Kalam"),
                TemplateField("mother", "Mother's Name", "মাতার নাম", "Amena Begum"),
                TemplateField("dob", "Date of Birth (DD-MM-YYYY)", "জন্ম তারিখ", "05-08-2005"),
                TemplateField("nid_no", "NID No (10 or 17 digit)", "আইডি নং", "5508210346")
            )
        ),
        DocumentTemplate(
            id = "nid_back",
            titleEn = "NID Card (Back)",
            titleBn = "জাতীয় পরিচয়পত্র (পেছনে)",
            category = "Identity",
            isOfficialWatermarked = true,
            fields = listOf(
                TemplateField("address", "Address", "ঠিকানা", "Gram: Chandpur, Dakghar: Chandpur, Upazila: Chandpur, Zila: Chandpur"),
                TemplateField("blood", "Blood Group", "রক্তের গ্রুপ", "O+"),
                TemplateField("place_of_birth", "Place of Birth", "জন্মস্থান", "Chandpur"),
                TemplateField("issue_date", "Issue Date (DD-MM-YYYY)", "প্রদানের তারিখ", "12-04-2024")
            )
        ),
        DocumentTemplate(
            id = "birth_cert",
            titleEn = "Birth Certificate",
            titleBn = "জন্ম নিবন্ধন সনদ",
            category = "Identity",
            isOfficialWatermarked = true,
            fields = listOf(
                TemplateField("reg_office", "Registrar Office", "নিবন্ধকের কার্যালয়", "Chandpur Pourashava, Chandpur"),
                TemplateField("reg_no", "Registration No", "নিবন্ধন নং", "20058210346123456"),
                TemplateField("name", "Name", "নাম", "Prince AR Abdur Rahman"),
                TemplateField("dob", "Date of Birth", "জন্ম তারিখ", "05-08-2005"),
                TemplateField("father_name", "Father's Name & Nationality", "পিতার নাম ও জাতীয়তা", "Abul Kalam - Bangladeshi"),
                TemplateField("mother_name", "Mother's Name & Nationality", "মাতার নাম ও জাতীয়তা", "Amena Begum - Bangladeshi")
            )
        ),
        DocumentTemplate(
            id = "passport_info",
            titleEn = "Passport Info Page",
            titleBn = "পাসপোর্ট তথ্য পাতা",
            category = "Identity",
            isOfficialWatermarked = true,
            fields = listOf(
                TemplateField("passport_no", "Passport No", "পাসপোর্ট নং", "EE0987654"),
                TemplateField("surname", "Surname", "বংশগত নাম", "Rahman"),
                TemplateField("given_names", "Given Name(s)", "প্রদত্ত নাম", "Prince AR Abdur"),
                TemplateField("nationality", "Nationality", "জাতীয়তা", "BANGALADESH"),
                TemplateField("dob", "Date of Birth", "জন্ম তারিখ", "05-08-2005"),
                TemplateField("place_of_issue", "Place of Issue", "প্রদানের স্থান", "DHAKA"),
                TemplateField("date_of_issue", "Date of Issue", "প্রদানের তারিখ", "21-02-2025"),
                TemplateField("date_of_expiry", "Date of Expiry", "মেয়াদোত্তীর্ণের তারিখ", "20-02-2035")
            )
        ),

        // CAREER CATEGORY
        DocumentTemplate(
            id = "cv_maker",
            titleEn = "Standard CV Maker",
            titleBn = "সিভি মেকার",
            category = "Career",
            fields = listOf(
                TemplateField("name", "Full Name", "পূর্ণ নাম", "Prince AR Abdur Rahman"),
                TemplateField("title", "Job Title / Professional Summary", "পেশাগত টাইটেল", "Software Engineer & Tech Lead"),
                TemplateField("phone", "Phone Number", "মোবাইল নম্বর", "+880 1782 634982"),
                TemplateField("email", "Email Address", "ইমেইল", "prince.ar.abdur.rahman200805@gmail.com"),
                TemplateField("address", "Address", "ঠিকানা", "Dhaka, Bangladesh"),
                TemplateField("objective", "Career Objective", "ক্যারিয়ার অবজেক্টিভ", "To leverage my technical and development skills in building highly robust and responsive custom applications.", true),
                TemplateField("skills", "Key Skills (comma separated)", "মূল দক্ষতা সমূহ", "Kotlin, Android, Compose, Jetpack, Firebase, Room, Git, UI/UX Design"),
                TemplateField("education", "Education Details", "শিক্ষাগত যোগ্যতা", "B.Sc in Computer Science & Engineering - Dhaka University (Ongoing)\nHSC - Chandpur Govt. College (GPA 5.00)", true),
                TemplateField("experience", "Work Experience", "কাজের অভিজ্ঞতা", "Junior Android Developer - TechCorp (2024 - Present)\nFreelance App Developer - Upwork & Fiverr (2022 - Present)", true)
            )
        ),
        DocumentTemplate(
            id = "resume_builder",
            titleEn = "Executive Resume",
            titleBn = "এক্সিকিউটিভ রেজুমে",
            category = "Career",
            fields = listOf(
                TemplateField("name", "Full Name", "পূর্ণ নাম", "Prince AR Abdur Rahman"),
                TemplateField("summary", "Executive Summary", "সংক্ষিপ্ত পরিচিতি", "Innovative and detail-oriented Android App Developer with 3+ years of experience crafting dynamic material design mobile interfaces.", true),
                TemplateField("contact", "Contact Info", "যোগাযোগের মাধ্যম", "Email: prince@example.com | Phone: 01782634982"),
                TemplateField("exp1", "Role 1 & Accomplishments", "ভূমিকা ১ ও অবদান", "Android Developer - AI Labs (2025 - Present):\nLed design of edge-to-edge UI layouts; integrated Room offline caching which improved app speed by 40%.", true),
                TemplateField("exp2", "Role 2 & Accomplishments", "ভূমিকা ২ ও অবদান", "Freelance Developer (2023 - 2025):\nDelivered 12+ production-ready Kotlin apps; achieved average 4.9 star rating.", true),
                TemplateField("education", "Education", "শিক্ষা", "Bachelor of CSE - Dhaka University", true)
            )
        ),
        DocumentTemplate(
            id = "biodata",
            titleEn = "Marriage Biodata",
            titleBn = "জীবনবৃত্তান্ত (পাত্র/পাত্রী)",
            category = "Career",
            fields = listOf(
                TemplateField("name", "Full Name", "পূর্ণ নাম", "Prince AR Abdur Rahman"),
                TemplateField("type", "Biodata Type", "জীবনবৃত্তান্তের ধরন", "পাত্রের জীবনবৃত্তান্ত (Groom's Biodata)"),
                TemplateField("dob", "Date of Birth", "জন্ম তারিখ", "05-08-2005"),
                TemplateField("height", "Height & Complexion", "উচ্চতা ও গায়ের রং", "5 feet 8 inches, Fair"),
                TemplateField("edu", "Educational Status", "শিক্ষাগত যোগ্যতা", "B.Sc in CSE (Ongoing)"),
                TemplateField("occupation", "Occupation", "পেশা", "Software Engineer"),
                TemplateField("family", "Family Background", "পারিবারিক বিবরণ", "Father is a businessman, mother is a homemaker. We have a respected family in Chandpur.", true),
                TemplateField("requirements", "Partner Expectations", "কেমন জীবনসঙ্গী প্রত্যাশা করেন", "A well-educated, religious, and understanding person.", true)
            )
        ),
        DocumentTemplate(
            id = "cover_letter",
            titleEn = "Professional Cover Letter",
            titleBn = "কভার লেটার",
            category = "Career",
            fields = listOf(
                TemplateField("date", "Date", "তারিখ", "28 June, 2026"),
                TemplateField("recipient", "Recipient Details", "প্রাপক", "The HR Manager\nInnoTech Solutions Ltd.\nDhaka, Bangladesh"),
                TemplateField("subject", "Subject", "বিষয়", "Application for the post of Senior Android Developer"),
                TemplateField("salutation", "Salutation", "সম্বোধন", "Dear Hiring Manager,"),
                TemplateField("body", "Body Paragraphs", "মূল বক্তব্য", "I am writing to express my strong interest in the Senior Android Developer position. With my robust experience in Jetpack Compose, state management, and Clean Architecture, I am confident I can bring immediate value to your development team.", true),
                TemplateField("sender", "Sender Name", "প্রেরকের নাম", "Prince AR Abdur Rahman")
            )
        ),

        // EDUCATION CATEGORY
        DocumentTemplate(
            id = "student_id",
            titleEn = "Student ID Card",
            titleBn = "ছাত্র আইডি কার্ড",
            category = "Education",
            fields = listOf(
                TemplateField("school", "School/University Name", "প্রতিষ্ঠান", "Dhaka University"),
                TemplateField("name", "Student Name", "ছাত্রের নাম", "Prince AR Abdur Rahman"),
                TemplateField("id_no", "Student ID / Roll", "আইডি নং / রোল", "DU-CSE-2025-08"),
                TemplateField("dept", "Department", "বিভাগ", "Computer Science & Engineering"),
                TemplateField("session", "Academic Session", "শিক্ষা বর্ষ", "2024-2025"),
                TemplateField("blood", "Blood Group", "রক্তের গ্রুপ", "O+")
            )
        ),
        DocumentTemplate(
            id = "char_cert",
            titleEn = "Character Certificate",
            titleBn = "চারিত্রিক প্রশংসাপত্র",
            category = "Education",
            fields = listOf(
                TemplateField("institute", "Institution", "শিক্ষা প্রতিষ্ঠান", "Chandpur Government College"),
                TemplateField("student_name", "Student Name", "শিক্ষার্থীর নাম", "Prince AR Abdur Rahman"),
                TemplateField("father_name", "Father's Name", "পিতার নাম", "Abul Kalam"),
                TemplateField("gpa", "GPA / Result", "জিপিএ / ফলাফল", "GPA 5.00"),
                TemplateField("passing_year", "Passing Year", "পাসের সন", "2023"),
                TemplateField("remarks", "Remarks on Character", "চারিত্রিক মন্তব্য", "To the best of my knowledge, he bears an excellent moral character and was never found involved in activities subversive of state discipline.")
            )
        ),

        // OFFICE CATEGORY
        DocumentTemplate(
            id = "invoice",
            titleEn = "Business Invoice",
            titleBn = "ইনভয়েস / বিল",
            category = "Office",
            fields = listOf(
                TemplateField("company", "Company Name", "কোম্পানির নাম", "Infinity Tech Solutions"),
                TemplateField("invoice_no", "Invoice No", "ইনভয়েস নং", "INV-2026-0042"),
                TemplateField("date", "Billing Date", "তারিখ", "28-06-2026"),
                TemplateField("client", "Bill To (Client)", "গ্রাহকের নাম", "Abdur Rahman Prince"),
                TemplateField("item1_desc", "Item 1 Description", "আইটেম ১ বিবরণ", "Mobile App Development Service"),
                TemplateField("item1_qty", "Item 1 Qty", "আইটেম ১ পরিমাণ", "1"),
                TemplateField("item1_price", "Item 1 Unit Price (৳)", "আইটেম ১ দর", "150000"),
                TemplateField("tax", "Tax/Vat (৳)", "ট্যাক্স / ভ্যাট", "7500"),
                TemplateField("discount", "Discount (৳)", "ডিসকাউন্ট", "10000")
            )
        ),
        DocumentTemplate(
            id = "money_receipt",
            titleEn = "Money Receipt",
            titleBn = "মানি রিসিট",
            category = "Office",
            fields = listOf(
                TemplateField("org", "Organization Name", "প্রতিষ্ঠানের নাম", "Creative Coding Academy"),
                TemplateField("receipt_no", "Receipt No", "রিসিট নং", "MR-10928"),
                TemplateField("date", "Receipt Date", "তারিখ", "28-06-2026"),
                TemplateField("received_from", "Received From", "যার কাছ থেকে পাওয়া গেল", "Prince AR Abdur Rahman"),
                TemplateField("amount_num", "Amount in Figures (৳)", "টাকার পরিমাণ (সংখ্যায়)", "25000"),
                TemplateField("amount_words", "Amount in Words", "টাকার পরিমাণ (কথায়)", "Twenty Five Thousand Taka Only"),
                TemplateField("purpose", "Received For / Purpose", "প্রদানের উদ্দেশ্য", "Premium Android Development Course Fee")
            )
        ),
        DocumentTemplate(
            id = "visiting_card",
            titleEn = "Visiting Card",
            titleBn = "ভিজিটিং কার্ড / বিজনেস কার্ড",
            category = "Office",
            fields = listOf(
                TemplateField("company", "Company / Brand Name", "কোম্পানির নাম", "Infinity Docs Corp"),
                TemplateField("name", "Name", "নাম", "Prince AR Abdur Rahman"),
                TemplateField("designation", "Designation", "পদবী", "Chief Technical Officer"),
                TemplateField("phone", "Mobile No", "মোবাইল", "01782634982"),
                TemplateField("email", "Email", "ইমেইল", "cto@infinitydocs.com"),
                TemplateField("website", "Website", "ওয়েবসাইট", "www.infinitydocs.com"),
                TemplateField("tagline", "Company Slogan", "কোম্পানির স্লোগান", "Digitizing Documents, Infinite Possibilities")
            )
        ),

        // PERSONAL CATEGORY
        DocumentTemplate(
            id = "wedding_card",
            titleEn = "Wedding Invitation",
            titleBn = "বিবাহের আমন্ত্রণপত্র",
            category = "Personal",
            fields = listOf(
                TemplateField("groom", "Groom Name", "বরের নাম", "Adnan Ahmed"),
                TemplateField("bride", "Bride Name", "কনের নাম", "Fariha Sultana"),
                TemplateField("date", "Wedding Date", "বিবাহের তারিখ", "18 December, 2026"),
                TemplateField("venue", "Venue Location", "অনুষ্ঠানের স্থান", "Senamalancha Hall, Dhaka Cantt."),
                TemplateField("time", "Time & Schedule", "সময়সূচী", "Recetpion: 7:00 PM onwards"),
                TemplateField("inviter", "Invited By", "আমন্ত্রণকারী", "Family of Mr. Kalam & Mr. Latif")
            )
        ),
        DocumentTemplate(
            id = "passport_photo",
            titleEn = "Passport Size Photo Maker",
            titleBn = "পাসপোর্ট সাইজ ফটো মেকার",
            category = "Personal",
            fields = listOf(
                TemplateField("bg_color", "Background Color (Blue/White/Light Grey)", "ব্যাকগ্রাউন্ডের রং", "Blue"),
                TemplateField("dimensions", "Photo Dimensions (e.g., 40x50 mm)", "ছবির সাইজ", "40x50 mm (Passport Size)"),
                TemplateField("border_width", "Border Width (dp)", "বর্ডারের সাইজ", "2"),
                TemplateField("title", "Photo Caption / Label (Optional)", "ছবির ক্যাপশন", "Prince AR")
            )
        )
    )
}
