package org.radarcns.stream.statistics;

import static org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG;
import static org.radarcns.monitor.AbstractKafkaMonitor.extractKey;

import io.confluent.kafka.streams.serdes.avro.GenericAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.Cancellable;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.radarcns.config.SourceStatisticsStreamConfig;
import org.radarcns.kafka.ObservationKey;
import org.radarcns.stream.AbstractStreamWorker;
import org.radarcns.stream.SourceStatistics;
import org.radarcns.stream.StreamDefinition;
import org.radarcns.util.serde.RadarSerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceStatisticsStream extends AbstractStreamWorker {
    private static final Logger logger = LoggerFactory.getLogger(SourceStatisticsStream.class);
    private String name;
    private Duration interval;

    @Override
    protected List<KafkaStreams> createStreams() {
        return Collections.singletonList(new KafkaStreams(getTopology(), getStreamsConfig()));
    }

    @Override
    protected void doCleanup() {
        // do nothing
    }

    @Override
    protected void initialize() {
        SourceStatisticsStreamConfig config = (SourceStatisticsStreamConfig) this.config;

        this.name = config.getName();

        List<String> inputTopics = config.getTopics();
        if (inputTopics == null || inputTopics.isEmpty()) {
            throw new IllegalArgumentException("Input topics for stream " + name + " is empty");
        }
        if (config.getOutputTopic() == null) {
            throw new IllegalArgumentException("Output topic for stream " + name + " is missing");
        }
        inputTopics.forEach(t -> defineStream(t, config.getOutputTopic()));

        this.interval = Duration.ofMillis(config.getFlushTimeout());
    }

    protected Duration getInterval() {
        return interval;
    }

    @SuppressWarnings("PMD.OptimizableToArrayCall")
    private Topology getTopology() {
        Topology builder = new Topology();
        GenericAvroDeserializer genericReader = new GenericAvroDeserializer();

        StoreBuilder<KeyValueStore<ObservationKey, SourceStatisticsRecord>> statisticsStore = Stores
                .keyValueStoreBuilder(
                        Stores.persistentKeyValueStore("statistics"),
                        new SpecificAvroSerde<>(),
                        new RadarSerde<>(SourceStatisticsRecord.class).getSerde());

        builder.addSource("source", genericReader, genericReader, getStreamDefinitions()
                .map(StreamDefinition::getInputTopic)
                .toArray(String[]::new));
        builder.addProcessor("process", SourceStatisticsProcessor::new, "source");
        builder.addSink("sink", getStreamDefinitions()
                        .map(StreamDefinition::getOutputTopic)
                        .filter(Objects::nonNull)
                        .findAny().orElseThrow(() ->
                                new IllegalStateException("Output topic for SourceStatisticsStream "
                                        + name + " is undefined.")).getName(),
                new SpecificAvroSerializer<ObservationKey>(),
                new SpecificAvroSerializer<SourceStatistics>(),
                "process");

        builder.addStateStore(statisticsStore, "process");
        return builder;
    }

    private Properties getStreamsConfig() {
        Properties settings = kafkaProperty.getStreamProperties(name, config);
        settings.remove(DEFAULT_KEY_SERDE_CLASS_CONFIG);
        settings.remove(DEFAULT_VALUE_SERDE_CLASS_CONFIG);
        return settings;
    }

    private class SourceStatisticsProcessor implements Processor<GenericRecord, GenericRecord> {
        private KeyValueStore<ObservationKey, SourceStatisticsRecord> store;
        private ProcessorContext context;
        private Cancellable punctuateCancellor;
        private Duration localInterval = Duration.ZERO;

        @SuppressWarnings("unchecked")
        @Override
        public void init(ProcessorContext context) {
            store = (KeyValueStore<ObservationKey, SourceStatisticsRecord>) context.getStateStore("statistics");
            this.context = context;
            updatePunctuate();
        }

        private void updatePunctuate() {
            if (!localInterval.equals(getInterval())) {
                localInterval = getInterval();
                if (punctuateCancellor != null) {
                    punctuateCancellor.cancel();
                }
                punctuateCancellor = this.context.schedule(
                        localInterval.toMillis(), PunctuationType.WALL_CLOCK_TIME, this::sendNew);
            }
        }

        @SuppressWarnings({"unused", "PMD.AccessorMethodGeneration"})
        private void sendNew(long timestamp) {
            List<KeyValue<ObservationKey, SourceStatisticsRecord>> sent = new ArrayList<>();

            try (KeyValueIterator<ObservationKey, SourceStatisticsRecord> iterator = store.all()) {
                while (iterator.hasNext()) {
                    KeyValue<ObservationKey, SourceStatisticsRecord> next = iterator.next();
                    if (!next.value.isSent) {
                        context.forward(next.key, next.value.sourceStatistics());
                        sent.add(new KeyValue<>(next.key, next.value.sentRecord()));
                    }
                }
            }

            sent.forEach(e -> store.put(e.key, e.value));
            context.commit();

            updatePunctuate();
        }

        @SuppressWarnings("PMD.AccessorMethodGeneration")
        @Override
        public void process(GenericRecord genericKey, GenericRecord value) {
            if (genericKey == null || value == null) {
                logger.error("Cannot process records without both a key and a value");
                return;
            }
            Schema keySchema = genericKey.getSchema();

            Schema valueSchema = value.getSchema();
            double time = getTime(value, valueSchema, "time", Double.NaN);
            time = getTime(value, valueSchema, "timeReceived", time);
            double timeStart = getTime(genericKey, keySchema, "timeStart", time);
            double timeEnd = getTime(genericKey, keySchema, "timeEnd", time);

            if (Double.isNaN(timeStart) || Double.isNaN(timeEnd)) {
                logger.error("Record did not contain time values: <{}, {}>", genericKey, value);
                return;
            }

            ObservationKey key;
            try {
                key = extractKey(genericKey, keySchema);
            } catch (IllegalArgumentException ex) {
                logger.error("Could not deserialize key"
                        + " without projectId, userId or sourceId: {}", genericKey);
                return;
            }

            SourceStatisticsRecord stats = store.get(key);
            SourceStatisticsRecord newStats = SourceStatisticsRecord.updateRecord(stats, timeStart, timeEnd);
            if (!newStats.equals(stats)) {
                store.put(key, newStats);
            }
        }

        @Override
        public void close() {
            // do nothing
        }
    }

    public static class SourceStatisticsRecord {
        private final double timeStart;
        private final double timeEnd;
        private final boolean isSent;

        @JsonCreator
        public SourceStatisticsRecord(
                @JsonProperty("timeStart") double timeStart,
                @JsonProperty("timeEnd") double timeEnd,
                @JsonProperty("isSent") boolean isSent) {
            this.timeStart = timeStart;
            this.timeEnd = timeEnd;
            this.isSent = isSent;
        }

        public SourceStatistics sourceStatistics() {
            return new SourceStatistics(timeStart, timeEnd);
        }

        static SourceStatisticsRecord updateRecord(SourceStatisticsRecord old,
                double timeStart, double timeEnd) {
            if (old == null) {
                return new SourceStatisticsRecord(timeStart, timeEnd, false);
            } else if (old.timeStart > timeStart || old.timeEnd < timeEnd) {
                return new SourceStatisticsRecord(
                        Math.min(timeStart, old.timeStart),
                        Math.max(timeEnd, old.timeEnd),
                        false);
            } else {
                return old;
            }
        }

        SourceStatisticsRecord sentRecord() {
            return new SourceStatisticsRecord(timeStart, timeEnd, true);
        }
    }

    private static double getTime(GenericRecord record, Schema schema, String fieldName,
            double defaultValue) {
        Schema.Field field = schema.getField(fieldName);
        if (field != null) {
            return ((Number) record.get(field.pos())).doubleValue();
        } else {
            return defaultValue;
        }
    }
}
