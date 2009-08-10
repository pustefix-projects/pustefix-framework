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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class ApplicationList implements Serializable {
    private static final Logger LOG = Logger.getLogger(ApplicationList.class);
    
    private static final long serialVersionUID = 4898818721473076365L;
    
    private final List<Application> apps;
    
    public ApplicationList() {
        apps = new ArrayList<Application>();
    }
    
    public void add(Application app) {
        if (lookup(app.getName()) != null) {
            throw new IllegalArgumentException("duplicate application " + app.getName());
        }
        apps.add(app);
    }
    
    public int size() {
        return apps.size();
    }
    
    public Application get(String displayName) {
        Application result;
        
        result = lookup(displayName);
        if (result == null) {
            throw new IllegalArgumentException("unknown application: " + displayName);
        }
        return result;
    }
    
    public Application lookup(String displayName) {
        Iterator<Application> iter;
        Application app;
        
        iter = apps.iterator();
        while (iter.hasNext()) {
            app = (Application) iter.next();
            if (app.getName().equals(displayName)) {
                return app;
            }
        }
        return null;
    }
    
    public List<Application> getApplications() {
        return apps;
    }
    
    @Override
    public String toString() {
        return "applications(" + apps.toString() + ")";
    }
}
