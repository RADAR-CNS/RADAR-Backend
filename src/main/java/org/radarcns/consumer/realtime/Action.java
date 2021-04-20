package org.radarcns.consumer.realtime;

import java.io.IOException;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface Action {
    String getName();

    Boolean executeFor(ConsumerRecord<?,?> record) throws IllegalArgumentException, IOException;

}