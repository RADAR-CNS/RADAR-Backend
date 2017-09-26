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

package org.radarcns.stream.empatica;

import java.util.List;
import org.radarcns.config.KafkaProperty;
import org.radarcns.config.RadarPropertyHandler;
import org.radarcns.stream.StreamGroup;
import org.radarcns.stream.StreamMaster;
import org.radarcns.stream.StreamWorker;
import org.radarcns.util.RadarSingletonFactory;

/**
 * Singleton StreamMaster for Empatica E4.
 * @see StreamMaster
 */
public class E4StreamMaster extends StreamMaster {

    protected StreamGroup getStreamGroup() {
        return  E4Streams.getInstance();
    }

    @Override
    protected void createWorkers(List<StreamWorker<?,?>> list) {
        RadarPropertyHandler propertyHandler = RadarSingletonFactory.getRadarPropertyHandler();
        KafkaProperty kafkaProperty = propertyHandler.getKafkaProperties();
        list.add(new E4AccelerationStream("E4AccelerationStream", highPriority(), this, kafkaProperty));
        list.add(new E4BatteryLevelStream("E4BatteryLevelStream", lowPriority(), this, kafkaProperty));
        list.add(new E4BloodVolumePulseStream(
                "E4BloodVolumePulseStream", highPriority(), this, kafkaProperty));
        list.add(new E4ElectroDermalActivityStream(
                "E4ElectroDermalActivityStream", normalPriority(), this, kafkaProperty));
        list.add(new E4HeartRateStream("E4HeartRateStream", highPriority(),this, kafkaProperty));
        list.add(new E4InterBeatIntervalStream(
                "E4InterBeatIntervalStream", highPriority(),this, kafkaProperty));
        list.add(new E4TemperatureStream("E4TemperatureStream", highPriority(), this, kafkaProperty));
    }
}
