package clickstream;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.io.InputStream;
import java.util.Properties;

public class ClickStreamJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties consumerConfig = new Properties();
        try (InputStream stream = ClickStreamJob.class
            .getClassLoader()
            .getResourceAsStream("consumer.properties")
        ) {
            consumerConfig.load(stream);
        }

        Properties producerConfig = new Properties();
        try (InputStream stream = ClickStreamJob.class
            .getClassLoader()
            .getResourceAsStream("producer.properties")
        ) {
            producerConfig.load(stream);
        }

        KafkaSource<ClickStreamRecord> source = KafkaSource.<ClickStreamRecord>builder()
            .setProperties(consumerConfig)
            .setTopics("clickstream")
            .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new JsonDeserializationSchema<>(ClickStreamRecord.class))
            .build();

        WatermarkStrategy<ClickStreamRecord> watermarkStrategy = WatermarkStrategy
            .<ClickStreamRecord>forMonotonousTimestamps()
            .withTimestampAssigner((record, timestamp) -> timestamp);

        DataStream<ClickStreamRecord> stream = env
            .fromSource(
                source,
                watermarkStrategy,
                "clickstream-source"
            );

        KafkaRecordSerializationSchema<ClickStreamAnalytics> serializer =
            KafkaRecordSerializationSchema.<ClickStreamAnalytics>builder()
                .setTopic("clickstream-analytics")
                .setValueSerializationSchema(new JsonSerializationSchema<>())
                .build();

        KafkaSink<ClickStreamAnalytics> sink =
            KafkaSink.<ClickStreamAnalytics>builder()
                .setKafkaProducerConfig(producerConfig)
                .setRecordSerializer(serializer)
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        defineWorkflow(stream).sinkTo(sink);

        env.execute("clickstream-job");
    }

    public static DataStream<ClickStreamAnalytics> defineWorkflow(DataStream<ClickStreamRecord> stream) {
        return stream
            .keyBy(ClickStreamRecord::getRequest)
            .window(TumblingEventTimeWindows.of(Time.minutes(1)))
            .process(new ClickStreamProcessFunction());
    }
}
