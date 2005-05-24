/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixxml.jmx;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import javax.management.Notification;
import javax.management.NotificationListener;

/** Listens to TrailLogger and writes the resulting steps to a trail file. */
public class TrailWriter implements NotificationListener {
    private static final String TRAIL_FILE_SUFFIX = ".trail";

    public static File[] listTrailFiles(File basedir) {
       return basedir.listFiles(new FileFilter() {
           public boolean accept(File file) {
                return file.isFile() && file.getName().endsWith(TRAIL_FILE_SUFFIX);
           }});
    }
    
    //--
    
    public static TrailWriter createTmp(String name) throws IOException {
        File file;
        
        file = File.createTempFile(name, TRAIL_FILE_SUFFIX);
        file.deleteOnExit();
        return new TrailWriter(file);
    }
    
    //-- 
    
	private final File file;
	private final Writer dest;
	private boolean closed;
	private IOException exception;
	
    public TrailWriter(File file) throws IOException {
        this.file = file;
        this.dest = new FileWriter(file);
        this.exception = null;
        this.closed = false;
        write("<trail>");
    }

    private void write(String str) {
        if (exception != null) {
            return;
        }
        if (closed) {
            throw new RuntimeException(str);
        }
        try {
            dest.write(str);
        } catch (IOException e) {
            exception = e;
        }
    }

    public synchronized void handleNotification(Notification notification, Object notUsed) {
        String type;
        
        type = notification.getType();
        if (TrailLogger.NOTIFICATION_TYPE.equals(type)) {
            write((String) notification.getUserData());
        } else if (TrailLogger.CLOSE_TYPE.equals(type)) {
            closed = true;
        } else {
            throw new RuntimeException("unkown notification: " + type);
        }
    }

    private static final int RETRY = 10;
    private static final int SLEEP = 200;
        
    public synchronized File close() throws IOException {
        int i;
        
        for (i = 0; i < RETRY; i++) {
            if (exception != null) {
                throw exception;
            }
            if (closed) {
                dest.write("</trail>");
                dest.close();
                return file;
            }
            try {
                System.out.println("sleep ");
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        throw new IOException("writer is not closed: " + file);
    }
}
