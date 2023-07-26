package clickstream;

import java.util.Random;

public class TestHelpers {
    private static Random random = new Random(System.currentTimeMillis());

    public static String generateString(int size) {
        final String alphaString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        StringBuilder sb = new StringBuilder(size);

        for(int i = 0; i < size; i++) {
            final int index = random.nextInt(alphaString.length());
            sb.append(alphaString.charAt(index));
        }

        return sb.toString();
    }

    public static String generateRequest() {
        String path = "/"+generateString(5)+"/"+generateString(5);
        return "GET "+path+" HTTP/1.1";
    }

    public static int generateStatusCode() {
        return 200 + random.nextInt(400);
    }

    public static ClickStreamRecord generateClickStreamRecord() {
        ClickStreamRecord record = new ClickStreamRecord();
        record.setRequest(generateRequest());
        record.setStatus(generateStatusCode());
        return record;
    }

    public static ClickStreamRecord generateClickStreamRecord(String request, int status) {
        ClickStreamRecord record = new ClickStreamRecord();
        record.setRequest(request);
        record.setStatus(status);
        return record;
    }

    public static ClickStreamAnalytics generateClickStreamAnalytics(String request, int total, int successes, int failures) {
        ClickStreamAnalytics record = new ClickStreamAnalytics();
        record.setRequest(request);
        record.setTotalCount(total);
        record.setSuccesses(successes);
        record.setFailures(failures);
        return record;
    }
}
