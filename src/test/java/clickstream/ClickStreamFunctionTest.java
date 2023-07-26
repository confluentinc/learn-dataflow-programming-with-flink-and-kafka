package clickstream;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.test.junit5.MiniClusterExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.ArrayList;
import java.util.List;

import static clickstream.TestHelpers.*;

import static org.junit.jupiter.api.Assertions.*;

class ClickStreamFunctionTest {
    private StreamExecutionEnvironment env;
    private ClickStreamFunction processFunction;
    private DataStream.Collector<ClickStreamAnalytics> collector;
    private WatermarkStrategy<ClickStreamRecord> defaultWatermarkStrategy;

    static final MiniClusterResourceConfiguration miniClusterConfig = new MiniClusterResourceConfiguration.Builder()
        .setNumberSlotsPerTaskManager(2)
        .setNumberTaskManagers(1)
        .build();

    @RegisterExtension
    static final MiniClusterExtension FLINK = new MiniClusterExtension(miniClusterConfig);

    private void assertContains(DataStream.Collector<ClickStreamAnalytics> collector, List<ClickStreamAnalytics> expectedList) {
        List<ClickStreamAnalytics> actualList = new ArrayList<>();
        collector.getOutput().forEachRemaining(actualList::add);

        assertEquals(expectedList.size(), actualList.size());

        for(int i = 0; i < actualList.size(); i++) {
            ClickStreamAnalytics actual = actualList.get(i);
            ClickStreamAnalytics expected = expectedList.get(i);

            assertEquals(expected.getRequest(), actual.getRequest());
            assertEquals(expected.getTotalCount(), actual.getTotalCount());
            assertEquals(expected.getSuccesses(), actual.getSuccesses());
            assertEquals(expected.getFailures(), actual.getFailures());
        }
    }

    private WindowedStream<ClickStreamRecord, String, TimeWindow> createWindowedStream(List<ClickStreamRecord> records) {
        DataStream<ClickStreamRecord> stream = env.fromCollection(records)
            .assignTimestampsAndWatermarks(defaultWatermarkStrategy);

        return stream
            .keyBy(ClickStreamRecord::getRequest)
            .window(TumblingEventTimeWindows.of(Time.milliseconds(10)));
    }

    @BeforeEach
    public void setup() {
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        processFunction = new ClickStreamFunction();
        collector = new DataStream.Collector<>();
        defaultWatermarkStrategy = WatermarkStrategy
            .<ClickStreamRecord>forMonotonousTimestamps()
            .withTimestampAssigner((event, timestamp) -> System.currentTimeMillis());
    }

    @Test
    public void process_shouldCountRecordsWithStatusCodesLessThan400AsSuccesses() throws Exception {
        String request = generateRequest();
        ClickStreamRecord record1 = generateClickStreamRecord(request, 0);
        ClickStreamRecord record2 = generateClickStreamRecord(request, 200);
        ClickStreamRecord record3 = generateClickStreamRecord(request, 399);

        createWindowedStream(List.of(record1, record2, record3))
            .process(processFunction)
            .collectAsync(collector);

        env.executeAsync();

        ClickStreamAnalytics expectedAnalytics = generateClickStreamAnalytics(
            request, 3, 3, 0
        );

        assertContains(collector, List.of(expectedAnalytics));
    }

    @Test
    public void process_shouldCountRecordsWithStatusCodesMoreThan400AsSuccesses() throws Exception {
        String request = generateRequest();
        ClickStreamRecord record1 = generateClickStreamRecord(request, 400);
        ClickStreamRecord record2 = generateClickStreamRecord(request, 500);
        ClickStreamRecord record3 = generateClickStreamRecord(request, 1000);

        createWindowedStream(List.of(record1, record2, record3))
            .process(processFunction)
            .collectAsync(collector);

        env.executeAsync();

        ClickStreamAnalytics expectedAnalytics = generateClickStreamAnalytics(
            request, 3, 0, 3
        );

        assertContains(collector, List.of(expectedAnalytics));
    }

    @Test
    public void process_shouldCountBothSuccessesAndFailures() throws Exception {
        String request = generateRequest();
        ClickStreamRecord record1 = generateClickStreamRecord(request, 200);
        ClickStreamRecord record2 = generateClickStreamRecord(request, 399);
        ClickStreamRecord record3 = generateClickStreamRecord(request, 400);
        ClickStreamRecord record4 = generateClickStreamRecord(request, 500);

        createWindowedStream(List.of(record1, record2, record3, record4))
            .process(processFunction)
            .collectAsync(collector);

        env.executeAsync();

        ClickStreamAnalytics expectedAnalytics = generateClickStreamAnalytics(
            request, 4, 2, 2
        );

        assertContains(collector, List.of(expectedAnalytics));
    }
}