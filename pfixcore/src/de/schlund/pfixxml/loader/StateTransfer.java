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

package de.schlund.pfixxml.loader;
 
import java.lang.reflect.*;
import java.util.*;
import org.apache.log4j.Category;

/**
 * StateTransfer.java 
 * 
 * Created: 29.04.2003
 * 
 * @author mleidig
 */
public class StateTransfer {
    
    private Category CAT=Category.getInstance(getClass().getName());
    private boolean debug;
    
    private static StateTransfer instance=new StateTransfer();

    //cache transferred AppLoader type objects (old -> new)
    private HashMap transferred=new HashMap();
    
    //cache scanned non AppLoader type objects
    private HashSet refs=new HashSet();
    
    //cache classes of transferred objects
    private HashSet appClasses=new HashSet();
    
    private ArrayList exceptions=new ArrayList();
    private int incType=-1;
    
    private ObjectBuilder builder;

    private StateTransfer() {
        debug=CAT.isDebugEnabled();
        try {
            builder=ObjectBuilder.getInstance();
        } catch(ObjectBuilderException x) {
            CAT.error("Can't transfer state.",x);
        }
    }

    public static StateTransfer getInstance() {
        return instance;
    }
    
    public void reset() {
        transferred.clear();
        refs.clear();
        appClasses.clear();
        exceptions.clear();
        incType=-1;
    }

    protected HashSet getAppClasses() {
        return appClasses;
    }

    public void addTransferred(Object oldObj,Object newObj) {
        transferred.put(new Integer(System.identityHashCode(oldObj)),newObj);
    }

    public Object getTransferred(Object oldObj) {
        return transferred.get(new Integer(System.identityHashCode(oldObj)));
    }
    
    public void addReferenced(Object obj) {
        refs.add(new Integer(System.identityHashCode(obj)));
    }
    
    public boolean isReferenced(Object obj) {
        return refs.contains(new Integer(System.identityHashCode(obj)));
    }
    
    public Object transfer(Object obj) {
        return transfer(obj,null);
    }

