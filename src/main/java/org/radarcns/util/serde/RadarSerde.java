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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

/**
 * It generates the jsonSerializer and jsonDeserializer for the given input class.
 */
public class RadarSerde<T> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    static final ObjectWriter GENERIC_WRITER = MAPPER.writer();
    static final ObjectReader GENERIC_READER = MAPPER.reader();

    private final JsonSerializer<T> jsonSerializer;
    private final JsonDeserializer<T> jsonDeserializer;

    public RadarSerde(Class<T> type) {
        this.jsonSerializer = new JsonSerializer<>(type);
        this.jsonDeserializer = new JsonDeserializer<>(type);
    }

    public Serde<T> getSerde() {
        return Serdes.serdeFrom(jsonSerializer, jsonDeserializer);
    }
}
