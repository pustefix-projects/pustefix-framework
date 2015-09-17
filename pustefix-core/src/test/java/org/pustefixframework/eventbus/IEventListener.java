package org.pustefixframework.eventbus;

import java.util.ArrayList;
import java.util.List;

public class IEventListener {

    private List<IEvent> events = new ArrayList<>();
    
    @Subscribe
    public void listen(IEvent event) {
        events.add(event);
    }
    
    @Subscribe
    public void listen(IEventImpl event) {
        events.add(event);
    }
    
    public List<IEvent> getEvents() {
        return events;
    }
    
}
