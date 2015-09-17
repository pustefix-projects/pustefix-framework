package org.pustefixframework.eventbus;

public class TestSubEventConsumer {

    private TestSubEvent lastEvent;
    private int eventCount;
    
    @Subscribe
    public void listen(TestSubEvent event) {
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
