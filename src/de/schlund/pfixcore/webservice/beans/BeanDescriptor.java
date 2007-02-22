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
package de.schlund.pfixcore.webservice.beans;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import de.schlund.pfixcore.example.webservices.DataBean;

/**
 * Bean property descriptor for bean classes.
 * Introspects a class to find all bean properties by its getter and setter methods.
 * 
 * @author mleidig@schlund.de
 */
public class BeanDescriptor {

	Class clazz;
	
    HashMap<String,Class> types=new HashMap<String,Class>();
    HashMap<String,Method> getters=new HashMap<String,Method>();
	HashMap<String,Method> setters=new HashMap<String,Method>();

	public <T> BeanDescriptor(Class<T> clazz) {
		this.clazz=clazz;
		introspectNew(clazz);
	}
	
    private <T> void introspectNew(Class<T> clazz) {
        Method[] methods=clazz.getMethods();
        for(int i=0;i<methods.length;i++) {
            if(methods[i].getDeclaringClass()!=Object.class) {
                int modifiers=methods[i].getModifiers();
                if(Modifier.isPublic(modifiers)&&!Modifier.isStatic(modifiers)) {
                    String name=methods[i].getName();
                    if(name.length()>3&&Character.isUpperCase(name.charAt(3))) {
                        if(name.startsWith("get") && methods[i].getParameterTypes().length==0) {
                            boolean isTransient=false;
                            Include prop=methods[i].getAnnotation(Include.class);
                            Exclude trans=methods[i].getAnnotation(Exclude.class);
                            if(trans==null) {
                                ExcludeByDefault defTrans=methods[i].getDeclaringClass().getAnnotation(ExcludeByDefault.class);
                                if(defTrans!=null && prop==null) isTransient=true;
                            } else isTransient=true;
                            if(!isTransient) {
                                String propName=extractPropertyName(name);
                                getters.put(propName,methods[i]);
                                types.put(propName,methods[i].getReturnType());
                            }
                        } else if(name.startsWith("set")) {
                            if(methods[i].getReturnType()==void.class && methods[i].getParameterTypes().length==1) {
                                String propName=extractPropertyName(name);
                                setters.put(propName,methods[i]);
                            }
                        }
                    }
                }
            }
        }   
    }
    
    public Set<String> getReadableProperties() {
        return getters.keySet();
    }
    
    public Set<String> getWritableProperties() {
        return setters.keySet();
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
		Iterator<String> it=getReadableProperties().iterator();
		while(it.hasNext()) {
			String propName=it.next();
			sb.append("\t"+propName+"\n");
		}
		return sb.toString();
	}
    
    public static void main(String[] args) {
        BeanDescriptor desc=new BeanDescriptor(DataBean.class); 
        System.out.println(desc);
    }

}
