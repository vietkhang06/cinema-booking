package com.example.cinemabooking.ui.customer.model;

import com.example.cinemabooking.domain.model.Cinema;
import com.example.cinemabooking.domain.model.Showtime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MovieDetailScheduleCatalog {

    private final LinkedHashMap<String, List<CinemaSection>> cinemasByCity = new LinkedHashMap<>();
    private final List<DateOption> dateOptions = new ArrayList<>();
    
    // Map dateKey -> (city -> list of CinemaSection)
    private final Map<String, LinkedHashMap<String, List<CinemaSection>>> dataByDateKey = new HashMap<>();

    public static MovieDetailScheduleCatalog createDefault() {
        // Return an empty catalog by default, to be populated dynamically
        return new MovieDetailScheduleCatalog();
    }

    public void buildFromShowtimes(List<Showtime> showtimes, Map<String, Cinema> cinemaMap) {
        cinemasByCity.clear();
        dateOptions.clear();
        dataByDateKey.clear();

        if (showtimes == null || showtimes.isEmpty()) {
            return;
        }

        SimpleDateFormat dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dateTextFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // 1. Group showtimes by dateKey
        Map<String, List<Showtime>> showtimesByDate = new HashMap<>();
        List<String> sortedDateKeys = new ArrayList<>();
        long now = System.currentTimeMillis();

        // Tính đầu ngày hôm nay (để không bỏ lỡ showtime hôm nay đã qua giờ chiếu)
        Calendar startOfTodayCal = Calendar.getInstance();
        startOfTodayCal.set(Calendar.HOUR_OF_DAY, 0);
        startOfTodayCal.set(Calendar.MINUTE, 0);
        startOfTodayCal.set(Calendar.SECOND, 0);
        startOfTodayCal.set(Calendar.MILLISECOND, 0);
        long startOfToday = startOfTodayCal.getTimeInMillis();

        for (Showtime s : showtimes) {
            // Chỉ bỏ lỡ showtime đã qua ngày (không phải qua giờ — để show tất cả suất hôm nay)
            if (s.deleted || s.startAt < startOfToday || !isBookableStatus(s.status)) {
                continue;
            }
            Date showDate = new Date(s.startAt);
            String dateKey = dateKeyFormat.format(showDate);

            if (!showtimesByDate.containsKey(dateKey)) {
                showtimesByDate.put(dateKey, new ArrayList<>());
                sortedDateKeys.add(dateKey);
            }
            showtimesByDate.get(dateKey).add(s);
        }

        // Sort date keys chronologically
        Collections.sort(sortedDateKeys);

        // 2. Build DateOptions
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        for (String dateKey : sortedDateKeys) {
            List<Showtime> dayShowtimes = showtimesByDate.get(dateKey);
            if (dayShowtimes == null || dayShowtimes.isEmpty()) {
                continue;
            }

            // Parse date
            try {
                Date dateObj = dateKeyFormat.parse(dateKey);
                if (dateObj == null) continue;

                Calendar dayCal = Calendar.getInstance();
                dayCal.setTime(dateObj);

                // Skip past days
                if (dayCal.before(today) && !isSameDay(dayCal, today)) {
                    continue;
                }

                String label = getDayLabel(dayCal);
                String dateText = dateTextFormat.format(dateObj);

                DateOption option = new DateOption(label, dateText, dateKey);
                dateOptions.add(option);

                // Build CinemaSections for this dateKey
                LinkedHashMap<String, List<CinemaSection>> citySections = new LinkedHashMap<>();

                // Group showtimes for this day by cinemaId
                Map<String, List<Showtime>> showtimesByCinema = new HashMap<>();
                for (Showtime s : dayShowtimes) {
                    if (!showtimesByCinema.containsKey(s.cinemaId)) {
                        showtimesByCinema.put(s.cinemaId, new ArrayList<>());
                    }
                    showtimesByCinema.get(s.cinemaId).add(s);
                }

                for (Map.Entry<String, List<Showtime>> entry : showtimesByCinema.entrySet()) {
                    String cinemaId = entry.getKey();
                    List<Showtime> cinemaShowtimes = entry.getValue();

                    Cinema cinema = cinemaMap.get(cinemaId);
                    if (cinema == null) {
                        continue;
                    }

                    String city = cinema.city != null ? cinema.city.trim() : "Khác";
                    String name = cinema.name != null ? cinema.name : "Rạp không tên";

                    // Group cinema showtimes by format and language (ShowtimeGroup)
                    Map<String, List<ShowtimeItem>> itemsByGroup = new HashMap<>();
                    for (Showtime s : cinemaShowtimes) {
                        String groupTitle = (s.format != null ? s.format : "2D") + " " +
                                (s.language != null ? s.language : "Phụ đề").toUpperCase();
                        
                        if (!itemsByGroup.containsKey(groupTitle)) {
                            itemsByGroup.put(groupTitle, new ArrayList<>());
                        }

                        String timeText = timeFormat.format(new Date(s.startAt));
                        itemsByGroup.get(groupTitle).add(new ShowtimeItem(s.showtimeId, timeText, s.startAt, s.basePrice));
                    }

                    List<ShowtimeGroup> groups = new ArrayList<>();
                    for (Map.Entry<String, List<ShowtimeItem>> groupEntry : itemsByGroup.entrySet()) {
                        // Sort times chronologically
                        Collections.sort(groupEntry.getValue(), (o1, o2) -> Long.compare(o1.startAt, o2.startAt));
                        groups.add(new ShowtimeGroup(groupEntry.getKey(), groupEntry.getValue()));
                    }

                    CinemaSection section = new CinemaSection(name, false, groups);
                    
                    if (!citySections.containsKey(city)) {
                        citySections.put(city, new ArrayList<>());
                    }
                    citySections.get(city).add(section);
                }

                dataByDateKey.put(dateKey, citySections);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void selectDateKey(String dateKey) {
        cinemasByCity.clear();
        LinkedHashMap<String, List<CinemaSection>> data = dataByDateKey.get(dateKey);
        if (data != null) {
            cinemasByCity.putAll(data);
        }
    }

    private String getDayLabel(Calendar targetCal) {
        Calendar today = Calendar.getInstance();
        if (isSameDay(targetCal, today)) {
            return "Hôm nay";
        }
        today.add(Calendar.DAY_OF_YEAR, 1);
        if (isSameDay(targetCal, today)) {
            return "Ngày mai";
        }
        return dayLabel(targetCal.get(Calendar.DAY_OF_WEEK));
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isBookableStatus(String status) {
        // null hoặc rỗng → coi là bookable (Admin có thể chưa set)
        if (status == null || status.trim().isEmpty()) {
            return true;
        }
        String s = status.trim().toLowerCase();
        return s.equals("active")
                || s.equals("available")
                || s.equals("open")
                || s.equals("scheduled")
                || s.equals("published");
    }

    private String dayLabel(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "Thứ 2";
            case Calendar.TUESDAY:
                return "Thứ 3";
            case Calendar.WEDNESDAY:
                return "Thứ 4";
            case Calendar.THURSDAY:
                return "Thứ 5";
            case Calendar.FRIDAY:
                return "Thứ 6";
            case Calendar.SATURDAY:
                return "Thứ 7";
            case Calendar.SUNDAY:
            default:
                return "CN";
        }
    }

    public List<String> getCityNames() {
        return new ArrayList<>(cinemasByCity.keySet());
    }

    public List<CinemaSection> getCinemas(String city) {
        List<CinemaSection> sections = cinemasByCity.get(city);
        if (sections == null) {
            return new ArrayList<>();
        }
        return sections;
    }

    public List<DateOption> getDateOptions() {
        return dateOptions;
    }

    public List<String> getCinemaNames(String city) {
        List<CinemaSection> sections = cinemasByCity.get(city);
        List<String> names = new ArrayList<>();
        if (sections != null) {
            for (CinemaSection section : sections) {
                names.add(section.name);
            }
        }
        return names;
    }

    public void setExpandedCinema(String city, String cinemaName) {
        List<CinemaSection> sections = cinemasByCity.get(city);
        if (sections == null) {
            return;
        }
        for (CinemaSection section : sections) {
            section.expanded = section.name.equals(cinemaName);
        }
    }

    public static class DateOption {
        public final String label;
        public final String dateText;
        public final String dateKey;

        public DateOption(String label, String dateText, String dateKey) {
            this.label = label;
            this.dateText = dateText;
            this.dateKey = dateKey;
        }
    }

    public static class CinemaSection {
        public final String name;
        public boolean expanded;
        public final List<ShowtimeGroup> groups;

        public CinemaSection(String name, boolean expanded, List<ShowtimeGroup> groups) {
            this.name = name;
            this.expanded = expanded;
            this.groups = groups;
        }
    }

    public static class ShowtimeGroup {
        public final String title;
        public final List<ShowtimeItem> showtimes;

        public ShowtimeGroup(String title, List<ShowtimeItem> showtimes) {
            this.title = title;
            this.showtimes = showtimes;
        }
    }

    public static class ShowtimeItem {
        public final String showtimeId;
        public final String timeText;
        public final long startAt;
        public final double basePrice;

        public ShowtimeItem(String showtimeId, String timeText, long startAt, double basePrice) {
            this.showtimeId = showtimeId;
            this.timeText = timeText;
            this.startAt = startAt;
            this.basePrice = basePrice;
        }
    }
}
