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

package de.schlund.pfixcore.webservice.jsonws.deserializers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.schlund.pfixcore.beans.BeanDescriptor;
import de.schlund.pfixcore.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.webservice.json.JSONObject;
import de.schlund.pfixcore.webservice.jsonws.DeserializationContext;
import de.schlund.pfixcore.webservice.jsonws.DeserializationException;
import de.schlund.pfixcore.webservice.jsonws.Deserializer;

/**
 * @author mleidig@schlund.de
 */
public class BeanDeserializer extends Deserializer {

    BeanDescriptorFactory beanDescFactory;
    
    public BeanDeserializer(BeanDescriptorFactory beanDescFactory) {
        this.beanDescFactory=beanDescFactory;
    }
    
    @Override
    public boolean canDeserialize(DeserializationContext ctx, Object jsonValue, Type targetType) {
        if(jsonValue instanceof JSONObject) {
            JSONObject jsonObj=(JSONObject)jsonValue;
            Class<?> targetClass=null;
            if(targetType instanceof Class) targetClass=(Class<?>)targetType;
            else if(targetType instanceof ParameterizedType) {
                Type rawType=((ParameterizedType)targetType).getRawType();
                if(rawType instanceof Class) targetClass=(Class<?>)rawType;
                else return false;
            }
            if(Map.class.isAssignableFrom(targetClass)) return true;
            else {
                String className=jsonObj.getStringMember("javaClass");
                if(className!=null) {
                    try {
                        Class<?> clazz=Class.forName(className);
                        if(targetClass!=null && !targetClass.isAssignableFrom(clazz)) return false;
                        targetClass=clazz;
                    } catch(ClassNotFoundException x) {
                        return false;
                    }
                }
                if(isInstantiable(targetClass)) return true;
            }
        }
        return false;
    }
    
    @Override
    public Object deserialize(DeserializationContext ctx,Object jsonValue,Type targetType) throws DeserializationException {
      
        if(jsonValue instanceof JSONObject) {
            
            JSONObject jsonObj=(JSONObject)jsonValue;
            
            Class<?> targetClass=null;
            if(targetType instanceof Class) targetClass=(Class<?>)targetType;
            else if(targetType instanceof ParameterizedType) {
                Type rawType=((ParameterizedType)targetType).getRawType();
                if(rawType instanceof Class) targetClass=(Class<?>)rawType;
                else throw new DeserializationException("Type not supported: "+targetType);
            }
            
            if(Map.class.isAssignableFrom(targetClass)) {

                Type valType=null;
                if(targetType instanceof ParameterizedType) {
                    ParameterizedType paramType=(ParameterizedType)targetType;
                    Type[] argTypes=paramType.getActualTypeArguments();
                    if(argTypes.length==2) {
                        Type keyType=argTypes[0];
                        if(keyType==String.class) {
                            valType=argTypes[1];
                        } else throw new DeserializationException("Unsupported Map key type (must be java.lang.String): "+keyType);
                    } else throw new DeserializationException("Type not supported: "+targetType);
                } else throw new DeserializationException("Deserialization of unparameterized Map types isn't supported: "+targetType);
                  
                Map<Object, Object> map=null;
                if(!targetClass.isInterface()) {
                    try {
                        map=(Map<Object, Object>)targetClass.newInstance();
                    } catch(Exception x) {}
                }
                if(map==null) {
                    if(targetClass.isAssignableFrom(HashMap.class)) {
                        map=new HashMap<Object, Object>();
                    } else throw new DeserializationException("Can't create instance of class '"+targetClass.getName()+"'.");
                }
                
                Iterator<String> it=jsonObj.getMemberNames();
                while(it.hasNext()) {
                    String prop=it.next();
                    if(!prop.equals("javaClass")) {
                        Object res=ctx.deserialize(jsonObj.getMember(prop),valType);
                        map.put(prop,res);
                    } 
                }
                       
                return map;
                
            } else {
            
                try {
                    
                    String className=jsonObj.getStringMember("javaClass");
                    if(className!=null) {
                        Class<?> clazz=Class.forName(className);
                        if(targetClass!=null && !targetClass.isAssignableFrom(clazz)) 
                            throw new DeserializationException("Class '"+targetClass.getName()+"' isn't assignable from '"+clazz.getName());
                        targetClass=clazz;
                    }
                    BeanDescriptor bd=beanDescFactory.getBeanDescriptor(targetClass);
                
                    Object newObj=targetClass.newInstance();
                    Iterator<String> it=jsonObj.getMemberNames();
                    while(it.hasNext()) {
                        String prop=it.next();
                        if(!prop.equals("javaClass")) {
                            Type propTargetType=bd.getPropertyType(prop);
                            if(propTargetType!=null) {
                                Object val=jsonObj.getMember(prop);
                                Method meth=bd.getSetMethod(prop);
                                if(meth!=null) {
                                    if(val==null) {
                                        meth.invoke(newObj,new Object[] {null}); 
                                    } else {
                                        Object res=ctx.deserialize(val,propTargetType);
                                        if(res!=null) meth.invoke(newObj,res);
                                    }
                                } else {
                                    Field field=bd.getDirectAccessField(prop);
                                    if(field!=null) {
                                        if(val==null) {
                                            field.set(newObj,null);
                                        } else {
                                            Object res=ctx.deserialize(val,propTargetType);
                                            if(res!=null) field.set(newObj,res);
                                        }
                                    } else throw new DeserializationException("Bean of type '"+targetClass.getName()+"' doesn't "+
                                            " have setter method or direct access to property '"+prop+"'.");
                                }
                            } else throw new DeserializationException("Bean of type '"+targetClass.getName()+"' doesn't have property '"+prop+"'.");         
                        }
                    }
                    return newObj;
                } catch(Exception x) {
                    if(x instanceof DeserializationException) throw (DeserializationException)x;
                    throw new DeserializationException("Can't deserialize as bean of type '"+targetClass.getName()+"'.",x);
                }
            }
        } else throw new DeserializationException("No instance of JSONObject: "+jsonValue.getClass().getName());
     
    }
    
    private boolean isInstantiable(Class<?> clazz) {
        if(clazz.isInterface()) return false;
        try {
            clazz.getConstructor(new Class[0]);
        } catch(NoSuchMethodException x) {
            return false;
        }
        return true;
    }
    
}