    protected Object transfer(Object oldObj,Object predecObj) {
        if(oldObj==null) return null;
        AppLoader loader=AppLoader.getInstance();
        ClassLoader theLoader=oldObj.getClass().getClassLoader();
        if(theLoader!=null && theLoader.equals(loader.getAppClassLoader())) return oldObj;
        //Transfer classes
        if(oldObj instanceof Class) {
        	Class oldClass=(Class)oldObj;
        	if(oldClass.getClassLoader() instanceof AppClassLoader) {
        		if(debug) CAT.debug("Transfer java.lang.Class instance of class '"+oldClass.getName()+"'.");
        		Object newObj=getTransferred(oldObj);
        		if(newObj!=null) return newObj;
        		try {
                    Class newClass=Class.forName(oldClass.getName(),false,loader.getAppClassLoader());
                    newObj=newClass;
                    addTransferred(oldClass,newClass);
                    appClasses.add(oldClass);
                    //Transfer static fields
                    while(oldClass!=null) {
                        Field[] fields=oldClass.getDeclaredFields();
                        int proc=0;
                        for(int i=0;i<fields.length;i++) {
                            if(Modifier.isStatic(fields[i].getModifiers())) {
                                String name=fields[i].getName();
                                Field field=null;
                                try {
                                    field=newClass.getDeclaredField(name);
                                    proc++;
                                    if(!Modifier.isStatic(field.getModifiers())) {
                                        addException(new StateTransferException(StateTransferException.MEMBER_TYPE_CHANGED,
                                            oldClass.getName(),"Member '"+fields[i].getName()+"' is no longer static."));
                                        continue;
                                    }
                                    if(debug) CAT.debug("Transfer static field '"+name+"' of class '"+oldClass.getName()+"'.");
                                    if(Modifier.isFinal(field.getModifiers())) {
                                        if(debug) CAT.debug("Final field -> set via JNI");
                                        if(Modifier.isStatic(field.getModifiers())) {
                                            setOBStaticField(newClass,oldClass,fields[i]);
                                        } else {
                                            setOBField(newObj,oldObj,fields[i]);    
                                        }
                                    } else {
                                        if(!fields[i].isAccessible()) fields[i].setAccessible(true);
                                        if(!field.isAccessible()) field.setAccessible(true);
                                        Object value=fields[i].get(null);
                                        value=transfer(value);
                                        field.set(null,value);
                                    }
                                } catch(NoSuchFieldException x) {
                                    addException(new StateTransferException(StateTransferException.MEMBER_REMOVED,
                                        oldClass.getName(),"Member '"+fields[i].getName()+"' was removed or renamed."));
                                } catch(IllegalAccessException x) {
                                    addException(new StateTransferException(StateTransferException.MEMBER_FINAL,
                                    oldClass.getName(), "Member '"+fields[i].getName()+"' is final."));
                                } catch(IllegalArgumentException x) {
                                    Class newType=field.getType();
                                    Class oldType=fields[i].getType();
                                    if(!newType.equals(oldType)) {
                                        addException(new StateTransferException(StateTransferException.MEMBER_TYPE_CHANGED,
                                            oldClass.getName(),"Member '"+fields[i].getName()+"' changed type from '"+oldType+"' to '"+newType+"'."));
                                    } else {
                                        addException(new StateTransferException(StateTransferException.MEMBER_TYPE_CONVERSION,
                                        oldClass.getName(),"Member value for '"+fields[i].getName()+"' can't be converted due to type mismatch."));
                                    }
                                }
                            }
                        }
                        Field[] newFields=newClass.getDeclaredFields();
                        if(proc<newFields.length) {
                            for(int i=0;i<newFields.length;i++) {
                                try {
                                    oldClass.getDeclaredField(newFields[i].getName());
                                } catch(NoSuchFieldException x) {
                                    addException(new StateTransferException(StateTransferException.MEMBER_ADDED,
                                        newClass.getName(),"Member '"+newFields[i].getName()+"' was added."));
                                }
                            }
                        }
                        oldClass=oldClass.getSuperclass();
                        newClass=newClass.getSuperclass();
                    }
                    return newObj;
                } catch(ClassNotFoundException x) {
                    addException(new StateTransferException(StateTransferException.CLASS_REMOVED,
                        oldClass.getName(),"Class was removed or renamed."));
                }
            }
        	return oldObj;
        }
        Class oldClass=oldObj.getClass();
        ClassLoader oldCL=oldClass.getClassLoader();
        if(oldCL instanceof AppClassLoader) {
            Object newObj=getTransferred(oldObj);
            if(newObj!=null) {return newObj;}
            if(debug) CAT.debug("Transfer instance of class '"+oldClass.getName()+"'."); 
            if(isObjectArray(oldClass)) {Object obj=transferArray(oldObj);return obj;} 
            Class newClass=null;
            try {
                newClass=loader.loadClass(oldClass.getName());
                newObj=createInstance(newClass,predecObj);
                appClasses.add(oldClass);
                addTransferred(oldObj,newObj);
                //refs.add(newObj);
                while(oldClass!=null) {
                    Field[] fields=oldClass.getDeclaredFields();
                    //iterate over fields
                    int proc=0;
                    for(int i=0;i<fields.length;i++) {
                        String name=fields[i].getName();
                        Field field=null;
                        try {
                            field=newClass.getDeclaredField(name);
                            proc++;
                            if(!fields[i].isAccessible()) fields[i].setAccessible(true);
                            //get field value
                            Object value=fields[i].get(oldObj);
                            if(debug) {
                                CAT.debug("Transfer field '"+name+"' of class '"+oldClass.getName()+"'.");
                            } 
                           	value=transfer(value,newObj);
                            int mod=field.getModifiers();
                            if(Modifier.isFinal(mod)) {
                                if(debug) CAT.debug("Final field -> set via JNI");
                                if(Modifier.isStatic(field.getModifiers())) {
                                    setOBStaticField(newClass,oldClass,fields[i]);
                                } else {
                                    setOBField(newObj,oldObj,fields[i]);    
                                }
                            } else {
                                if(!field.isAccessible()) field.setAccessible(true);     
                                field.set(newObj,value);
                            }
                        } catch(NoSuchFieldException x) {
                            addException(new StateTransferException(StateTransferException.MEMBER_REMOVED,
                                oldClass.getName(),"Member '"+fields[i].getName()+"' was removed or renamed."));
                        } catch(IllegalAccessException x) {
                            addException(new StateTransferException(StateTransferException.MEMBER_FINAL,
                               oldClass.getName(), "Member '"+fields[i].getName()+"' is final."));
                        } catch(IllegalArgumentException x) {
                            Class newType=field.getType();
                            Class oldType=fields[i].getType();
                            if(!newType.equals(oldType)) {
                                addException(new StateTransferException(StateTransferException.MEMBER_TYPE_CHANGED,
                                    oldClass.getName(),"Member '"+fields[i].getName()+"' changed type from '"+oldType+"' to '"+newType+"'."));
                            } else {
                                addException(new StateTransferException(StateTransferException.MEMBER_TYPE_CONVERSION,
                                    oldClass.getName(),"Member value for '"+fields[i].getName()+"' can't be converted due to type mismatch."));
                            }
                        }
                    }
                    Field[] newFields=newClass.getDeclaredFields();
                    if(proc<newFields.length) {
                        for(int i=0;i<newFields.length;i++) {
                            try {
                                oldClass.getDeclaredField(newFields[i].getName());
                            } catch(NoSuchFieldException x) {
                                addException(new StateTransferException(StateTransferException.MEMBER_ADDED,
                                    newClass.getName(),"Member '"+newFields[i].getName()+"' was added."));
                            }
                        }
                    }
                    oldClass=oldClass.getSuperclass();
                    newClass=newClass.getSuperclass();
                }
            } catch(ClassNotFoundException x) {
                addException(new StateTransferException(StateTransferException.CLASS_REMOVED,
                    oldClass.getName(),"Class was removed or renamed."));
            }
            return newObj;
        } else {
			if(!loader.needToTraverse(oldClass)) return oldObj;
            try {
                if(isReferenced(oldObj)) {return oldObj;} 
            } catch(NullPointerException x) {
                addException(new StateTransferException(StateTransferException.NULLHASH_EXCEPTION,oldObj.getClass().getName(),
                    "Can't locate object in hash due to NullPointerException. Possible reason: invalid 'hashcode()' implementation."));
                return oldObj;
            } 
            
            //check if object is a container and transfer its content
          
            addReferenced(oldObj);
            if(debug) CAT.debug("Transfer content from instance of class: "+oldObj.getClass().getName());
          
            //HashMaps and Hashtables hold special hash data, which has to be updated, when contained objects are reloaded,
            //therefore these objects have to be removed and added once again.
            //HashSets don't have do be handled this way, because they internally use HashMaps, which will get updated by the 
            //normal recursive transfer mechanism.
            if(oldObj instanceof Map)  {
                if(oldObj instanceof HashMap || oldObj instanceof Hashtable) return transferHashedMap((Map)oldObj);
            }
            
            /**
            Class[] interfaces=oldClass.getInterfaces();
            for(int j=0;j<interfaces.length;j++) {
                String name=interfaces[j].getName();
                if(name.equals("java.util.Collection")) {
                    oldObj=transferCollection((Collection)oldObj);
                }
                if(name.equals("java.util.Map")) {
                    oldObj=transferMap((Map)oldObj);
                }
            }
            */
            if(isObjectArray(oldClass)) {Object obj=transferArray(oldObj);return obj;} 
            //check if fields contains reloadable objects and transfer them
            while(oldClass!=null) {
                Field[] fields=oldClass.getDeclaredFields();
                //iterate over fields
                for(int i=0;i<fields.length;i++) {
                    String name=fields[i].getName();
                    try {
                        if(!fields[i].isAccessible()) fields[i].setAccessible(true);
                        //get field value
                        Object value=fields[i].get(oldObj);
                        if(debug) CAT.debug("Transfer field '"+name+"'.");
                        if(value!=null) {
                        	Object newValue=transfer(value,null);
                            if(newValue.getClass().getClassLoader() instanceof AppClassLoader) {
                            	fields[i].set(oldObj,newValue);
                            }
                        }
                    } catch(IllegalAccessException x) {
                        addException(new StateTransferException(StateTransferException.MEMBER_FINAL,
                           oldClass.getName(), "Member '"+fields[i].getName()+"' is final."));
                    }
                }    
                oldClass=oldClass.getSuperclass();
            }
            return oldObj;
        }
    }
                    
