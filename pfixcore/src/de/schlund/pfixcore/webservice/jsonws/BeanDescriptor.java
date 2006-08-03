/*
 * Created on 08.10.2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.webservice.jsonws;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class BeanDescriptor {

	Class clazz;
	ArrayList<String> propertyNames=new ArrayList<String>();
	HashMap<String,Method> getters=new HashMap<String,Method>();
	HashMap<String,Method> setters=new HashMap<String,Method>();
    HashMap<String,Class> types=new HashMap<String,Class>();
	
	public BeanDescriptor(Class clazz) {
		this.clazz=clazz;
		introspect(clazz);
	}
	
	private void introspect(Class clazz) {
		Method[] methods=clazz.getMethods();
		for(int i=0;i<methods.length;i++) {
			int modifiers=methods[i].getModifiers();
			if(Modifier.isPublic(modifiers)&&!Modifier.isStatic(modifiers)) {
				String name=methods[i].getName();
				if(name.length()>3&&Character.isUpperCase(name.charAt(3))) {
					if(name.startsWith("get")) {
						String propName=extractPropertyName(name);
						getters.put(propName,methods[i]);
					} else if(name.startsWith("set")) {
						if(methods[i].getReturnType()==void.class) {
							String propName=extractPropertyName(name);
							setters.put(propName,methods[i]);
						}
					}
				}
			}
		}
		Iterator<String> it=getters.keySet().iterator();
		while(it.hasNext()) {
			String propName=it.next();
			Method method=setters.get(propName);
			if(method==null) it.remove();
			else {
                propertyNames.add(propName);
                Method getMeth=getters.get(propName);
                types.put(propName,getMeth.getReturnType());
            }
		}
	}
    
    public List<String> getPropertyNames() {
        return propertyNames;
    }
	
	private String extractPropertyName(String methodName) {
		String name=methodName.substring(3);
		if(name.length()>1&&Character.isUpperCase(name.charAt(0))&&
				Character.isUpperCase(name.charAt(1))) return name;
		return Character.toLowerCase(name.charAt(0))+name.substring(1);
	}
	
	public Method getSetMethod(String propName) {
		return setters.get(propName);
	}
	
	public Method getGetMethod(String propName) {
		return getters.get(propName);
	}
    
    public Class getPropertyType(String propName) {
        return types.get(propName);
    }
	
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append("Class:\n");
		sb.append("\t"+clazz.getName()+"\n");
		sb.append("Properties:\n");
		Iterator<String> it=propertyNames.iterator();
		while(it.hasNext()) {
			String propName=it.next();
			sb.append("\t"+propName+"\n");
		}
		return sb.toString();
	}

}
