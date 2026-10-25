package tv.afnon.home;

import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.Madhab;

/** Interface strings for both locales, taken from the `t` tables in design/Home.dc.html. */
final class Text {
    final boolean ar;
    /** Eastern Arabic digits (٠١٢٣). Its own setting; defaults to on in Arabic. */
    final boolean eastern;

    Text(boolean ar, boolean eastern) {
        this.ar = ar;
        this.eastern = eastern;
    }

    String d(String s) {
        if (!eastern) return s;
        // "·" beside Eastern digits reads as a zero (٠); Arabic punctuation takes its own comma instead.
        s = s.replace(" · ", "، ");
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            b.append(c >= '0' && c <= '9' ? (char) ('٠' + (c - '0')) : c);
        }
        return b.toString();
    }

    /** The " · " between parts of a line; a comma where "·" would sit beside Eastern digits. */
    String sep() { return d(" · "); }

    /** The arch verse's reference (An-Nisa 4:103), in the screen's digits like every other number. */
    String ref() { return ar ? d("النساء 103") : d("An-Nisa 4:103"); }
    String nextPrayer() { return ar ? "الصلاة القادمة" : "NEXT PRAYER"; }
    String today() { return ar ? "مواقيت اليوم والقبلة" : "Today & Qibla"; }
    String settings() { return ar ? "الإعدادات" : "Settings"; }
    /** The saved city in this language; set by {@link Screen#loadText}. */
    String cityName = "";
    String city() { return cityName; }
    String fav() { return ar ? "المفضلة" : "FAVOURITES"; }
    String addFavourite() { return ar ? "إضافة إلى المفضلة" : "Add to favourites"; }
    String removeFavourite() { return ar ? "إزالة من المفضلة" : "Remove from favourites"; }
    String replaceFavourite() { return ar ? "استبدال تطبيق في المفضلة" : "Replace a favourite"; }
    String replaceWith(String name) { return (ar ? "استبدال بـ " : "REPLACE WITH ") + name; }
    String uninstall() { return ar ? "إلغاء التثبيت" : "Uninstall"; }
    String focusLighting() { return ar ? "إضاءة حسب التطبيق" : "App focus lighting"; }
    String focusLightingHelp() { return ar ? "تتغير إضاءة الخلفية قليلًا حسب التطبيق المحدد" : "Gently tint the background using the selected app's colours"; }
    String hideSystemApps() { return ar ? "إخفاء تطبيقات النظام من كل التطبيقات" : "Hide system apps in All apps"; }
    String hideSystemAppsHelp() { return ar ? "تبقى الوسائط والألعاب ومتجر Play ظاهرة" : "Media apps, games and Play Store stay visible"; }
    String recentApps() { return ar ? "التطبيقات الأخيرة" : "RECENT APPS"; }
    /** Home's tile: the label over the count. */
    String all(int n) { return d((ar ? "كل التطبيقات" : "All apps") + "\n" + n); }
    String ampm(boolean am) { return ar ? (am ? "ص" : "م") : (am ? "AM" : "PM"); }
    String adhanAt() { return ar ? "الأذان" : "Adhan at"; }
    String nowTag() { return ar ? "الآن" : "NOW"; }
    String nextTag() { return ar ? "التالية" : "NEXT"; }
    String jumuahLabel() { return ar ? "صلاة الجمعة" : "JUMU'AH"; }
    String jumuah() { return ar ? "الجمعة" : "Jumu'ah"; }
    String kahf() { return ar ? "سورة الكهف اليوم" : "Surat Al-Kahf today"; }
    String iftarIn() { return ar ? "الإفطار بعد" : "IFTAR IN"; }
    String iftar() { return ar ? "الإفطار" : "Iftar"; }
    String iftarTag() { return ar ? "الإفطار" : "IFTAR"; }
    String suhoorTag() { return ar ? "الإمساك" : "SUHOOR"; }
    String suhoorEnds(String time) { return d(ar ? "السحور حتى " + time : "Suhoor ends " + time); }
    String comingSoon() { return ar ? "هذه الشاشة قادمة في التحديث التالي" : "This screen comes in the next update"; }

    String open(String app) { return ar ? "فتح " + app : "Open " + app; }
    String network() { return ar ? "إعدادات الشبكة" : "Network settings"; }

    // Screensaver (design/Screensaver.dc.html)
    /**
     * "in 27 min". Arabic counts take their proper forms: دقيقة واحدة، دقيقتين، ٣–١٠ دقائق، ١١+ دقيقة. An hour
     * or more reads as h:mm, as on Prayer day.
     */
    String inMinutes(long minutes, String hoursAndMinutes) {
        if (minutes >= 60) return in(hoursAndMinutes);
        if (!ar) return d("in " + minutes + " min");
        if (minutes <= 1) return "بعد دقيقة واحدة";
        if (minutes == 2) return "بعد دقيقتين";
        return d("بعد " + minutes + (minutes <= 10 ? " دقائق" : " دقيقة"));
    }

    // Reminder and call to prayer (design/Home.dc.html approach/now, design/ApproachVideo.dc.html)
    String apLabel() { return ar ? "اقتربت الصلاة" : "PRAYER APPROACHING"; }
    /** "Asr in 13 minutes". */
    String apTitle(String prayer, long minutes, String hoursAndMinutes) {
        if (!ar && minutes < 60) return d(prayer + " in " + minutes + (minutes == 1 ? " minute" : " minutes"));
        return prayer + " " + inMinutes(minutes, hoursAndMinutes);
    }
    String apSub(CharSequence time) { return d((ar ? "الأذان " : "Adhan at ") + time); }
    String apOk() { return ar ? "حسنًا" : "Got it"; }
    String nowLabel() { return ar ? "حان الآن وقت صلاة" : "IT'S TIME FOR"; }
    String play() { return ar ? "تشغيل الأذان" : "Play adhan"; }
    String stop() { return ar ? "إيقاف الأذان" : "Stop adhan"; }
    String dismiss() { return ar ? "إغلاق" : "Dismiss"; }
    // Radio: Home's pill and card, the screen-off view, the screensaver line and the Settings section.
    String radio() { return ar ? "الإذاعة" : "Radio"; }
    String station(Radio.Station s) { return d(ar ? s.ar : s.en); }
    String stationWhere(Radio.Station s) { return d(ar ? s.whereAr : s.whereEn); }
    /** A station in a list: its name and place, since several are called إذاعة القرآن الكريم. */
    String stationChoice(Radio.Station s) { return s.reciter ? station(s) : station(s) + sep() + stationWhere(s); }
    String stationRow() { return ar ? "المحطة" : "Station"; }
    String[] stationGroups() {
        return ar ? new String[] {"إذاعات القرآن الكريم", "القرّاء"} : new String[] {"Quran radio stations", "Reciters"};
    }
    String radioStatus(int s) {
        switch (s) {
            case Radio.PLAYING: return ar ? "على الهواء" : "On air";
            case Radio.CONNECTING: return ar ? "جارٍ الاتصال…" : "Connecting…";
            case Radio.RETRYING: return ar ? "انقطع البث · نعيد المحاولة" : "Signal lost · retrying";
            case Radio.PAUSED: return ar ? "متوقفة للأذان" : "Paused for the adhan";
            default: return ar ? "متوقفة" : "Off";
        }
    }
    String radioPlay() { return ar ? "تشغيل" : "Play"; }
    String radioStop() { return ar ? "إيقاف" : "Stop"; }
    String screenOff() { return ar ? "إطفاء الشاشة" : "Screen off"; }
    String sectionRadio() { return ar ? "الإذاعة" : "Radio"; }
    String radioRowHelp(Radio.Station s) { return stationWhere(s) + (ar ? " · تحتاج إلى الإنترنت" : " · needs the internet"); }
    String afterAdhanRow() { return ar ? "بعد الأذان" : "After the adhan"; }
    String afterAdhanHelp() { return ar ? "تتوقف الإذاعة أثناء الأذان ثم…" : "The radio pauses for the adhan, then…"; }
    String[] afterAdhanChoices() { return ar ? new String[] {"تُستأنف", "تبقى متوقفة"} : new String[] {"Resumes", "Stays off"}; }
    String radioDarkRow() { return ar ? "إطفاء الشاشة أثناء الاستماع" : "Screen off while listening"; }
    String radioDarkHelp() {
        return d(ar ? "توفير الكهرباء: تنطفئ الشاشة وتستمر الإذاعة · أي زر يعيدها"
                    : "Saves power: the screen goes dark, the radio keeps playing · any key brings it back");
    }
    String[] radioDarkChoices() {
        return ar ? new String[] {"مع شاشة التوقف", "بعدها بـ 15 د", "أبدًا"} : new String[] {"With screensaver", "15 min later", "Never"};
    }

    String testPrayerAlerts() { return ar ? "تجربة التذكير والأذان" : "Test reminders & adhan"; }
    String testPrayerHelp() { return ar ? "معاينة الشريط أو شاشة الأذان بإعداداتك الحالية" : "Preview the banner or adhan screen with your current settings"; }
    String[] testPrayerChoices() { return ar ? new String[] {"شريط التذكير", "شاشة الأذان"} : new String[] {"Reminder banner", "Adhan popup"}; }
    String callCloseRow() { return ar ? "إغلاق شاشة الأذان" : "Close the call screen"; }
    String callCloseHelp() { return ar ? "ثم تعود إلى ما كنت تشاهده" : "Then back to what you were watching"; }
    /** In the order of the row's minutes: 0 (after the adhan), 1, 5, 10. */
    String[] callCloseChoices() {
        return ar ? new String[] {"بعد الأذان", "1 د", "5 د", "10 د"} : new String[] {"After adhan", "1 min", "5 min", "10 min"};
    }
    String voiceRow() { return ar ? "صوت الأذان" : "Adhan voice"; }
    String voiceHelp() { return ar ? "لكل الصلوات عدا الفجر · يُسمع عند التنقل في القائمة" : "Every prayer but Fajr · plays as you move through the list"; }
    String fajrVoiceRow() { return ar ? "أذان الفجر" : "Fajr adhan"; }
    String fajrVoiceHelp() { return ar ? "يُفضَّل أذان فيه «الصلاة خير من النوم»" : "Best with “prayer is better than sleep”"; }

    // All apps (design/AllApps.dc.html)
    String allTitle() { return ar ? "كل التطبيقات" : "All apps"; }
    String back() { return ar ? "رجوع" : "Back"; }
    /** "Asr in", followed by the countdown. */
    String chip(String prayer) { return ar ? prayer + " بعد" : prayer + " in"; }
    String favNote(int position) {
        String[] ord = {"الأول", "الثاني", "الثالث", "الرابع", "الخامس", "السادس", "السابع", "الثامن", "التاسع"};
        return ar ? "في المفضلة · " + ord[position - 1] : d("In favourites · position " + position);
    }
    String notFav() { return ar ? "اضغط مطولًا على OK لوضعه في المفضلة" : "Hold OK to put it in favourites"; }
    String moveEarlier() { return ar ? "تقديمه في المفضلة" : "Move earlier in favourites"; }
    String moveLater() { return ar ? "تأخيره في المفضلة" : "Move later in favourites"; }
    String putInPlaceOf(String app) { return ar ? "ضعه في المفضلة مكان " + app : "Put in favourites instead of " + app; }
    String cancel() { return ar ? "إلغاء" : "Cancel"; }
    String favourite() { return ar ? "مفضل" : "Favourite"; }

    // Prayer day (design/PrayerDay.dc.html)
    String dayTitle() { return ar ? "مواقيت اليوم" : "Today's prayers"; }
    String home() { return ar ? "الرئيسية" : "Home"; }
    String school(Madhab m) {
        if (m == Madhab.HANAFI) return ar ? "العصر: الحنفي" : "Asr: Hanafi";
        return ar ? "العصر: الجمهور (شافعي)" : "Asr: standard (Shafi‘i)";
    }

    String methodName(PrayerMethod m) {
        switch (m) {
            case UMM_AL_QURA: return ar ? "أم القرى، مكة المكرمة" : "Umm al-Qura, Makkah";
            case MUSLIM_WORLD_LEAGUE: return ar ? "رابطة العالم الإسلامي" : "Muslim World League";
            case KARACHI: return ar ? "جامعة العلوم الإسلامية، كراتشي" : "University of Islamic Sciences, Karachi";
            case DUBAI: return ar ? "دبي" : "Dubai";
            case KUWAIT: return ar ? "الكويت" : "Kuwait";
            case QATAR: return ar ? "قطر" : "Qatar";
            case NORTH_AMERICA: return ar ? "الجمعية الإسلامية لأمريكا الشمالية" : "ISNA, North America";
            case MOON_SIGHTING_COMMITTEE: return ar ? "لجنة رؤية الهلال" : "Moonsighting Committee";
            case SINGAPORE: return ar ? "سنغافورة" : "Singapore";
            case FRANCE: return ar ? "اتحاد المنظمات الإسلامية في فرنسا" : "France (UOIF)";
            case TURKEY: return ar ? "تركيا — ديانت (حساب تقريبي)" : "Turkey / Diyanet (approximate)";
            case RUSSIA: return ar ? "الإدارة الدينية لمسلمي روسيا" : "Spiritual Administration of Muslims of Russia";
            case JAKIM: return ar ? "ماليزيا — جاكيم" : "Malaysia (JAKIM)";
            case KEMENAG: return ar ? "وزارة الشؤون الدينية الإندونيسية" : "Indonesia (KEMENAG)";
            case TUNISIA: return ar ? "تونس" : "Tunisia";
            case ALGERIA: return ar ? "الجزائر" : "Algeria";
            case MOROCCO: return ar ? "المغرب" : "Morocco";
            case GULF: return ar ? "منطقة الخليج" : "Gulf region";
            default: return ar ? "الهيئة المصرية العامة للمساحة" : "Egyptian General Authority of Survey";
        }
    }

    /** "Fajr angle 19.5° · Isha 17.5°", or Isha as minutes after Maghrib where the method fixes it that way. */
    String methodAngles(PrayerMethod m) {
        CalculationParameters p = m.getParameters();
        String fajr = (ar ? "زاوية الفجر " : "Fajr angle ") + trim(p.fajrAngle) + "°";
        String isha = p.ishaInterval > 0
            ? (ar ? "العشاء بعد المغرب بـ " + p.ishaInterval + " دقيقة" : "Isha " + p.ishaInterval + " min after Maghrib")
            : (ar ? "العشاء " : "Isha ") + trim(p.ishaAngle) + "°";
        return d(fajr + " · " + isha);
    }

    private static String trim(double v) {
        return v == Math.rint(v) ? String.valueOf((int) v) : String.valueOf(v);
    }

    // Settings (design/Settings.dc.html)
    String sectionPrayer() { return ar ? "مواقيت الصلاة" : "Prayer times"; }
    String sectionReminders() { return ar ? "التنبيهات والأذان" : "Reminders & adhan"; }
    String sectionDisplay() { return ar ? "العرض واللغة" : "Display & language"; }
    String sectionNetwork(String ssid) { return ssid == null ? (ar ? "الشبكة" : "Network") : (ar ? "الشبكة · " : "Network · ") + ssid; }
    String sectionDevice() { return ar ? "إعدادات الجهاز" : "Device settings"; }
    String preview() { return ar ? "العصر اليوم بهذه الإعدادات" : "Asr today with these settings"; }
    String location() { return ar ? "الموقع" : "Location"; }
    String country() { return ar ? "الدولة" : "Country"; }
    String region() { return ar ? "المنطقة" : "Region"; }
    /** Location's first step, in the order of Prefs.REGIONS. */
    String[] regions() {
        return ar ? new String[] {"الدول العربية", "آسيا", "أوروبا", "الأمريكتان", "أفريقيا", "أوقيانوسيا"}
            : new String[] {"Arab world", "Asia", "Europe", "Americas", "Africa", "Oceania"};
    }
    String locationHelp(double lat, double lng) {
        String ns = lat >= 0 ? (ar ? "ش" : "N") : (ar ? "ج" : "S");
        String ew = lng >= 0 ? (ar ? "ق" : "E") : (ar ? "غ" : "W");
        return d(String.format(java.util.Locale.US, "%.2f° %s · %.2f° %s", Math.abs(lat), ns, Math.abs(lng), ew)
            + (ar ? " · يُختار من القائمة" : " · chosen from the list"));
    }
    String place(String city, String country) { return city + (ar ? "، " : ", ") + country; }
    String method() { return ar ? "طريقة الحساب" : "Calculation method"; }
    String asrSchool() { return ar ? "مذهب العصر" : "Asr school"; }
    String asrSchoolHelp() { return ar ? "يغيّر وقت العصر فقط" : "Only changes the Asr time"; }
    String[] asrSchools() { return ar ? new String[] {"الجمهور", "الحنفي"} : new String[] {"Standard", "Hanafi"}; }
    String hijriAdjust() { return ar ? "تعديل التاريخ الهجري" : "Hijri date adjustment"; }
    String hijriToday(String date) { return (ar ? "اليوم: " : "Today: ") + date; }
    String days(int n) {
        String sign = n > 0 ? "+" : "";
        if (!ar) return d(sign + n + (Math.abs(n) == 1 ? " day" : " days"));
        return d(sign + n + (Math.abs(n) == 2 ? " يومان" : " يوم"));
    }
    String roomRow() { return roomTitle(); }
    String roomHelp() { return ar ? "لمعرفة القبلة بالنسبة لمكان جلوسك" : "So the Qibla is shown from where you sit"; }
    String notSet() { return ar ? "لم يُحدَّد" : "Not set"; }
    String remind() { return ar ? "التنبيه قبل الصلاة" : "Remind me before prayer"; }
    String remindHelp() { return ar ? "على الرئيسية وفوق الفيديو" : "On Home and over video"; }
    String[] remindChoices() {
        return ar ? new String[] {"5 د", "10 د", "15 د", "20 د", "30 د", "إيقاف"}
                  : new String[] {"5 min", "10 min", "15 min", "20 min", "30 min", "Off"};
    }
    String remindVoiceRow() { return ar ? "تذكير صوتي" : "Spoken reminder"; }
    String remindVoiceHelp() { return ar ? "«اقترب موعد صلاة …» مع التذكير" : "Says «اقترب موعد صلاة …» with the reminder"; }
    String announceRow() { return ar ? "إعلان دخول الوقت" : "Announce the time"; }
    String announceHelp() {
        return ar ? "«حان الآن موعد صلاة …» للصلاة التي أذانها متوقف" : "Says «حان الآن موعد صلاة …» when a prayer's adhan is off";
    }
    String adhanRow() { return ar ? "الأذان" : "Adhan"; }
    String adhanHelp() { return ar ? "يُشغَّل عند دخول الوقت · يمكن إيقافه لكل صلاة" : "Plays when the time begins · can be muted per prayer"; }
    String language() { return ar ? "اللغة" : "Language"; }
    String languageHelp() { return ar ? "«التلفاز» يتبع لغة التلفاز" : "“TV” follows the TV's language"; }
    String[] languages() { return new String[] {ar ? "التلفاز" : "TV", "العربية", "English"}; }
    String digits() { return ar ? "الأرقام" : "Digits"; }
    String clockRow() { return ar ? "الساعة" : "Clock"; }
    String clockHelp() { return ar ? "«التلفاز» يتبع إعداد الوقت في التلفاز" : "“TV” follows the TV's time setting"; }
    String[] clocks() { return ar ? new String[] {"التلفاز", "12 س", "24 س"} : new String[] {"TV", "12 h", "24 h"}; }
    String networkRow() { return ar ? "إعدادات الشبكة في التلفاز" : "TV network settings"; }
    String networkHelp() { return ar ? "واي فاي وإيثرنت" : "Wi-Fi and Ethernet"; }
    String deviceRow() { return ar ? "إعدادات التلفاز" : "TV settings"; }
    String deviceHelp() { return ar ? "الصورة والصوت والتطبيقات والنظام" : "Picture, sound, apps and system"; }
    String saved() { return ar ? "✓  تم الحفظ" : "✓  Saved"; }
    String qibla() { return ar ? "القبلة" : "QIBLA"; }
    String north() { return ar ? "الشمال ▲" : "▲ North"; }
    String qiblaHint() { return ar ? "وجّه الشمال على البوصلة نحو الشمال الحقيقي" : "Line up the compass north with true north"; }
    String bellToggle() { return ar ? "تشغيل/إيقاف الأذان لهذه الصلاة" : "Adhan on/off for this prayer"; }
    String bell(boolean on) { return ar ? (on ? "الأذان مفعّل" : "الأذان متوقف") : (on ? "Adhan on" : "Adhan off"); }
    String rowNow() { return ar ? "الآن" : "now"; }
    /** Sunrise is masculine in Arabic (الشروق مضى); the prayers are feminine (الصلاة مضت). */
    String passed(boolean sunrise) { return ar ? (sunrise ? "مضى" : "مضت") : "passed"; }
    String in(String duration) { return d(ar ? "بعد " + duration : "in " + duration); }

    /** "South-east · from Cairo" for a bearing in degrees from true north. */
    String qiblaDirection(double bearing) {
        return octant(bearing) + (ar ? " · من " : " · from ") + city();
    }

    String octant(double bearing) {
        String[] names = ar ? new String[] {"شمال", "شمال شرق", "شرق", "جنوب شرق", "جنوب", "جنوب غرب", "غرب", "شمال غرب"}
                            : new String[] {"North", "North-east", "East", "South-east", "South", "South-west", "West", "North-west"};
        return names[sector(bearing)];
    }

    /**
     * Where the Qibla is for someone facing the TV, from its angle clockwise from straight ahead. Right and left
     * are the viewer's own, so they never mirror with the layout.
     */
    String qiblaRelative(double clockwise) {
        String[] where = ar
            ? new String[] {"أمامك", "أمامك إلى اليمين", "على يمينك", "خلفك إلى اليمين", "خلفك", "خلفك إلى اليسار", "على يسارك", "أمامك إلى اليسار"}
            : new String[] {"ahead of you", "ahead, to your right", "to your right", "behind, to your right", "behind you",
                            "behind, to your left", "to your left", "ahead, to your left"};
        return (ar ? "القبلة " : "Qibla is ") + where[sector(clockwise)];
    }

    /** "North-west · 320°". */
    String bearingLabel(int degrees) { return d(octant(degrees) + " · " + degrees + "°"); }

    String facingLine(int facing) { return (ar ? "تجلس مواجهًا: " : "You face: ") + bearingLabel(facing); }
    String tvMarker() { return ar ? "التلفاز ▲" : "▲ TV"; }
    String roomButton(boolean set) { return ar ? (set ? "تغيير اتجاه الغرفة" : "حدّد اتجاه الغرفة") : (set ? "Change room direction" : "Set room direction"); }
    String roomTitle() { return ar ? "اتجاه الغرفة" : "Room direction"; }
    String roomTip() {
        return ar ? "قف مواجهًا للتلفاز، وافتح تطبيق البوصلة في هاتفك وأمسكه مستويًا أمامك، ثم اضبط الرقم هنا على ما تقرؤه. يكفي ذلك مرة واحدة ما دام التلفاز في مكانه."
                  : "Stand facing the TV, open the compass app on your phone and hold it flat in front of you, then set the number here to what it reads. Once is enough while the TV stays where it is.";
    }
    String roomKeys() { return ar ? "◀ ▶  ٥°     ▲ ▼  ٤٥°     OK  حفظ     رجوع  إلغاء" : "◀ ▶  5°     ▲ ▼  45°     OK  Save     Back  Cancel"; }

    private static int sector(double degrees) {
        return (int) Math.round(((degrees % 360) + 360) % 360 / 45) % 8;
    }

    /** Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha. */
    String[] prayerNames() {
        return ar ? new String[] {"الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء"}
                  : new String[] {"Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha"};
    }

    String[] hijriMonths() {
        return ar ? new String[] {"محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
                                  "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"}
                  : new String[] {"Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Ula",
                                  "Jumada al-Akhirah", "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qadah",
                                  "Dhu al-Hijjah"};
    }

    String hijriSuffix() { return ar ? " هـ" : " AH"; }
    String themeRow() { return ar ? "السمة" : "Theme"; }
    String backgroundRow() { return ar ? "الخلفية" : "Background"; }
    String lightingRow() { return ar ? "الإضاءة المحيطية" : "Ambient light"; }
    String reduceMotion() { return ar ? "تقليل الحركة" : "Reduce motion"; }
    String screensaver() { return ar ? "شاشة التوقف" : "Screensaver"; }
    String saverSystem() { return ar ? "إعدادات شاشة التوقف والصور" : "Screensaver and photo settings"; }
    String homeSettings() { return ar ? "الشاشة الرئيسية الافتراضية" : "Default Home"; }
    String prayerDock() { return ar ? "شريط الصلاة" : "Prayer bar"; }
    String openDock() { return ar ? "فتح شريط الصلاة" : "Open prayer bar"; }
    String quickLabels() { return ar ? "أسماء أزرار الوصول السريع" : "Quick control labels"; }
    String dockPosition() { return ar ? "موضع شريط الصلاة" : "Prayer bar position"; }
    String[] dockPositions() { return ar ? new String[] {"أعلى", "أسفل"} : new String[] {"Top", "Bottom"}; }
    String collapseDock() { return ar ? "طي" : "Collapse"; }
    String dockDisabled() { return ar ? "شريط الصلاة مغلق في الإعدادات" : "Prayer bar is off in Settings"; }
    String[] dockModes() { return ar ? new String[] {"مخفي حتى فتحه", "تبويب صغير", "مغلق"} : new String[] {"Hidden until opened", "Small prayer tab", "Off"}; }
    String[] colourButtons() { return ar ? new String[] {"الأحمر", "الأخضر", "الأصفر", "الأزرق"} : new String[] {"Red", "Green", "Yellow", "Blue"}; }
    String liveTv() { return ar ? "التلفزيون المباشر" : "Live TV"; }
    String inputsTitle() { return ar ? "المداخل" : "Inputs"; }
    String inputsSystem() { return ar ? "قائمة مداخل التلفزيون" : "TV input menu"; }
    String prayerOffsets() { return ar ? "تعديل مواقيت الصلاة بالدقائق" : "Prayer time offsets"; }
    String prayerOffsetsHelp() { return ar ? "لكل صلاة: السالب أبكر والموجب لاحقًا · يشمل التذكير والأذان" : "Per prayer: minus is earlier, plus is later · includes reminders and adhan"; }
    String offsetMinutes(int n) { return d((n > 0 ? "+" : "") + n + (ar ? " د" : " min")); }
    String offsetCount(int n) { return d(n == 0 ? (ar ? "الكل 0 د" : "All 0 min") : n + (ar ? " معدّلة" : " adjusted")); }
    String remoteButtons() { return ar ? "تخصيص أزرار التحكم" : "Remote buttons"; }
    String pressButton() { return ar ? "اضغط زرًا لتخصيصه" : "Press a button to assign"; }
    String pressButtonHelp() { return ar ? "اضغط زر اختصار في جهاز التحكم ثم اختر التطبيق. أزرار التنقل والصوت والطاقة تبقى كما هي. إذا فتح الزر تطبيقًا آخر، ارجع واختر الزر من القائمة." : "Press a shortcut button on your remote, then choose an app. Navigation, volume and power keep their usual function. If the button opens another app, return and select it from the list."; }
    String buttonPreviewHelp() { return ar ? "على التلفزيون، اضغط زر جهاز التحكم هنا. هذه المعاينة تعرض أزرارًا وتطبيقات تجريبية فقط." : "On your TV, press the remote button here. This preview shows example buttons and apps only."; }
    String enableButtons() { return ar ? "تفعيل خدمة أزرار التحكم" : "Enable the remote button service"; }
    String buttonsFailed() { return ar ? "تعذّر حفظ الأزرار أو استعادتها. حاول مرة أخرى." : "Could not save or restore the buttons. Please try again."; }
    String buttonAppMissing() { return ar ? "هذا التطبيق لم يعد متاحًا. اختر تطبيقًا آخر." : "This app is no longer available. Choose another app."; }
    String buttonNumber(int code) { return (ar ? "زر " : "Button ") + d(String.valueOf(code)); }
    String[] buttonNames() { return ar ? new String[] {"اختصار الإعدادات", "الإعدادات", "التلفزيون", "المعلومات", "الويب"} : new String[] {"Settings shortcut", "Settings", "TV", "Info", "Web"}; }
    String restoreButtons() { return ar ? "استعادة أزرار التلفزيون الأصلية" : "Restore original TV buttons"; }
    String serviceHealth() { return ar ? "خدمة الأذان والتذكير" : "Prayer and reminder service"; }
    String serviceHelp() { return ar ? "إيقاف الخدمة يوقف تنبيهات الصلاة" : "Prayer alerts need this service enabled"; }
    String serviceRunning() { return ar ? "تعمل" : "Running"; }
    String serviceStopped() { return ar ? "متوقفة — فتح الإعدادات" : "Stopped — open settings"; }
    String repairService() { return ar ? "استعادة خدمة الصلاة تلقائيًا" : "Recover prayer service"; }
    String repairHelp() { return ar ? "عند العودة للرئيسية بعد إيقاف التطبيق" : "When Home opens after the app was stopped"; }
    String systemUnavailable() { return ar ? "هذه الصفحة غير متاحة على التلفزيون" : "This settings page is unavailable on this TV"; }
    String applyAppearance() { return ar ? "تطبيق التغييرات" : "Apply changes"; }
    String appearancePreview() { return ar ? "معاينة الرئيسية" : "Home preview"; }
    String sectionAppearance() { return ar ? "السمات والخلفيات" : "Themes & backgrounds"; }
    String[] themeNames() { return ar ? new String[] {"محراب", "شفق", "جمر", "غابة", "منتصف الليل", "وردي", "رمال", "أردوازي", "مخصصة"} : new String[] {"Mihrab", "Aurora", "Ember", "Forest", "Midnight", "Rose", "Sand", "Slate", "Custom"}; }
    String[] backgroundNames() { return ar ? new String[] {"حسب السمة", "زخرفة", "سادة", "بانوراما", "ضوء النهار", "جبال", "كثبان", "هالة", "سماء الليل"} : new String[] {"Theme", "Lattice", "Solid", "Panorama", "Daylight", "Mountains", "Dunes", "Halo", "Night sky"}; }
    String[] buttonActions() { return ar ? new String[] {"الأصلي", "الرئيسية", "التطبيقات", "المداخل", "الإعدادات", "شريط الصلاة"} : new String[] {"Original", "Home", "All apps", "Inputs", "Settings", "Prayer bar"}; }
}