    /**
    public Class getContainerInterface(Class clazz) {
        Class[] classes=clazz.getInterfaces();
        for(int i=0;i<classes.length;i++) {
            Class itf=classes[i];
            if(itf.equals(Collection.class)) return itf;
            if(itf.equals(Map.class)) return itf;
            Class sub=getContainerInterface(itf);
            if(sub!=null) return sub;
        }
        Class sup=clazz.getSuperclass();
        if(sup!=null) {
            return getContainerInterface(sup);  
        }
        return null;
    }
                            
    public Collection transferCollection(Collection oldCol) {
        if(debug) CAT.debug("Transfer collection of type '"+oldCol.getClass().getName()+"'.");
        ArrayList tmpList=new ArrayList();
        boolean changed=false;
        Iterator it=oldCol.iterator();
        while(it.hasNext()) {
            Object val=it.next();
            Object newVal=transfer(val);
            if(!newVal.equals(val)) changed=true;
            tmpList.add(newVal);
        }
        if(changed) {
            oldCol.clear();
            it=tmpList.iterator();
            while(it.hasNext()) {
                Object val=it.next();
                oldCol.add(val);
            }
        }
        return oldCol;
    }
    */
                    
    public Map transferHashedMap(Map oldMap) {
        if(debug) CAT.debug("Transfer map of type '"+oldMap.getClass().getName()+"'.");
        HashMap tmpMap=new HashMap();
        Iterator it=oldMap.keySet().iterator();
        while(it.hasNext()) {
            Object key=it.next();
            Object val=oldMap.get(key);
            Object newKey=transfer(key,null);
            Object newVal=transfer(val,null);
            if(newKey!=null && newKey.getClass().getClassLoader() instanceof AppClassLoader) {
                it.remove();
                tmpMap.put(newKey,newVal);    
            } else if(newVal!=null && newVal.getClass().getClassLoader() instanceof AppClassLoader) {
                tmpMap.put(key,newVal);
            }
        }
        if(!tmpMap.isEmpty()) {
            it=tmpMap.keySet().iterator();
            while(it.hasNext()) {
                Object key=it.next();
                Object val=tmpMap.get(key);
                oldMap.put(key,val);
            }
        }
	return oldMap;
    }

