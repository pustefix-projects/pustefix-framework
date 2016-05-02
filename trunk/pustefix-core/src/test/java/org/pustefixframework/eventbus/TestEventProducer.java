package org.pustefixframework.eventbus;

import org.springframework.beans.factory.annotation.Autowired;

public class TestEventProducer {

    @Autowired
    EventBus eventBus;
    
    public void publish() {
        eventBus.publish(new TestEvent());
    }
    
}
