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
package de.schlund.pfixxml.testrecording;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Pustefix application running on a given tomcat.
 * Note that https is not a property of an application because the same application
 * may run secure and insecure.
 */
public class Application implements Serializable {
    private final String name;
    private final String server;
    private final boolean tomcat;
    private final String startPath;
    private final String sessionSuffix;
    
    private static final long serialVersionUID = 6157375568733984286L;

    public Application(String name, String server, boolean tomcat, String startPath, String sessionSuffix) {
        if (server.indexOf('/') != -1) {
            throw new IllegalArgumentException(server);
        }
        if (!startPath.startsWith("/") || startPath.endsWith("/")) {
            throw new IllegalArgumentException(startPath);
        }
        this.name = name;
        this.server = server;
        this.tomcat = tomcat;
        this.startPath = startPath;
        this.sessionSuffix = sessionSuffix;
    }
    
    public Application() {
        throw new IllegalStateException("TODO: castor");
    }

    /** server as stored in session */
    public String getServer() {
        return server;
    }

    public String getStartPath() {
        return startPath;
    }

    public URL getUrl(boolean https, String path) {
        return getUrl(https, path, "nosuchsession." + sessionSuffix);
    }

    public URL getUrl(boolean https, String path, String sessionId) {
        String protocol;
        String port;
        
        if (!path.startsWith("/") || path.endsWith("/")) {
            throw new IllegalArgumentException("invalid path: " + path);
        }
        if (https) {
            protocol = "https";
        } else {
            protocol = "http";
        }
        if (tomcat) {
            port = https ? ":8443" : ":8080";
        } else {
            port = "";
        }
        try {
            return new URL(protocol + "://" + server + port + path + ";jsessionid=" + sessionId);
        } catch (MalformedURLException e) {
            throw new RuntimeException("TODO", e);
        }
    }

    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return "application(name=" + name + ", tomcat=" + tomcat + ", server=" + server + ")";
    }
}
