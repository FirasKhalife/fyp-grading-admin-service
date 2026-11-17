package com.fypgrading.adminservice.schedule;

import com.fypgrading.adminservice.common.demo.SeedClock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Dates in notification and feed texts, in the demo's time zone ("Thu 11 Jun, 09:00"). */
public final class Dates {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

    private Dates() {}

    public static String day(Instant at) {
        return DAY.format(at.atZone(SeedClock.ZONE));
    }

    public static String time(Instant at) {
        return TIME.format(at.atZone(SeedClock.ZONE));
    }

    public static String dayAndTime(Instant at) {
        return day(at) + ", " + time(at);
    }
}
