package org.pustefixframework.eventbus;

public class TestEventConsumer {

    private TestEvent lastEvent;
    private int eventCount;
    
    @Subscribe
    public void listen(TestEvent event) {
        lastEvent = event;
        eventCount++;
    }

    public TestEvent getLastEvent() {
        return lastEvent;
    }
    
    public int getEventCount() {
        return eventCount;
    }
    
}