    public Object transferArray(Object oldArray) {
        String className=oldArray.getClass().getName();
        if(debug) CAT.debug("Transfer array of type '"+className+"'.");
        Class comp=oldArray.getClass().getComponentType();
        String compName=comp.getName();
        
        if(oldArray.getClass().getClassLoader() instanceof AppClassLoader) {
            Class newClass=null;
            try {
                AppLoader loader=AppLoader.getInstance();
                newClass=loader.loadClass(compName);
                int len=Array.getLength(oldArray);        
                Object newArray=Array.newInstance(newClass,len);
                addTransferred(oldArray,newArray);
                for(int k=0;k<len;k++) {
                    Object val=Array.get(oldArray,k);
                    if(val!=null) {
                        Object newVal=transfer(val,null);
                        Array.set(newArray,k,newVal);
                    }
                }
                return newArray;
            } catch(ClassNotFoundException x) {
                addException(new StateTransferException(StateTransferException.CLASS_REMOVED,
                newClass.getName(),"Class was removed or renamed."));
            } catch(Exception x) {
                addException(new StateTransferException(StateTransferException.UNHANDLED_EXCEPTION,oldArray.getClass().getName(),x));
            }
        } else {
            int len=Array.getLength(oldArray);        
            for(int k=0;k<len;k++) {
                Object val=Array.get(oldArray,k);
                if(val!=null) {
                    Object newVal=transfer(val,null);
                    //if(val.hashCode()!=newVal.hashCode()) Array.set(oldArray,k,val);
                    if(newVal.getClass().getClassLoader() instanceof AppClassLoader) Array.set(oldArray,k,newVal);
                }
            }
            //refs.add(oldArray);
            return oldArray;
        }
        return oldArray;
    }

    protected boolean isObjectArray(Class clazz) {
        if(clazz.isArray()) {
            String name=clazz.getName();
            int ind=name.lastIndexOf('[');
            if(name.charAt(ind+1)=='L') return true; 
        }
        return false;
    }

    protected Object createInstance(Class clazz,Object predecObj) {
        Object obj=builder.allocateObject(clazz);
        return obj;
    }

    //exceptions
    
