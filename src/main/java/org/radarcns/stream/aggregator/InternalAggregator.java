/*
 * Copyright 2017 Kings College London and The Hyve
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

package org.radarcns.stream.aggregator;

import java.io.IOException;
import javax.annotation.Nonnull;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.radarcns.config.KafkaProperty;
import org.radarcns.key.MeasurementKey;
import org.radarcns.key.WindowedKey;
import org.radarcns.topic.InternalTopic;

/**
 * Runnable abstraction of a Kafka stream that consumes Internal Topic
 * @param <I> consumed message type
 * @param <O> aggregated results type
 * @see InternalTopic
 * @see org.radarcns.config.KafkaProperty
 * @see org.radarcns.stream.aggregator.DeviceTimestampExtractor
 */
public abstract class InternalAggregator<I, O extends SpecificRecord>
        extends AggregatorWorker<WindowedKey, O, InternalTopic<O>> {
    /**
     * @param topic     kafka topic that will be consumed
     * @param clientId  useful to debug usign the Kafka log
     * @param monitor states whether it requires or not a Monitor to check the stream progress
     * @param master    pointer to the MasterAggregator useful to call the notification functions
     */
    public InternalAggregator(@Nonnull InternalTopic<O> topic, @Nonnull String clientId,
                                boolean monitor, @Nonnull MasterAggregator master,
                                @Nonnull KafkaProperty kafkaProperty) {
        this(topic, clientId, 1, monitor, master, kafkaProperty);
    }

    /**
     * @param topic      kafka topic that will be consumed
     * @param clientId   useful to debug usign the Kafka log
     * @param numThread  number of threads to execute stream processing
     * @param monitor states whether it requires or not a Monitor to check the stream progress
     * @param master     pointer to the MasterAggregator useful to call the notification functions
     */
    public InternalAggregator(@Nonnull InternalTopic<O> topic, @Nonnull String clientId,
                                int numThread, boolean monitor, @Nonnull MasterAggregator master,
                                @Nonnull KafkaProperty kafkaProperty) {
        super(topic, clientId, numThread, monitor, master, kafkaProperty);
    }

    @Override
    protected KStreamBuilder getBuilder() throws IOException {
        KStreamBuilder builder = super.getBuilder();

        KStream<MeasurementKey, I> valueKStream = builder.stream(getTopic().getInputTopic());
        setStream(valueKStream, getTopic());

        return builder;
    }

    /**
     * @implSpec it defines the stream computation
     */
    protected  abstract void setStream(@Nonnull KStream<MeasurementKey, I> kstream,
                                      @Nonnull InternalTopic<O> topic) throws IOException;

}
