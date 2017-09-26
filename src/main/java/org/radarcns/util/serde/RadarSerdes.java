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

package org.radarcns.util.serde;

import org.apache.kafka.common.serialization.Serde;
import org.radarcns.stream.collector.DoubleArrayCollector;
import org.radarcns.stream.collector.DoubleValueCollector;
import org.radarcns.stream.phone.PhoneUsageCollector;

/**
 * Set of Serde usefull for Kafka Streams
 */
public final class RadarSerdes {
    private final Serde<DoubleValueCollector> doubelCollector;
    private final Serde<DoubleArrayCollector> doubelArrayCollector;
    private final Serde<PhoneUsageCollector> phoneUsageCollector;

    private static RadarSerdes instance = new RadarSerdes();

    public static RadarSerdes getInstance() {
        return instance;
    }

    private RadarSerdes() {
        doubelCollector = new RadarSerde<>(DoubleValueCollector.class).getSerde();
        doubelArrayCollector = new RadarSerde<>(DoubleArrayCollector.class).getSerde();
        phoneUsageCollector = new RadarSerde<>(PhoneUsageCollector.class).getSerde();
    }

    public Serde<DoubleValueCollector> getDoubleCollector() {
        return doubelCollector;
    }

    public Serde<DoubleArrayCollector> getDoubleArrayCollector()  {
        return doubelArrayCollector;
    }

    public Serde<PhoneUsageCollector> getPhoneUsageCollector() {
        return phoneUsageCollector;
    }
}
