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
package org.pustefixframework.admin.mbeans;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.auth.Role;
import de.schlund.pfixcore.auth.RoleNotFoundException;
import de.schlund.pfixcore.workflow.Context;

/**
 * 
 * @author mleidig@schlund.de
 * 
 */
public class AuthAdmin implements AuthAdminMBean {

    public final static Logger LOG = Logger.getLogger(AuthAdmin.class);

    private static AuthAdmin instance = new AuthAdmin();

    public static AuthAdmin getInstance() {
        return instance;
    }

    public void init(Properties props) {
        try {
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName("Pustefix:type=AuthAdmin");
            mbeanServer.registerMBean(this, objectName);
        } catch (Exception x) {
            LOG.error("Can't register AuthAdmin MBean!", x);
        }
    }

    // FIXME: Create a new implementation or dump this class
    /*
    private Context getContext(String sessionId) {
        SessionAdmin sessionAdmin = SessionAdmin.getInstance();
        try {
            HttpSession session = sessionAdmin.getSession(sessionId);
            if (session != null) {
                SessionContextStore store = SessionContextStore.getInstance(session);
                Set<String> servlets = store.getServletNames();
                if (servlets.size() > 0) {
                    String servlet = servlets.iterator().next();
                    ContextImpl context = store.getContext(servlet);
                    if (context != null) return context;
                    else throw new RuntimeException("No context found!");
                } else throw new RuntimeException("No context servlet found!");
            } else throw new RuntimeException("No HttpSession found!");
        } catch (IOException x) {
            throw new RuntimeException("Can't get context!", x);
        }
    }
    */
    private Context getContext(String sessionId) {
        throw new UnsupportedOperationException("This method has to be fixed.");
    }
    
    public String[] listAvailableRoles(String sessionId) {
        Context context = getContext(sessionId);
        List<Role> roles = context.getContextConfig().getRoleProvider().getRoles();
        String[] roleArr = new String[roles.size()];
        for (int i = 0; i < roles.size(); i++)
            roleArr[i] = roles.get(i).getName();
        Arrays.sort(roleArr);
        return roleArr;
    }

    public String[] listCurrentRoles(String sessionId) {
        Context context = getContext(sessionId);
        List<String> roles = new ArrayList<String>();
        for (Role role : context.getAuthentication().getRoles()) {
            roles.add(role.getName());
        }
        String[] roleArr = new String[roles.size()];
        roles.toArray(roleArr);
        Arrays.sort(roleArr);
        return roleArr;
    }

    public boolean addRole(String sessionId, String name) {
        Context context = getContext(sessionId);
        try {
            return context.getAuthentication().addRole(name);
        } catch (RoleNotFoundException x) {
            throw new RuntimeException("Illegal role: " + name);
        }
    }

    public boolean revokeRole(String sessionId, String name) {
        Context context = getContext(sessionId);
        return context.getAuthentication().revokeRole(name);
    }

}
