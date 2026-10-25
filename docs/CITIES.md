# Cities and calculation defaults

The current catalog contains **137 cities in 43 countries**, grouped into six regions. Choose a
location in **Settings → Prayer times → Location**. There is no automatic location discovery.

The source is [`Prefs.CITIES`](../app/src/main/java/tv/afnon/home/Prefs.java). The browser preview
extracts this catalog during its build, and the phone installer queries it from the TV. The browser
introduction derives its city/country totals from the extracted data. This table is a reference
snapshot; update it and the README totals whenever the catalog changes.

## Leamington Spa

Leamington Spa is included alongside London, Birmingham and Manchester in the United Kingdom.
Its catalog coordinates are 52.2913, −1.5364, with Muslim World League and Standard/Shafi'i Asr as
the initial defaults. These are editable calculation defaults, **not a verified local mosque timetable**.
Changing city resets the calculation method and Asr school to that city's defaults; saved per-prayer
offsets remain. Check the method, Asr school, timezone and offsets after changing location.

The [city contribution rules](../CONTRIBUTING.md#1-add-your-city) require a six-time comparison for
one date against an official authority or local mosque timetable. That comparison for this addition
is still pending; do not describe its default as locally verified in an upstream PR. A fixed offset
cannot reproduce seasonal timetable rules. [Calculation methods](PRAYER-METHODS.md) explains the
19 presets and why London Unified is not offered as a generic city-independent method.

## Included locations

| Country / الدولة | Cities / المدن |
|---|---|
| Egypt / مصر | Cairo / القاهرة; Giza / الجيزة; Alexandria / الإسكندرية; Mansoura / المنصورة; Tanta / طنطا; Port Said / بورسعيد; Suez / السويس; Asyut / أسيوط; Luxor / الأقصر; Aswan / أسوان |
| Saudi Arabia / السعودية | Makkah / مكة المكرمة; Madinah / المدينة المنورة; Riyadh / الرياض; Jeddah / جدة; Dammam / الدمام; Taif / الطائف; Tabuk / تبوك; Abha / أبها |
| UAE / الإمارات | Dubai / دبي; Abu Dhabi / أبو ظبي; Sharjah / الشارقة; Al Ain / العين |
| Kuwait / الكويت | Kuwait City / الكويت |
| Qatar / قطر | Doha / الدوحة |
| Jordan / الأردن | Amman / عمّان; Irbid / إربد; Zarqa / الزرقاء; Aqaba / العقبة |
| Palestine / فلسطين | Jerusalem / القدس; Ramallah / رام الله; Nablus / نابلس; Hebron / الخليل; Bethlehem / بيت لحم; Jenin / جنين; Tulkarm / طولكرم; Qalqilya / قلقيلية; Salfit / سلفيت; Tubas / طوباس; Jericho / أريحا; Gaza / غزة; Jabalia / جباليا; Deir al-Balah / دير البلح; Khan Yunis / خان يونس; Rafah / رفح |
| Lebanon / لبنان | Beirut / بيروت; Tripoli / طرابلس; Sidon / صيدا; Tyre / صور; Baalbek / بعلبك |
| Syria / سوريا | Damascus / دمشق; Aleppo / حلب; Homs / حمص; Hama / حماة; Latakia / اللاذقية; Deir ez-Zor / دير الزور |
| Iraq / العراق | Baghdad / بغداد; Basra / البصرة; Mosul / الموصل; Erbil / أربيل; Najaf / النجف; Karbala / كربلاء; Kirkuk / كركوك; Sulaymaniyah / السليمانية |
| Bahrain / البحرين | Manama / المنامة; Muharraq / المحرق |
| Oman / عُمان | Muscat / مسقط; Salalah / صلالة; Sohar / صحار; Nizwa / نزوى |
| Yemen / اليمن | Sanaa / صنعاء; Aden / عدن; Taiz / تعز; Hodeidah / الحديدة; Mukalla / المكلا |
| United States / الولايات المتحدة | New York / نيويورك; Chicago / شيكاغو; Dallas / دالاس; Houston / هيوستن; Los Angeles / لوس أنجلوس |
| Canada / كندا | Toronto / تورونتو |
| India / الهند | Bengaluru / بنغالور; Kozhikode / كوزيكود; Mumbai / مومباي; Delhi / دلهي; Hyderabad / حيدر أباد; Chennai / تشيناي; Kolkata / كولكاتا; Ahmedabad / أحمد أباد; Pune / بونه; Lucknow / لكهنؤ; Srinagar / سرينغار; Kochi / كوتشي; Malappuram / ملابورام; Bhopal / بوبال; Patna / باتنا |
| Pakistan / باكستان | Karachi / كراتشي; Lahore / لاهور; Islamabad / إسلام أباد; Rawalpindi / راولبندي; Peshawar / بيشاور |
| Bangladesh / بنغلاديش | Dhaka / دكا; Chittagong / شيتاغونغ |
| Sri Lanka / سريلانكا | Colombo / كولمبو |
| Maldives / المالديف | Malé / ماليه |
| Indonesia / إندونيسيا | Jakarta / جاكرتا; Surabaya / سورابايا; Bandung / باندونغ; Medan / ميدان |
| Malaysia / ماليزيا | Kuala Lumpur / كوالالمبور; Penang / بينانغ |
| Singapore / سنغافورة | Singapore / سنغافورة |
| Brunei / بروناي | Bandar Seri Begawan / بندر سري بكاوان |
| Thailand / تايلاند | Bangkok / بانكوك |
| Japan / اليابان | Tokyo / طوكيو |
| South Korea / كوريا الجنوبية | Seoul / سيول |
| Turkey / تركيا | Istanbul / إسطنبول; Ankara / أنقرة |
| United Kingdom / المملكة المتحدة | Leamington Spa / ليمينغتون سبا; London / لندن; Birmingham / برمنغهام; Manchester / مانشستر |
| France / فرنسا | Paris / باريس |
| Germany / ألمانيا | Berlin / برلين; Frankfurt / فرانكفورت |
| Netherlands / هولندا | Amsterdam / أمستردام |
| Belgium / بلجيكا | Brussels / بروكسل |
| Austria / النمسا | Vienna / فيينا |
| Switzerland / سويسرا | Zurich / زيورخ |
| Italy / إيطاليا | Rome / روما |
| Spain / إسبانيا | Madrid / مدريد |
| Bosnia and Herzegovina / البوسنة والهرسك | Sarajevo / سراييفو |
| Kenya / كينيا | Nairobi / نيروبي |
| Nigeria / نيجيريا | Lagos / لاغوس |
| South Africa / جنوب أفريقيا | Johannesburg / جوهانسبرغ |
| Australia / أستراليا | Sydney / سيدني; Melbourne / ملبورن |
| New Zealand / نيوزيلندا | Auckland / أوكلاند |
