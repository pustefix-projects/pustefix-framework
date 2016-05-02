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
package de.schlund.pfixcore.generator.iwrpgen;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.generator.annotation.Param;
import de.schlund.pfixcore.generator.annotation.Transient;

/**
 * @author mleidig@schlund.de
 */
public class BeanDescriptor {

    final static Logger LOG=Logger.getLogger(BeanDescriptor.class);
    
	Class<?> clazz;
	
    HashMap<String,Type> types=new HashMap<String,Type>();
    HashMap<String,Method> getters=new HashMap<String,Method>();
	HashMap<String,Method> setters=new HashMap<String,Method>();
    HashMap<String,Field> directFields=new HashMap<String,Field>();

	public <T> BeanDescriptor(Class<T> clazz) {
		this.clazz=clazz;
		introspect(clazz);
	}
	
	public Class<?> getBeanClass() {
	    return clazz;
	}
	
    private <T> void introspect(Class<T> clazz) {
        Field[] fields=clazz.getFields();
        for(int i=0;i<fields.length;i++) {
            if(!Modifier.isStatic(fields[i].getModifiers())&&!Modifier.isFinal(fields[i].getModifiers())) {
                Method getter=null;
                try {
                    getter=clazz.getMethod(createGetterName(fields[i].getName()),new Class[0]);
                    if(getter!=null && (Modifier.isStatic(getter.getModifiers()) || getter.getReturnType()==void.class)) getter=null;
                } catch(NoSuchMethodException x) {}
                if(getter==null) {
                    String origPropName=fields[i].getName();
                    String propName=origPropName;
                    boolean isExcluded=false;
                    Transient ex=fields[i].getAnnotation(Transient.class);
                    if(ex!=null) isExcluded=true;
                    Param param=fields[i].getAnnotation(Param.class);
                    if(param!=null&&!param.name().equals("")) propName=param.name();
                    if(!isExcluded) {
                        if(types.get(propName)!=null) throw new RuntimeException("Duplicate bean property name: "+propName);
                        types.put(propName,fields[i].getGenericType());
                        directFields.put(propName,fields[i]);
                    }
                } else if(getter.getReturnType()!=fields[i].getType()) {
                    if(LOG.isDebugEnabled()) LOG.debug("Ignore public field '"+fields[i].getName()+"' cause getter with different "+
                            "return type found: "+getter.getReturnType().getName()+" -> "+fields[i].getType().getName());
                }
            }    
        }
        Method[] methods=clazz.getMethods();
        for(int i=0;i<methods.length;i++) {
            if(methods[i].getDeclaringClass()!=Object.class) {
                if(!Modifier.isStatic(methods[i].getModifiers())) {
                    String name=methods[i].getName();
                    if (methods[i].getParameterTypes().length == 0 && 
                            ((name.length() > 3 && Character.isUpperCase(name.charAt(3)) && name.startsWith("get")) || 
                            (name.length() > 2 && Character.isUpperCase(name.charAt(2)) && name.startsWith("is")))) {
                        String origPropName=extractPropertyName(name);
                        String propName=origPropName;
                        boolean isExcluded=false;
                        Field field=null;
                        try {
                            field=clazz.getField(origPropName);
                            if(field!=null && (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()))) field=null;
                        } catch(NoSuchFieldException x) {}
                        Transient ex=methods[i].getAnnotation(Transient.class);
                        if(ex==null&&field!=null) ex=field.getAnnotation(Transient.class);
                        if(ex!=null) isExcluded=true;
                        Param param=methods[i].getAnnotation(Param.class);
                        if(param==null&&field!=null) param=field.getAnnotation(Param.class);
                        if(param!=null&&!param.name().equals("")) propName=param.name();
                        if(!isExcluded) {
                            if(getters.get(propName)!=null) throw new RuntimeException("Duplicate bean property name: "+propName);
                            Method setter=null;
                            try {
                                setter=clazz.getMethod(createSetterName(origPropName),new Class[] {methods[i].getReturnType()});
                                if(setter.getReturnType()!=void.class) setter=null;
                            } catch(NoSuchMethodException x) {}
                            if(setter!=null) {
                                setters.put(propName,setter);
                                getters.put(propName,methods[i]);
                                types.put(propName,methods[i].getGenericReturnType());
                            }
                        }   
                    } 
                }
            }
        }   
    }
    
    private String extractPropertyName(String methodName) {
        String name = "";
        if (methodName.startsWith("is") && Character.isUpperCase(methodName.charAt(2)) && methodName.length() > 2) {
            name = methodName.substring(2);
        } else if (methodName.startsWith("get") && Character.isUpperCase(methodName.charAt(3)) && methodName.length() > 3) {
            name = methodName.substring(3);
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1))) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
    
    private String createSetterName(String propName) {
        return "set"+Character.toUpperCase(propName.charAt(0))+propName.substring(1);
    }
    
    private String createGetterName(String propName) {
        return "get"+Character.toUpperCase(propName.charAt(0))+propName.substring(1);
    }
    
    
    public Set<String> getProperties() {
        return types.keySet();
    }
    
	public Method getSetMethod(String propName) {
		return setters.get(propName);
	}
	
	public Method getGetMethod(String propName) {
		return getters.get(propName);
	}
    
    public Field getDirectAccessField(String propName) {
        return directFields.get(propName);
    }
    
    public Type getPropertyType(String propName) {
        return types.get(propName);
    }
	
    @Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append("Class:\n");
		sb.append("\t"+clazz.getName()+"\n");
		sb.append("Properties:\n");
		Iterator<String> it=getProperties().iterator();
		while(it.hasNext()) {
			String propName=it.next();
			sb.append("\t"+propName+"\n");
		}
		return sb.toString();
	}
    
}
