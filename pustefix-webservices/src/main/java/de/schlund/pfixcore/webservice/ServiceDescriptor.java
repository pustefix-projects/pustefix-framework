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

package de.schlund.pfixcore.webservice;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.schlund.pfixcore.webservice.config.ServiceConfig;

/**
 * @author mleidig@schlund.de
 */
public class ServiceDescriptor {

    Class<?> serviceClass;
    Map<String,List<Method>> serviceMethods;

    public ServiceDescriptor(Class<?> serviceClass) throws ServiceException {
        this.serviceClass=serviceClass;
        serviceMethods=introspect(serviceClass);
    }
    
	public ServiceDescriptor(ServiceConfig serviceConfig) throws ServiceException {
        try {
            ClassLoader cl=Thread.currentThread().getContextClassLoader();
            Class<?> itf=Class.forName(serviceConfig.getInterfaceName(),true,cl);
            Class<?> clazz=Class.forName(serviceConfig.getImplementationName(),true,cl);
            serviceMethods=introspect(clazz,itf);
            serviceClass=itf;
        } catch (ClassNotFoundException x) {
            throw new ServiceException("Can't instantiate service class.",x);
        }
	}
    
    public Class<?> getServiceClass() {
        return serviceClass;
    }
    
	private Map<String,List<Method>> introspect(Class<?> clazz,Class<?> itf) throws ServiceException {
        if(itf!=null) {
            if(!itf.isInterface()) throw new IllegalArgumentException("Class '"+itf.getName()+"' is no interface!");
            if(!itf.isAssignableFrom(clazz)) 
                throw new IllegalArgumentException("Class '"+clazz.getName()+"' doesn't implement interface '"+itf.getName()+"'!");
        }
        Map<String,List<Method>> methods=new HashMap<String,List<Method>>();
        Method[] meths=itf.getMethods();
        for(int i=0;i<meths.length;i++) {
            String name=meths[i].getName();
            Method implMeth=null;
            try {
                implMeth=clazz.getMethod(name,meths[i].getParameterTypes());
            } catch(NoSuchMethodException x) {
                throw new IllegalArgumentException("Method '"+name+"' not found!",x);
            }
            List<Method> ml=methods.get(name);
            if(ml==null) {
                ml=new ArrayList<Method>();
                methods.put(name,ml);
            }
            ml.add(implMeth);
        }
        return methods;
	}
    
    private Map<String,List<Method>> introspect(Class<?> clazz) throws ServiceException {
        Map<String,List<Method>> methods=new HashMap<String,List<Method>>();
        Class<?> current=clazz;
        while(current!=null && !current.equals(Object.class)) {
            Method[] meths=current.getDeclaredMethods();
            for(int i=0;i<meths.length;i++) {
                int modifiers=meths[i].getModifiers();
                if(Modifier.isPublic(modifiers)) {
                    String name=meths[i].getName();
                    List<Method> ml=methods.get(name);
                    if(ml==null) {
                        ml=new ArrayList<Method>();
                        methods.put(name,ml);
                    }
                    ml.add(meths[i]);
                }
            }
            current=current.getSuperclass();
        }
        return methods;
    }
	
	public Set<String> getMethods() {
		return Collections.unmodifiableSet(serviceMethods.keySet());
	}
    
    public List<Method> getMethods(String name) {
        return Collections.unmodifiableList(serviceMethods.get(name));
    }
	
	public String toString() {
		StringBuffer sb=new StringBuffer();
		Iterator<String> it=serviceMethods.keySet().iterator();
		while(it.hasNext()) {
			String methName=it.next();
			sb.append(methName+"\n");
		}
		return sb.toString();
	}
    
}
