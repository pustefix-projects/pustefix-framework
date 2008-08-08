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
 */

package de.schlund.pfixcore.util;



import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.context.ServerContextImpl;
import de.schlund.pfixxml.config.ContextXMLServletConfig;

/**
 * Helper class that provides a dummy context instance that can
 * be used within unit tests.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DummyContextFactory {

    public static Context getDummyContext(ContextXMLServletConfig config) throws Exception {
        HttpSession        session        = new DummySession();
        ServerContextImpl  servercontext  = new ServerContextImpl();
        servercontext.init(config.getContextConfig(), "Dummy");
        ContextImpl        context        = new ContextImpl();
        context.setServerContext(servercontext);
        context.setSession(session);
        context.prepareForRequest();
        return context;
    }
    
    private static class DummySession implements HttpSession {

        private int maxInactiveInterval;
        
        private Map<String, Object> atts = Collections.synchronizedMap(new HashMap<String, Object>());
        
        public long getCreationTime() {
            return 0;
        }

        public String getId() {
            return "dummysession";
        }

        public long getLastAccessedTime() {
            return 0;
        }

        public ServletContext getServletContext() {
            // There is no ServletContext
            return null;
        }

        public void setMaxInactiveInterval(int arg0) {
            this.maxInactiveInterval = arg0;
        }

        public int getMaxInactiveInterval() {
            return this.maxInactiveInterval;
        }

        @SuppressWarnings("deprecation")
        public javax.servlet.http.HttpSessionContext getSessionContext() {
            return null;
        }

        public Object getAttribute(String arg0) {
            return atts.get(arg0);
        }

        public Object getValue(String arg0) {
            return getAttribute(arg0);
        }

        @SuppressWarnings("unchecked")
        public Enumeration getAttributeNames() {
            return Collections.enumeration(atts.keySet());
        }

        public String[] getValueNames() {
            return atts.keySet().toArray(new String[0]);
        }

        public void setAttribute(String arg0, Object arg1) {
            atts.put(arg0, arg1);
            
        }

        public void putValue(String arg0, Object arg1) {
            setAttribute(arg0, arg1);
        }

        public void removeAttribute(String arg0) {
            atts.remove(arg0);
        }

        public void removeValue(String arg0) {
            removeAttribute(arg0);
        }

        public void invalidate() {
            // Does nothing here
        }

        public boolean isNew() {
            // Here: always false
            return false;
        }
        
    }
}
