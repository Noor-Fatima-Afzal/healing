package com.example.utils

import android.content.Context

enum class ContentType { AYAH, HADITH, DUA }

data class DynamicIslamicContent(
    val type: ContentType,
    val arabicText: String,
    val urduTranslation: String,
    val englishTranslation: String,
    val surahName: String = "",
    val ayahNumber: String = "",
    val reference: String = "",
    val bookName: String = "",
    val source: String = ""
)

data class QuranVerse(
    val arabic: String,
    val urdu: String,
    val english: String,
    val surah: String,
    val ayah: String
)

data class HadithContent(
    val arabic: String,
    val urdu: String,
    val english: String,
    val reference: String
)

data class DuaContent(
    val arabic: String,
    val urdu: String,
    val english: String,
    val reference: String
)

object DailyContentProvider {
    val quranVerses = listOf(
        QuranVerse(
            arabic = "وَنُنَزِّلُ مِنَ الْقُرْآنِ مَا هُوَ شِفَاءٌ وَرَحْمَةٌ لِّلْمُؤْمِنِينَ",
            urdu = "اور ہم قرآن میں سے وہ چیز نازل کرتے ہیں جو ایمان والوں کے لیے شفا اور رحمت ہے۔",
            english = "And We send down of the Qur'an that which is healing and mercy for the believers.",
            surah = "Al-Isra",
            ayah = "82"
        ),
        QuranVerse(
            arabic = "وَإِذَا مَرِضْتُ فَهُوَ يَشْفِينِ",
            urdu = "اور جب میں بیمار ہوتا ہوں تو وہی مجھے شفا دیتا ہے۔",
            english = "And when I am ill, it is He who cures me.",
            surah = "Ash-Shu'ara",
            ayah = "80"
        ),
        QuranVerse(
            arabic = "لاَ يُكَلِّفُ اللَّهُ نَفْسًا إِلاَّ وُسْعَهَا",
            urdu = "اللہ کسی جان پر اس کی طاقت سے زیادہ بوجھ نہیں ڈالتا۔",
            english = "Allah does not burden a soul beyond that it can bear.",
            surah = "Al-Baqarah",
            ayah = "286"
        ),
        QuranVerse(
            arabic = "قُلْ هُوَ لِلَّذِينَ آمَنُوا هُدًى وَشِفَاءٌ",
            urdu = "کہہ دیجئے: یہ ان لوگوں کے لیے جو ایمان لائے ہیں، ہدایت اور شفا ہے۔",
            english = "Say, \"It is, for those who believe, a guidance and cure.\"",
            surah = "Fussilat",
            ayah = "44"
        ),
        QuranVerse(
            arabic = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            urdu = "سنو! اللہ کے ذکر سے ہی دلوں کو اطمینان نصیب ہوتا ہے۔",
            english = "Unquestionably, by the remembrance of Allah hearts are assured.",
            surah = "Ar-Ra'd",
            ayah = "28"
        )
    )

    val hadiths = listOf(
        HadithContent(
            arabic = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
            urdu = "تم میں سے بہترین شخص وہ ہے جو قرآن سیکھے اور اسے دوسروں کو سکھائے۔",
            english = "The best among you are those who learn the Qur'an and teach it.",
            reference = "Sahih al-Bukhari 5027"
        ),
        HadithContent(
            arabic = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ",
            urdu = "اعمال کا دارومدار نیتوں پر ہے۔",
            english = "Actions are but by intention.",
            reference = "Sahih al-Bukhari 1"
        ),
        HadithContent(
            arabic = "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ طَرِيقًا إِلَى الْجَنَّةِ",
            urdu = "جو علم کی تلاش میں کسی راستے پر چلے، اللہ اس کے لیے جنت کا راستہ آسان کر دیتا ہے۔",
            english = "Whoever takes a path upon which he seeks knowledge, Allah will make a path to Paradise easy for him.",
            reference = "Sahih Muslim 2699"
        ),
        HadithContent(
            arabic = "الدُّعَاءُ هُوَ الْعِبَادَةُ",
            urdu = "دعا ہی اصل عبادت ہے۔",
            english = "Supplication (Dua) is itself worship.",
            reference = "Sunan Abi Dawud 1479"
        )
    )

