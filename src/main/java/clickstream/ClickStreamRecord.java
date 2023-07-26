package clickstream;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClickStreamRecord {
    private String request;
    private int status;

    public ClickStreamRecord() {
        status = 0;
        request = "";
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClickStreamRecord that = (ClickStreamRecord) o;
        return status == that.status && Objects.equals(request, that.request);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, request);
    }

    @Override
    public String toString() {
        return "ClickStreamRecord{" +
            "status=" + status +
            ", request='" + request + '\'' +
            '}';
    }
}
