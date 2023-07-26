package clickstream;

import java.util.Objects;

public class ClickStreamAnalytics {
    private String request;
    private long totalCount;
    private long successes;
    private long failures;
    private long timestamp;

    public ClickStreamAnalytics() {
        request = "";
        totalCount = 0;
        successes = 0;
        failures = 0;
        timestamp = 0;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getSuccesses() {
        return successes;
    }

    public void setSuccesses(long successes) {
        this.successes = successes;
    }

    public long getFailures() {
        return failures;
    }

    public void setFailures(long failures) {
        this.failures = failures;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClickStreamAnalytics that = (ClickStreamAnalytics) o;
        return totalCount == that.totalCount && successes == that.successes && failures == that.failures && Objects.equals(request, that.request) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(request, totalCount, successes, failures, timestamp);
    }

    @Override
    public String toString() {
        return "ClickStreamAnalytics{" +
            "request='" + request + '\'' +
            ", totalCount=" + totalCount +
            ", successes=" + successes +
            ", failures=" + failures +
            ", timestamp=" + timestamp +
            '}';
    }
}
