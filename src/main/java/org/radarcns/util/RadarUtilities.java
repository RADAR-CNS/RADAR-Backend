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

package org.radarcns.util;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.radarcns.aggregator.PhoneUsageAggregator;
import org.radarcns.kafka.AggregateKey;
import org.radarcns.kafka.ObservationKey;
import org.radarcns.passive.empatica.EmpaticaE4Acceleration;
import org.radarcns.stream.aggregator.DoubleAggregation;
import org.radarcns.stream.aggregator.DoubleArrayAggregation;
import org.radarcns.stream.collector.DoubleArrayCollector;
import org.radarcns.stream.collector.DoubleValueCollector;
import org.radarcns.stream.phone.PhoneUsageCollector;
import org.radarcns.stream.phone.TemporaryPackageKey;

/**
 * Interface that facades all utility functions that are required to support RadarBackend features.
 */
public interface RadarUtilities {

    /**
     * Creates a AggregateKey for a window of ObservationKey.
     * @param window Windowed measurement keys
     * @return relevant AggregateKey
     */
    AggregateKey getWindowed(Windowed<ObservationKey> window);

    AggregateKey getWindowedTuple(Windowed<TemporaryPackageKey> window);

    KeyValue<AggregateKey, DoubleArrayAggregation> collectorToAvro(
            Windowed<ObservationKey> window, DoubleArrayCollector collector);

    KeyValue<AggregateKey, DoubleAggregation> collectorToAvro(
            Windowed<ObservationKey> window, DoubleValueCollector collector);

    KeyValue<AggregateKey, PhoneUsageAggregator> collectorToAvro(
            Windowed<TemporaryPackageKey> window, PhoneUsageCollector collector);

    double floatToDouble(float input);

    double ibiToHeartRate(float input);

    double[] accelerationToArray(EmpaticaE4Acceleration value);
}
