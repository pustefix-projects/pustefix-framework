package org.pustefixframework.admin.mbeans;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;


/**
 * MBean Facade providing asynchronous access to Admin MBean methods,
 * thus avoiding Thread/AccessControlContext dependencies on the WebappClassLoader
 * when triggering Admin methods from within the application (e.g. prevents memory
 * leaks when reloading the webapp)
 * 
 * @author mleidig@schlund.de
 *
 */
public class AdminBroadcaster extends NotificationBroadcasterSupport implements AdminBroadcasterMBean {

    public final static String JMX_NAME = Admin.JMX_NAME + ",subtype=Broadcaster";
    
    public final static String NOTIFICATION_TYPE_RELOAD = "RELOAD";
    
    private AtomicLong seqNo = new AtomicLong();
    
    public AdminBroadcaster() {
        super(new ThreadPoolExecutor(0, 2, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3)));
    }
    
    public void reload(String workDir) {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(null);
        Notification notification = new Notification(NOTIFICATION_TYPE_RELOAD, this, seqNo.incrementAndGet());
        notification.setUserData(workDir);
        sendNotification(notification);
        Thread.currentThread().setContextClassLoader(current);
    }
    
}
