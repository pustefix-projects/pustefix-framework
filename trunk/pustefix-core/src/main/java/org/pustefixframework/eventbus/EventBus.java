/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.eventbus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central event dispatcher. Can be called to publish events, which then will
 * be propagated to the event subscribers registered for the according event type.
 */
public class EventBus {

    private Map<Class<?>, List<EventSubscriber>> eventToSubscribers = new ConcurrentHashMap<>();

    /**
     * Publishes an event to all registered event subscribers.
     *
     * @param event  Event object to be published. 
     */
    public void publish(Object event) {
        
        Class<?> eventType = event.getClass();
        while(eventType != Object.class) {
            publish(event, eventType);
            eventType = eventType.getSuperclass();
        }
            
        Class<?>[] eventInterfaces = event.getClass().getInterfaces();
        for(Class<?> eventInterface : eventInterfaces) {
            publish(event, eventInterface);
        }
        
    }
    
    private void publish(Object event, Class<?> eventType) {
        
        List<EventSubscriber> subscribers = eventToSubscribers.get(eventType);
        if(subscribers != null) {
            for(EventSubscriber subscriber : subscribers) {
                Method method = subscriber.getMethod();
                try {
                    method.invoke(subscriber.getBean(), event);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException("Event publishing failed", e);
                }
            }
        }
    }

    void addSubscriber(EventSubscriber subscriber) {
        
        List<EventSubscriber>  subscribers = eventToSubscribers.get(subscriber.getEventType());
        if(subscribers == null) {
            subscribers = new CopyOnWriteArrayList<EventSubscriber>();
            eventToSubscribers.put(subscriber.getEventType(), subscribers);
        }
        subscribers.add(subscriber);
    }

}