    val duas = listOf(
        DuaContent(
            arabic = "رَّبِّ زِدْنِي عِلْمًا",
            urdu = "اے میرے رب! میرے علم میں اضافہ فرما۔",
            english = "My Lord, increase me in knowledge.",
            reference = "Surah Ta-Ha, 114"
        ),
        DuaContent(
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا وَرِزْقًا طَيِّبًا وَعَمَلاً مُتَقَبَّلاً",
            urdu = "اے اللہ! میں تجھ سے نفع بخش علم، پاکیزہ رزق اور قبول ہونے والے عمل کا سوال کرتا ہوں۔",
            english = "O Allah, I ask You for beneficial knowledge, clean sustenance, and acceptable deeds.",
            reference = "Sunan Ibn Majah 925"
        ),
        DuaContent(
            arabic = "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ",
            urdu = "اے دلوں کو پھیرنے والے! میرے دل کو اپنے دین پر ثابت قدم رکھ۔",
            english = "O Controller of the hearts, make my heart steadfast in Your religion.",
            reference = "Jami` at-Tirmidhi 2140"
        ),
        DuaContent(
            arabic = "رَبَّنَا تَقَبَّلْ مِنَّا إِنَّكَ أَنتَ السَّمِيعُ الْعَلِيمُ",
            urdu = "اے ہمارے رب! ہم سے قبول فرما، بے شک تو ہی سب کچھ سننے والا، سب کچھ جاننے والا ہے۔",
            english = "Our Lord, accept [this] from us. Indeed You are the Hearing, the Knowing.",
            reference = "Surah Al-Baqarah, 127"
        )
    )

    val reminders = listOf(
        "Allah is the ultimate source of healing; turn your heart towards His words and find peace.",
        "A consistent student is more beloved to Allah than one who studies intensely but infrequently.",
        "Your attendance in a Tafseer class is an honor and selection by Allah. Cherish this blessing.",
        "Quranic knowledge without reflection is like a tree without fruit. Contemplate on every Ayah.",
        "Every difficulty you face in seeking sacred knowledge is written down as a source of high status."
    )

    fun getQuranVerseForDay(day: Int): QuranVerse {
        val index = (day - 1).coerceAtLeast(0) % quranVerses.size
        return quranVerses[index]
    }

    fun getHadithForDay(day: Int): HadithContent {
        val index = (day - 1).coerceAtLeast(0) % hadiths.size
        return hadiths[index]
    }

    fun getDuaForDay(day: Int): DuaContent {
        val index = (day - 1).coerceAtLeast(0) % duas.size
        return duas[index]
    }

    fun getReminderForDay(day: Int): String {
        val index = (day - 1).coerceAtLeast(0) % reminders.size
        return reminders[index]
    }

