package committee.nova.dateutils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Borrowed from https://github.com/EssentialsX/Essentials/blob/2.x/Essentials/src/main/java/com/earth2me/essentials/utils/DateUtil.java
 */
public class DateUtils {
    private static final Pattern timePattern = Pattern.compile(
            "(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" +
                    "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" +
                    "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" +
                    "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);

    private static final int yearLimit = 5000000;

    public static final String[] units = new String[]{"year", "month", "day", "hour", "minute", "second", "years", "months", "days", "hours", "minutes", "seconds"};

    public static String removeTimePattern(final String input) {
        return timePattern.matcher(input).replaceFirst("").trim();
    }

    public static long parseDateDiff(String time, boolean future) throws Exception {
        return parseDateDiff(time, future, false);
    }

    public static long parseDateDiff(String time, boolean future, boolean emptyEpoch) throws Exception {
        final Matcher m = timePattern.matcher(time);
        int years = 0;
        int months = 0;
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        boolean found = false;
        while (m.find()) {
            if (m.group() == null || m.group().isEmpty()) continue;
            for (int i = 0; i < m.groupCount(); i++) {
                if (m.group(i) != null && !m.group(i).isEmpty()) {
                    found = true;
                    break;
                }
            }
            if (found) {
                if (m.group(1) != null && !m.group(1).isEmpty()) years = Integer.parseInt(m.group(1));
                if (m.group(2) != null && !m.group(2).isEmpty()) months = Integer.parseInt(m.group(2));
                if (m.group(3) != null && !m.group(3).isEmpty()) weeks = Integer.parseInt(m.group(3));
                if (m.group(4) != null && !m.group(4).isEmpty()) days = Integer.parseInt(m.group(4));
                if (m.group(5) != null && !m.group(5).isEmpty()) hours = Integer.parseInt(m.group(5));
                if (m.group(6) != null && !m.group(6).isEmpty()) minutes = Integer.parseInt(m.group(6));
                if (m.group(7) != null && !m.group(7).isEmpty()) seconds = Integer.parseInt(m.group(7));
                break;
            }
        }
        if (!found) throw new IllegalArgumentException("illegalDate");
        final Calendar c = new GregorianCalendar();
        if (emptyEpoch) c.setTimeInMillis(0);
        if (years > 0) {
            if (years > yearLimit) years = yearLimit;
            c.add(Calendar.YEAR, years * (future ? 1 : -1));
        }
        if (months > 0) c.add(Calendar.MONTH, months * (future ? 1 : -1));
        if (weeks > 0) c.add(Calendar.WEEK_OF_YEAR, weeks * (future ? 1 : -1));
        if (days > 0) c.add(Calendar.DAY_OF_MONTH, days * (future ? 1 : -1));
        if (hours > 0) c.add(Calendar.HOUR_OF_DAY, hours * (future ? 1 : -1));
        if (minutes > 0) c.add(Calendar.MINUTE, minutes * (future ? 1 : -1));
        if (seconds > 0) c.add(Calendar.SECOND, seconds * (future ? 1 : -1));
        final Calendar max = new GregorianCalendar();
        max.add(Calendar.YEAR, 10);
        if (c.after(max)) return max.getTimeInMillis();
        return c.getTimeInMillis();
    }

    static int dateDiff(final int type, final Calendar fromDate, final Calendar toDate, final boolean future) {
        final int year = Calendar.YEAR;
        final int fromYear = fromDate.get(year);
        final int toYear = toDate.get(year);
        if (Math.abs(fromYear - toYear) > yearLimit) toDate.set(year, fromYear + (future ? yearLimit : -yearLimit));
        int diff = 0;
        long savedDate = fromDate.getTimeInMillis();
        while ((future && !fromDate.after(toDate)) || (!future && !fromDate.before(toDate))) {
            savedDate = fromDate.getTimeInMillis();
            fromDate.add(type, future ? 1 : -1);
            diff++;
        }
        diff--;
        fromDate.setTimeInMillis(savedDate);
        return diff;
    }

    public static String formatDateDiff(final long date) {
        final Calendar c = new GregorianCalendar();
        c.setTimeInMillis(date);
        final Calendar now = new GregorianCalendar();
        return formatDateDiff(now, c);
    }

    public static String formatDateDiff(final Calendar fromDate, final Calendar toDate) {
        boolean future = false;
        if (toDate.equals(fromDate)) return "now";
        if (toDate.after(fromDate)) future = true;
        // Temporary 50ms time buffer added to avoid display truncation due to code execution delays
        toDate.add(Calendar.MILLISECOND, future ? 50 : -50);
        final StringBuilder builder = new StringBuilder();
        final int[] types = new int[]{Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND};
        int accuracy = 0;
        for (int i = 0; i < types.length; i++) {
            if (accuracy > 2) break;
            final int diff = dateDiff(types[i], fromDate, toDate, future);
            if (diff > 0) {
                accuracy++;
                builder.append(diff).append(units[i + (diff > 1 ? 6 : 0)]);
            }
        }
        // Preserve correctness in the original date object by removing the extra buffer time
        toDate.add(Calendar.MILLISECOND, future ? -50 : 50);
        if (builder.length() == 0) return "now";
        return builder.toString().trim();
    }
}
