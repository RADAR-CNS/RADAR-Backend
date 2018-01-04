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

import java.io.IOException;

/**
 * Subcommand of RadarBackend to run.
 */
public interface SubCommand {
    /**
     * Start the subcommand. The command is not guaranteed to return
     * immediately.
     * @throws IOException if the command cannot be started
     * @throws InterruptedException if the command is interrupted
     */
    void start() throws IOException, InterruptedException;

    /** Stop the subcommand, possibly waiting for it to complete. */
    void shutdown() throws IOException, InterruptedException;
}
