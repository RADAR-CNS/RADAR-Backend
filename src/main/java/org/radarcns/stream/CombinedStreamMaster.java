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

package org.radarcns.stream;

import java.util.Collection;
import java.util.List;
import org.radarcns.config.ConfigRadar;

/** Combine multiple StreamMasters into a single object. */
public class CombinedStreamMaster extends StreamMaster {

    private final Collection<StreamMaster> streamMasters;

    /**
     * Create a stream master that will act as a master over given stream masters.
     * @param streamMasters stream masters to take care of
     */
    public CombinedStreamMaster(Collection<StreamMaster> streamMasters) {
        if (streamMasters == null || streamMasters.isEmpty()) {
            throw new IllegalArgumentException("Stream workers collection may not be empty");
        }
        this.streamMasters = streamMasters;
    }

    @Override
    public void setNumberOfThreads(ConfigRadar config) {
        for (StreamMaster master : streamMasters) {
            master.setNumberOfThreads(config);
        }
    }

    @Override
    protected void createWorkers(List<StreamWorker> list, StreamMaster streamMaster) {
        for (StreamMaster master : streamMasters) {
            master.createWorkers(list, streamMaster);
        }
    }
}
