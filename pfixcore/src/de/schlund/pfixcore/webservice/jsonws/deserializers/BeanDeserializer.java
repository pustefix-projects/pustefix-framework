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

import java.lang.reflect.Method;
import java.util.Iterator;

import de.schlund.pfixcore.webservice.json.JSONObject;
import de.schlund.pfixcore.webservice.jsonws.BeanDescriptor;
import de.schlund.pfixcore.webservice.jsonws.BeanDescriptorFactory;
import de.schlund.pfixcore.webservice.jsonws.DeserializationContext;
import de.schlund.pfixcore.webservice.jsonws.DeserializationException;
import de.schlund.pfixcore.webservice.jsonws.Deserializer;

public class BeanDeserializer extends Deserializer {

    BeanDescriptorFactory beanDescFactory;
    
    public BeanDeserializer(BeanDescriptorFactory beanDescFactory) {
        this.beanDescFactory=beanDescFactory;
    }
    
    @Override
    public boolean canDeserialize(DeserializationContext ctx, Object jsonValue, Class<?> targetClass) {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public Object deserialize(DeserializationContext ctx,Object jsonValue,Class<?> targetClass) throws DeserializationException {
        if(jsonValue instanceof JSONObject) {
            try {
                JSONObject jsonObj=(JSONObject)jsonValue;
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
                        Class propTargetClass=bd.getPropertyType(prop);
                        Object val=jsonObj.getMember(prop);
                        Method meth=bd.getSetMethod(prop);
                        if(val==null) {
                           meth.invoke(newObj,new Object[] {null}); 
                        } else {
                            Object res=ctx.deserialize(val,propTargetClass);
                            if(res!=null) meth.invoke(newObj,res);
                        }
                    }
                }
                return newObj;
            } catch(Exception x) {
                throw new DeserializationException("Error while serializing bean.",x);
            }
        } else throw new DeserializationException("No instance of JSONObject: "+jsonValue.getClass().getName());
     
    }
    
}
