package de.schlund.pfixxml.jmx;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnectionNotification;

import org.apache.log4j.Logger;

public class JMXConnectionListener implements NotificationListener {

    private static Logger LOG = Logger.getLogger(JMXConnectionListener.class);
    
    public void handleNotification(Notification notification, Object handback) {
        LOG.info("Got notification: "+notification.getClass().getName()+"\n");
        if(notification instanceof JMXConnectionNotification) {
            JMXConnectionNotification cn = (JMXConnectionNotification)notification;
            StringBuffer sb = new StringBuffer();
            sb.append("***"+cn.getClass().getName()+"***\n");
            sb.append(" ConnectionId:  "+cn.getConnectionId()+"\n");
            sb.append(" Message:       "+cn.getMessage()+"\n");
            sb.append(" Sequence:      "+cn.getSequenceNumber()+"\n");
            sb.append(" TimeStamp:     "+cn.getTimeStamp()+"\n");
            sb.append(" Type:          "+cn.getType()+"\n");
            sb.append(" Source:        "+cn.getSource()+"\n");
            sb.append(" UserData:      "+cn.getUserData()+"\n");
            LOG.info(sb.toString());
        }
    }

}
