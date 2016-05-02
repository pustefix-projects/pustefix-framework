package org.pustefixframework.eventbus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("spring-test.xml")
@WebAppConfiguration
public class EventBusTest {

    @Autowired
    EventBus eventBus;
    
    @Autowired
    TestEventProducer producer;
    
    @Autowired
    @Qualifier("testEventConsumer")
    TestEventConsumer consumer;
    
    @Autowired
    @Qualifier("testEventConsumerScoped")
    TestEventConsumer sessionConsumer;
    
    @Autowired
    @Qualifier("testSubEventConsumer")
    TestSubEventConsumer subConsumer;
    
    @Autowired
    IEventListener eventListener;
    
    @Autowired
    IEventListenerBean eventListenerBean;
    
    @Test
    public void test() {
        
        TestEvent event = new TestEvent();
        eventBus.publish(event);
        assertSame(event, consumer.getLastEvent());
        assertSame(event, sessionConsumer.getLastEvent());
        assertNull(subConsumer.getLastEvent());
        assertEquals(1, consumer.getEventCount());
        assertEquals(1, sessionConsumer.getEventCount());
        assertEquals(0, subConsumer.getEventCount());
        
        //test event not listened to
        eventBus.publish(new Object());
        assertSame(event, consumer.getLastEvent());
        assertSame(event, sessionConsumer.getLastEvent());
        assertNull(subConsumer.getLastEvent());
        assertEquals(1, consumer.getEventCount());
        assertEquals(1, sessionConsumer.getEventCount());
        assertEquals(0, subConsumer.getEventCount());
        
        producer.publish();
        assertSame(consumer.getLastEvent(), sessionConsumer.getLastEvent());
        assertNull(subConsumer.getLastEvent());
        assertEquals(2, consumer.getEventCount());
        assertEquals(2, sessionConsumer.getEventCount());
        assertEquals(0, subConsumer.getEventCount()); 
       
        //test derived event class
        TestSubEvent subEvent = new TestSubEvent();
        eventBus.publish(subEvent);
        assertSame(subEvent, consumer.getLastEvent());
        assertSame(subEvent, sessionConsumer.getLastEvent());
        assertSame(subEvent, subConsumer.getLastEvent());
        assertEquals(3, consumer.getEventCount());
        assertEquals(3, sessionConsumer.getEventCount());
        assertEquals(1, subConsumer.getEventCount());
      
        //test event interface
        IEvent e = new IEventImpl();
        eventBus.publish(e);
        assertSame(e, eventListener.getEvents().get(0));
        assertSame(e, eventListener.getEvents().get(1));
        assertEquals(2, eventListener.getEvents().size());
        assertSame(e, eventListenerBean.getEvents().get(0));
        assertSame(e, eventListenerBean.getEvents().get(1));
        assertEquals(2, eventListenerBean.getEvents().size());
    
    }

}
