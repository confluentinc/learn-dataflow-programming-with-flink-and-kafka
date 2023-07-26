package clickstream;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.test.junit5.MiniClusterExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Array;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static clickstream.ClickStreamJob.defineWorkflow;
import static clickstream.TestHelpers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClickStreamJobTest {
    private StreamExecutionEnvironment env;
    private DataStream.Collector<ClickStreamAnalytics> collector;
    private WatermarkStrategy<ClickStreamRecord> defaultWatermarkStrategy;

    static final MiniClusterResourceConfiguration miniClusterConfig = new MiniClusterResourceConfiguration.Builder()
        .setNumberSlotsPerTaskManager(2)
        .setNumberTaskManagers(1)
        .build();

    @RegisterExtension
    static final MiniClusterExtension FLINK = new MiniClusterExtension(miniClusterConfig);

    private void assertContains(DataStream.Collector<ClickStreamAnalytics> collector, List<ClickStreamAnalytics> expectedList) {
        List<Tuple4<String, Long, Long, Long>> actualTuples = new ArrayList<>();
        collector.getOutput().forEachRemaining((r) ->
            actualTuples.add(new Tuple4<>(r.getRequest(), r.getTotalCount(), r.getSuccesses(), r.getFailures()))
        );

        List<Tuple4<String, Long, Long, Long>> expectedTuples = new ArrayList<>();
        expectedList.forEach((r) ->
            expectedTuples.add(new Tuple4<>(r.getRequest(), r.getTotalCount(), r.getSuccesses(), r.getFailures()))
        );

        assertEquals(expectedTuples.size(), actualTuples.size());
        assertTrue(actualTuples.containsAll(expectedTuples), "The results did not contain the expected values.");
    }

    private DataStream<ClickStreamAnalytics> createStream(List<ClickStreamRecord> records) {
        DataStream<ClickStreamRecord> stream = env.fromCollection(records)
            .assignTimestampsAndWatermarks(defaultWatermarkStrategy);

        return defineWorkflow(stream);
    }

    @BeforeEach
    public void setup() {
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        collector = new DataStream.Collector<>();
        defaultWatermarkStrategy = WatermarkStrategy
            .<ClickStreamRecord>forMonotonousTimestamps()
            .withTimestampAssigner((event, timestamp) -> System.currentTimeMillis());
    }

    @Test
    public void defineWorkflow_shouldDetermineAnalytics_forASingleRequestEnpoint() throws Exception {
        String request = generateRequest();
        ClickStreamRecord record1 = generateClickStreamRecord(request, 200);
        ClickStreamRecord record2 = generateClickStreamRecord(request, 399);
        ClickStreamRecord record3 = generateClickStreamRecord(request, 400);
        ClickStreamRecord record4 = generateClickStreamRecord(request, 500);

        createStream(List.of(record1, record2, record3, record4))
            .collectAsync(collector);

        env.executeAsync();

        ClickStreamAnalytics expectedAnalytics = generateClickStreamAnalytics(
            request, 4, 2, 2
        );

        assertContains(collector, List.of(expectedAnalytics));
    }

    @Test
    public void defineWorkflow_shouldDetermineAnalytics_forMultipleRequestEndpoints() throws Exception {
        String request1 = generateRequest();
        String request2 = generateRequest();
        ClickStreamRecord record1 = generateClickStreamRecord(request1, 200);
        ClickStreamRecord record2 = generateClickStreamRecord(request1, 500);
        ClickStreamRecord record3 = generateClickStreamRecord(request2, 400);
        ClickStreamRecord record4 = generateClickStreamRecord(request2, 500);

        createStream(List.of(record1, record2, record3, record4))
            .collectAsync(collector);

        env.executeAsync();

        ClickStreamAnalytics expectedAnalytics1 = generateClickStreamAnalytics(
            request1, 2, 1, 1
        );

        ClickStreamAnalytics expectedAnalytics2 = generateClickStreamAnalytics(
            request2, 2, 0, 2
        );

        assertContains(collector, List.of(expectedAnalytics1, expectedAnalytics2));
    }

    @Test
    public void defineWorkflow_shouldDivideTheStreamIntoOneMinuteWindows() throws Exception {
        String request = generateRequest();
        ClickStreamRecord record1 = generateClickStreamRecord(request, 200);
        ClickStreamRecord record2 = generateClickStreamRecord(request, 201);
        ClickStreamRecord record3 = generateClickStreamRecord(request, 399);
        ClickStreamRecord record4 = generateClickStreamRecord(request, 400);
        ClickStreamRecord record5 = generateClickStreamRecord(request, 500);

        defaultWatermarkStrategy = defaultWatermarkStrategy
            .withTimestampAssigner((record, timestamp) -> {
                if (record.getStatus() < 300) {
                    return System.currentTimeMillis();
                } else {
                    return System.currentTimeMillis() + Duration.ofMinutes(1).toMillis();
                }
            });

        createStream(List.of(record1, record2, record3, record4, record5))
            .collectAsync(collector);

        env.executeAsync();

        ClickStreamAnalytics expectedAnalyticsWindow1 = generateClickStreamAnalytics(
            request, 2, 2, 0
        );

        ClickStreamAnalytics expectedAnalyticsWindow2 = generateClickStreamAnalytics(
            request, 3, 1, 2
        );

        assertContains(collector, List.of(expectedAnalyticsWindow1, expectedAnalyticsWindow2));
    }
}