    val allDynamicContent = listOf(
        // Quran Ayahs
        DynamicIslamicContent(
            type = ContentType.AYAH,
            arabicText = "وَنُنَزِّلُ مِنَ الْقُرْآنِ مَا هُوَ شِفَاءٌ وَرَحْمَةٌ لِّلْمُؤْمِنِينَ",
            urduTranslation = "اور ہم قرآن میں سے وہ چیز نازل کرتے ہیں جو ایمان والوں کے لیے شفا اور رحمت ہے۔",
            englishTranslation = "And We send down of the Qur'an that which is healing and mercy for the believers.",
            surahName = "Al-Isra",
            ayahNumber = "82"
        ),
        DynamicIslamicContent(
            type = ContentType.AYAH,
            arabicText = "وَإِذَا مَرِضْتُ فَهُوَ يَشْفِينِ",
            urduTranslation = "اور جب میں بیمار ہوتا ہوں تو وہی مجھے شفا دیتا ہے۔",
            englishTranslation = "And when I am ill, it is He who cures me.",
            surahName = "Ash-Shu'ara",
            ayahNumber = "80"
        ),
        DynamicIslamicContent(
            type = ContentType.AYAH,
            arabicText = "لاَ يُكَلِّفُ اللَّهُ نَفْسًا إِلاَّ وُسْعَهَا",
            urduTranslation = "اللہ کسی جان پر اس کی طاقت سے زیادہ بوجھ نہیں ڈالتا۔",
            englishTranslation = "Allah does not burden a soul beyond that it can bear.",
            surahName = "Al-Baqarah",
            ayahNumber = "286"
        ),
        DynamicIslamicContent(
            type = ContentType.AYAH,
            arabicText = "قُلْ هُوَ لِلَّذِينَ آمَنُوا هُدًى وَشِفَاءٌ",
            urduTranslation = "کہہ دیجئے: یہ ان لوگوں کے لیے جو ایمان لائے ہیں، ہدایت اور شفا ہے۔",
            englishTranslation = "Say, \"It is, for those who believe, a guidance and cure.\"",
            surahName = "Fussilat",
            ayahNumber = "44"
        ),
        DynamicIslamicContent(
            type = ContentType.AYAH,
            arabicText = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            urduTranslation = "سنو! اللہ کے ذکر سے ہی دلوں کو اطمینان نصیب ہوتا ہے۔",
            englishTranslation = "Unquestionably, by the remembrance of Allah hearts are assured.",
            surahName = "Ar-Ra'd",
            ayahNumber = "28"
        ),
        // Hadiths
        DynamicIslamicContent(
            type = ContentType.HADITH,
            arabicText = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
            urduTranslation = "تم میں سے بہترین شخص وہ ہے جو قرآن سیکھے اور اسے دوسروں کو سکھائے۔",
            englishTranslation = "The best among you are those who learn the Qur'an and teach it.",
            reference = "5027",
            bookName = "Sahih al-Bukhari"
        ),
        DynamicIslamicContent(
            type = ContentType.HADITH,
            arabicText = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ",
            urduTranslation = "اعمال کا دارومدار نیتوں پر ہے۔",
            englishTranslation = "Actions are but by intention.",
            reference = "1",
            bookName = "Sahih al-Bukhari"
        ),
        DynamicIslamicContent(
            type = ContentType.HADITH,
            arabicText = "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ طَرِيقًا إِلَى الْجَنَّةِ",
            urduTranslation = "جو علم کی تلاش میں کسی راستے پر چلے، اللہ اس کے لیے جنت کا راستہ آسان کر دیتا ہے۔",
            englishTranslation = "Whoever takes a path upon which he seeks knowledge, Allah will make a path to Paradise easy for him.",
            reference = "2699",
            bookName = "Sahih Muslim"
        ),
        DynamicIslamicContent(
            type = ContentType.HADITH,
            arabicText = "الدُّعَاءُ هُوَ الْعِبَادَةُ",
            urduTranslation = "دعا ہی اصل عبادت ہے۔",
            englishTranslation = "Supplication (Dua) is itself worship.",
            reference = "1479",
            bookName = "Sunan Abi Dawud"
        ),
        // Duas
        DynamicIslamicContent(
            type = ContentType.DUA,
            arabicText = "رَّبِّ زِدْنِي عِلْمًا",
            urduTranslation = "اے میرے رب! میرے علم میں اضافہ فرما۔",
            englishTranslation = "My Lord, increase me in knowledge.",
            source = "Surah Ta-Ha, 114"
        ),
        DynamicIslamicContent(
            type = ContentType.DUA,
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا وَرِزْقًا طَيِّبًا وَعَمَلاً مُتَقَبَّلاً",
            urduTranslation = "اے اللہ! میں تجھ سے نفع بخش علم، پاکیزہ رزق اور قبول ہونے والے عمل کا سوال کرتا ہوں۔",
            englishTranslation = "O Allah, I ask You for beneficial knowledge, clean sustenance, and acceptable deeds.",
            source = "Sunan Ibn Majah 925"
        ),
        DynamicIslamicContent(
            type = ContentType.DUA,
            arabicText = "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ",
            urduTranslation = "اے دلوں کو پھیرنے والے! میرے دل کو اپنے دین پر ثابت قدم رکھ۔",
            englishTranslation = "O Controller of the hearts, make my heart steadfast in Your religion.",
            source = "Jami` at-Tirmidhi 2140"
        ),
        DynamicIslamicContent(
            type = ContentType.DUA,
            arabicText = "رَبَّنَا تَقَبَّلْ مِنَّا إِنَّكَ أَنتَ السَّمِيعُ الْعَلِيمُ",
            urduTranslation = "اے ہمارے رب! ہم سے قبول فرما، بے شک تو ہی سب کچھ سننے والا، سب کچھ جاننے والا ہے۔",
            englishTranslation = "Our Lord, accept [this] from us. Indeed You are the Hearing, the Knowing.",
            source = "Surah Al-Baqarah, 127"
        )
    )

    fun getRandomContent(context: Context): DynamicIslamicContent {
        val prefs = context.getSharedPreferences("islamic_content_prefs", Context.MODE_PRIVATE)
        val lastIndex = prefs.getInt("last_content_index", -1)
        
        var newIndex = lastIndex
        if (allDynamicContent.size > 1) {
            var retries = 0
            while (newIndex == lastIndex && retries < 10) {
                newIndex = (0 until allDynamicContent.size).random()
                retries++
            }
        } else {
            newIndex = 0
        }
        
        prefs.edit().putInt("last_content_index", newIndex).apply()
        return allDynamicContent[newIndex]
    }
}