    protected void addException(StateTransferException ste) {
        exceptions.add(ste);
        int type=getInconsistencyType(ste);
        if(type==AppLoader.INCONSISTENCY_POSSIBLE) {
            if(debug) CAT.debug(ste);
        } else {
            CAT.warn(ste);
        }
        if(incType!=AppLoader.INCONSISTENCY_PROBABLE) {
            incType=type;
        } 
    }

    protected Iterator getExceptions() {
        return exceptions.iterator();
    }

    protected Iterator getExceptions(int type) {
        ArrayList al=new ArrayList();
        Iterator it=getExceptions();
        while(it.hasNext()) {
            StateTransferException ste=(StateTransferException)it.next();
            if(getInconsistencyType(ste)==type) al.add(ste);
        }
        return al.iterator();
    }

    //inconsistency types

    protected int getInconsistencyType() {
        return incType;
    }

    protected int getInconsistencyType(StateTransferException ste) {
        int type=ste.getType();
        if(type==StateTransferException.MEMBER_FINAL || type==StateTransferException.NULLHASH_EXCEPTION) 
            return AppLoader.INCONSISTENCY_POSSIBLE;
        return AppLoader.INCONSISTENCY_PROBABLE;
    }

    protected void setOBField(Object newObj,Object oldObj,Field oldFld) throws IllegalAccessException {
        String name=oldFld.getName();
        String sign=builder.getTypeSignature(oldFld);
        if(!oldFld.isAccessible()) oldFld.setAccessible(true);
        char start=sign.charAt(0);
        if(start=='[') {
            Object value=oldFld.get(oldObj);
            value=transfer(value,null);
            builder.setObjectField(newObj,name,sign,value);
        } else {
            if(start=='L') {
                Object value=oldFld.get(oldObj);
                value=transfer(value,null);
                builder.setObjectField(newObj,name,sign,value);
            } else if(start=='Z') {
                builder.setBooleanField(newObj,name,sign,oldFld.getBoolean(oldObj));
            } else if(start=='B') {
                builder.setByteField(newObj,name,sign,oldFld.getByte(oldObj));
            } else if(start=='C') {
                builder.setCharField(newObj,name,sign,oldFld.getChar(oldObj));
            } else if(start=='S') {
                builder.setShortField(newObj,name,sign,oldFld.getShort(oldObj));
            } else if(start=='I') {
                builder.setIntField(newObj,name,sign,oldFld.getInt(oldObj));
            } else if(start=='J') {
                builder.setLongField(newObj,name,sign,oldFld.getLong(oldObj));
            } else if(start=='F') {
                builder.setFloatField(newObj,name,sign,oldFld.getFloat(oldObj));
            } else if(start=='D') {
                builder.setDoubleField(newObj,name,sign,oldFld.getDouble(oldObj));
            }
        }
    }
    
    protected void setOBStaticField(Class newClass,Class oldClass,Field oldFld) throws IllegalAccessException {
        String name=oldFld.getName();
        String sign=builder.getTypeSignature(oldFld);
        if(!oldFld.isAccessible()) oldFld.setAccessible(true);
        char start=sign.charAt(0);
        if(start=='[') {
            Object value=oldFld.get(null);
            value=transfer(value,null);
            builder.setStaticObjectField(newClass,name,sign,value);
        } else {
            if(start=='L') {
                Object value=oldFld.get(null);
                value=transfer(value,null);
                builder.setStaticObjectField(newClass,name,sign,oldFld.get(null));
            } else if(start=='Z') {
                builder.setStaticBooleanField(newClass,name,sign,oldFld.getBoolean(null));
            } else if(start=='B') {
                builder.setStaticByteField(newClass,name,sign,oldFld.getByte(null));
            } else if(start=='C') {
                builder.setStaticCharField(newClass,name,sign,oldFld.getChar(null));
            } else if(start=='S') {
                builder.setStaticShortField(newClass,name,sign,oldFld.getShort(null));
            } else if(start=='I') {
                builder.setStaticIntField(newClass,name,sign,oldFld.getInt(null));
            } else if(start=='J') {
                builder.setStaticLongField(newClass,name,sign,oldFld.getLong(null));
            } else if(start=='F') {
                builder.setStaticFloatField(newClass,name,sign,oldFld.getFloat(null));
            } else if(start=='D') {
                builder.setStaticDoubleField(newClass,name,sign,oldFld.getDouble(null));
            }
        }
    }

}
