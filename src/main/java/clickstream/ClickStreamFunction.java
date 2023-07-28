package clickstream;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class ClickStreamFunction extends ProcessWindowFunction<ClickStreamRecord, ClickStreamAnalytics, String, TimeWindow> {
    @Override
    public void process(
        String request,
        ProcessWindowFunction<ClickStreamRecord, ClickStreamAnalytics, String, TimeWindow>.Context context,
        Iterable<ClickStreamRecord> iterable,
        Collector<ClickStreamAnalytics> collector
    ) throws Exception {
        long total = 0;
        long successes = 0;
        long failures = 0;

        for(ClickStreamRecord record : iterable) {
            if(record.getStatus() >= 400)
                failures++;
            else
                successes++;

            total++;
        }

        ClickStreamAnalytics analytics = new ClickStreamAnalytics();
        analytics.setRequest(request);
        analytics.setTotalCount(total);
        analytics.setSuccesses(successes);
        analytics.setFailures(failures);
        analytics.setTimestamp(context.currentProcessingTime());
        collector.collect(analytics);
    }
}
