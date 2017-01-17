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

package org.radarcns.empatica.topic;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.radarcns.topic.SensorTopic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
/**
 * Created by nivethika on 21-12-16.
 */
public class E4SensorTopicsTest {

    private E4SensorTopics sensorTopics;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        this.sensorTopics = E4SensorTopics.getInstance();
    }

    @Test
    public void getTopicNames() {
        Set<String> sensorTopics = this.sensorTopics.getTopicNames();
        assertEquals(14, sensorTopics.size());
    }

    @Test
    public void getTopic() {
        List<String> topicNames = new ArrayList<String>();
        topicNames.add("android_empatica_e4_acceleration");
        topicNames.add("android_empatica_e4_battery_level");
        topicNames.add("android_empatica_e4_blood_volume_pulse");
        topicNames.add("android_empatica_e4_electrodermal_activity");
        topicNames.add("android_empatica_e4_inter_beat_interval");
        topicNames.add("android_empatica_e4_sensor_status");
        topicNames.add("android_empatica_e4_temperature");

        for(String topicName :topicNames) {
            SensorTopic topic = this.sensorTopics.getTopic(topicName);
            assertEquals(topic.getInputTopic(), topicName);
        }
    }

    @Test
    public void getUnknownTopic() {

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Topic something unknown");
        this.sensorTopics.getTopic("something");
    }
}
