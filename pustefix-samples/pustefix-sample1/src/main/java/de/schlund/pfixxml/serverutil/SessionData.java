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

package de.schlund.pfixxml.serverutil;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SessionData implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1211421548094778389L;
    private final String  id;
    private final long    creation;
    private long          lastaccess;
    private final String  serverName;
    private final String  remoteAddr;
    
    public SessionData() {
        throw new IllegalArgumentException(); // TODO: castor
    }
    public SessionData(String id, String serverName, String remoteAddr) {
        this.id = id;
        this.serverName = serverName;
        this.remoteAddr = remoteAddr;
        this.creation = System.currentTimeMillis();
        this.lastaccess = creation;
    }
    
    public String getId() {
        return id;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public String getRemoteAddr() {
        return remoteAddr;
    }
    
    public void updateTimestamp() {
        lastaccess = System.currentTimeMillis();
    }

    public long getCreation() {
        return creation;
    }
    
    public String getCreationTime() {
        return time(creation);
    }
    
    /**
     * Get the value of the last access to the session (access to subframes isn't counted)
     * @return value of lastAccess.
     */
    public long getLastAccess() {
        return lastaccess;
    }
    
    public String getLastAccessTime() {
        return time(lastaccess);
    }
    
    private static final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
    
    private String time(long millis) {
        return fmt.format(new Date(millis));
    }
}


