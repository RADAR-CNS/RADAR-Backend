/*
 * Copyright 2017 King's College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarcns.config;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.TimestampExtractor;
import org.radarcns.util.serde.SpecificAvroSerde;

// TODO this class should substitute org.radarcns.util.RadarConfig
public class KafkaProperty {

    private final ConfigRadar configRadar;

    protected KafkaProperty(ConfigRadar configRadar) {
        this.configRadar = configRadar;
    }

    /**
     * @param clientId useful for debugging
     * @param numThread number of threads to execute stream processing
     * @return Properties for a Kafka Stream
     */
    public Properties getStream(@Nonnull String clientId, @Nonnull int numThread) {
        Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, clientId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, configRadar.getBrokerPaths());
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                configRadar.getSchemaRegistryPaths());
        props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, numThread);

        if (configRadar.getAutoCommitIntervalMs() != null) {
            props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, configRadar
                    .getAutoCommitIntervalMs());
        }

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return props;
    }

    /**
     * @param clientId useful for debugging
     * @param numThread number of threads to execute stream processing
     * @param timestampExtractor custom timestamp extract that overrides the out-of-the-box
     * @return Properties for a Kafka Stream
     */
    public Properties getStream(@Nonnull String clientId, @Nonnull int numThread,
                                @Nonnull Class<? extends TimestampExtractor> timestampExtractor) {
        Properties props = getStream(clientId, numThread);

        props.put(StreamsConfig.TIMESTAMP_EXTRACTOR_CLASS_CONFIG, timestampExtractor.getName());

        return props;
    }

}
