package org.schemaspy.util;

public class DurationFormatter {

    private static final long MS_TO_SEC = 1000;
    private static final long MS_TO_MIN = MS_TO_SEC * 60;
    private static final long MS_TO_HR = MS_TO_MIN * 60;

    public static String formatMS(final long durationInMilliseconds) {
        long timeToProcess = durationInMilliseconds;
        StringBuilder stringBuilder = new StringBuilder();
        if (timeToProcess >= MS_TO_HR) {
            stringBuilder.append(timeToProcess/MS_TO_HR).append( " hr ");
            timeToProcess = timeToProcess % MS_TO_HR;
        }
        if (timeToProcess >= MS_TO_MIN) {
            stringBuilder.append(timeToProcess/MS_TO_MIN).append(" min ");
            timeToProcess = timeToProcess % MS_TO_MIN;
        }
        if (timeToProcess >= MS_TO_SEC) {
            stringBuilder.append(timeToProcess/MS_TO_SEC).append(" s ");
            timeToProcess = timeToProcess % MS_TO_SEC;
        }
        if (timeToProcess >  0) {
            stringBuilder.append(timeToProcess).append(" ms");
        }
        return stringBuilder.toString().trim();
    }
}
