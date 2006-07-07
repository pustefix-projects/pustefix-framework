package de.schlund.pfixcore.webservice;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;

import de.schlund.pfixcore.webservice.config.ServiceConfig;

public class ServiceDescriptor {

	ServiceConfig service;
	ArrayList<String> methods=new ArrayList<String>();
	
	public ServiceDescriptor(ServiceConfig service) throws ServiceException {
		this.service=service;
		introspect(service);
	}
	
	private void introspect(ServiceConfig service) throws ServiceException {
		try {
			Class clazz=Class.forName(service.getInterfaceName());
			Class current=clazz;
			while(current!=null && !current.equals(Object.class)) {
				Method[] meths=current.getDeclaredMethods();
				for(int i=0;i<meths.length;i++) {
					int modifiers=meths[i].getModifiers();
					if(Modifier.isPublic(modifiers)) {
						String name=meths[i].getName();
						methods.add(name);
					}
				}
				current=current.getSuperclass();
			}
		} catch (ClassNotFoundException x) {
			throw new ServiceException("Service introspection error",x);
		}
	}
	
	public Iterator<String> getMethods() {
		return methods.iterator();
	}
	
	public String toString() {
		StringBuffer sb=new StringBuffer();
		Iterator<String> it=methods.iterator();
		while(it.hasNext()) {
			String methName=it.next();
			sb.append(methName+"\n");
		}
		return sb.toString();
	}
	
}