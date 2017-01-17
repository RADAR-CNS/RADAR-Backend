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

package org.radarcns.monitor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import org.radarcns.config.BatteryMonitorConfig;
import org.radarcns.config.DisconnectMonitorConfig;
import org.radarcns.config.MonitorConfig;
import org.radarcns.config.RadarBackendOptions;
import org.radarcns.config.RadarPropertyHandler;
import org.radarcns.util.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaMonitorFactory {
    private static final Logger logger = LoggerFactory.getLogger(KafkaMonitorFactory.class);

    private final RadarPropertyHandler properties;
    private final RadarBackendOptions options;

    public KafkaMonitorFactory(RadarBackendOptions options,
            RadarPropertyHandler properties) {
        this.options = options;
        this.properties = properties;
    }

    public KafkaMonitor createMonitor() throws IOException {
        KafkaMonitor monitor;
        String[] args = options.getSubCommandArgs();
        String commandType;
        if (args == null || args.length == 0) {
            commandType = "battery";
        } else {
            commandType = args[0];
        }
        switch (commandType) {
            case "battery":
                monitor = createBatteryLevelMonitor();
                break;
            case "disconnect":
                monitor = createDisconnectMonitor();
                break;
            default:
                throw new IllegalArgumentException("Cannot create unknown monitor " + commandType);
        }
        return monitor;
    }

    private KafkaMonitor createBatteryLevelMonitor() {
        BatteryLevelMonitor.Status minLevel = BatteryLevelMonitor.Status.CRITICAL;
        BatteryMonitorConfig config = properties.getRadarProperties().getBatteryMonitor();
        EmailSender sender = getSender(config);
        Collection<String> topics = getTopics(config, "android_empatica_e4_battery_level");

        if (config != null && config.getLevel() != null) {
            String level = config.getLevel().toUpperCase(Locale.US);
            try {
                minLevel = BatteryLevelMonitor.Status.valueOf(level);
            } catch (IllegalArgumentException ex) {
                logger.warn("Minimum battery level {} is not recognized. "
                                + "Choose from {} instead. Using CRITICAL.",
                        level, Arrays.toString(BatteryLevelMonitor.Status.values()));
            }
        }

        return new BatteryLevelMonitor(properties, topics, sender, minLevel);
    }

    private KafkaMonitor createDisconnectMonitor() {
        DisconnectMonitorConfig config = properties.getRadarProperties().getDisconnectMonitor();
        EmailSender sender = getSender(config);
        long timeout = 300_000L;  // 5 minutes
        if (config != null && config.getTimeout() != null) {
            timeout = config.getTimeout();
        }
        Collection<String> topics = getTopics(config, "android_empatica_e4_temperature");
        return new DisconnectMonitor(properties, topics, "temperature_disconnect", sender,
                timeout);
    }

    private EmailSender getSender(MonitorConfig config) {
        if (config != null && config.getEmailAddress() != null) {
            return new EmailSender("localhost", 25, "no-reply@radar-cns.org",
                    Collections.singletonList(config.getEmailAddress()));
        }
        return null;
    }

    private Collection<String> getTopics(MonitorConfig config, String defaultTopic) {
        if (config != null && config.getTopics() != null) {
            return config.getTopics();
        } else {
            return Collections.singleton(defaultTopic);
        }
    }
}
