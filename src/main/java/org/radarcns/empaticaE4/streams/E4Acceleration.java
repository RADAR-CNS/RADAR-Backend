package org.radarcns.empaticaE4.streams;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.radarcns.config.KafkaProperty;
import org.radarcns.empaticaE4.EmpaticaE4Acceleration;
import org.radarcns.empaticaE4.topic.E4Topics;
import org.radarcns.key.MeasurementKey;
import org.radarcns.stream.aggregator.MasterAggregator;
import org.radarcns.stream.aggregator.SensorAggregator;
import org.radarcns.stream.collector.DoubleArrayCollector;
import org.radarcns.topic.SensorTopic;
import org.radarcns.util.RadarSingletonFactory;
import org.radarcns.util.RadarUtilities;
import org.radarcns.util.serde.RadarSerdes;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Definition of Kafka Stream for aggregating data collected by Empatica E4 Acceleromete
 */
public class E4Acceleration extends SensorAggregator<EmpaticaE4Acceleration> {

    private final RadarUtilities UTILITIES = RadarSingletonFactory.getRadarUtilities();

    public E4Acceleration(String clientID, int numThread, MasterAggregator master, KafkaProperty kafkaProperties) throws IOException{
        super(E4Topics.getInstance().getSensorTopics().getAccelerationTopic(),clientID,numThread,master, kafkaProperties);
    }


    @Override
    protected void setStream(@Nonnull KStream<MeasurementKey, EmpaticaE4Acceleration> kstream,
                             @Nonnull SensorTopic<EmpaticaE4Acceleration> topic)
            throws IOException {
        kstream.groupByKey()
                .aggregate(
                    DoubleArrayCollector::new,
                    (k, v, valueCollector) -> valueCollector.add(UTILITIES.accelerationToArray(v)),
                    TimeWindows.of(10 * 1000L),
                    RadarSerdes.getInstance().getDoubelArrayCollector(),
                    topic.getStateStoreName())
                .toStream()
                .map((k,v) -> new KeyValue<>(UTILITIES.getWindowed(k), v.convertInAvro()))
                .to(topic.getOutputTopic());
    }
}
