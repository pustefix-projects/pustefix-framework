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
import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
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
    
    private ArrayList exceptions=new ArrayList();
    private int incType=-1;

    private StateTransfer() {
        initVersionDeps();
        debug=CAT.isDebugEnabled();
    }

    public static StateTransfer getInstance() {
        return instance;
    }
    
    public void reset() {
        transferred.clear();
        refs.clear();
        exceptions.clear();
        incType=-1;
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

    public Object transfer(Object oldObj) {
        if(oldObj==null) return null;
        Class oldClass=oldObj.getClass();
        ClassLoader oldCL=oldClass.getClassLoader();
        AppLoader loader=AppLoader.getInstance();
        if(oldCL instanceof AppClassLoader) {
            Object newObj=getTransferred(oldObj);
            if(newObj!=null) {return newObj;}
            if(debug) CAT.debug("Transfer instance of class '"+oldClass.getName()+"'."); 
            if(isObjectArray(oldClass)) {Object obj=transferArray(oldObj);return obj;} 
            Class newClass=null;
            try {
                newClass=loader.loadClass(oldClass.getName());
                newObj=createInstance(newClass);
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
                                CAT.debug("Transfer field '"+name+"' ('"+value+"') of class '"+oldClass.getName()+"'.");
                            } 
                            //check if field value defined
                            if(value!=null) {value=transfer(value);} 
                            if(!field.isAccessible()) field.setAccessible(true);     
                            field.set(newObj,value);
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
                            if(loader.needToTraverse(value.getClass())) {
                                Object newValue=transfer(value);
                                if(newValue.getClass().getClassLoader() instanceof AppClassLoader) {
                                    fields[i].set(oldObj,newValue);
                                }
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
            Object newKey=transfer(key);
            Object newVal=transfer(val);
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
                        Object newVal=transfer(val);
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
                    Object newVal=transfer(val);
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

    protected Object createInstance(Class clazz) {
        Class decClazz=clazz.getDeclaringClass();
        Constructor con=null;
        Object obj=null;
        try {
            if(decClazz!=null) {
                con=clazz.getDeclaredConstructor(new Class[] {decClazz});
                con.setAccessible(true);
                //hack or not? is instance of outer class needed?
                obj=con.newInstance(new Object[] {null});
            } else {
                con=clazz.getDeclaredConstructor(null);
                con.setAccessible(true);
                obj=con.newInstance(null);
            }
        } catch(NoSuchMethodException x) {
            if(debug) CAT.debug("Class '"+clazz.getName()+"' hasn't empty or default constructor. Allocate object with native method.");
            return allocateNewObject(clazz);
        } catch(Exception x) {
            addException(new StateTransferException(StateTransferException.UNHANDLED_EXCEPTION,clazz.getName(),x));
        }
        return obj;
    }

    //java vendor/version dependant code for creating instances of classes which don't have a no-arg constructor
    
    final int SUN_IBM_1_3=0;
    final int SUN_IBM_1_4=1;
    int javaVersion;
    
    protected void initVersionDeps() {
        String version=System.getProperty("java.version").toLowerCase();
        String vendor=System.getProperty("java.vendor").toLowerCase();
        if(!(vendor.startsWith("sun") || vendor.startsWith("ibm"))) {
            CAT.warn("StateTransfer doesn't support Java vendor '"+vendor+"'. Try to use settings for 'Sun/IBM'.");
            vendor="sun";
        }
        if(!(version.startsWith("1.3") || version.startsWith("1.4"))) {
            CAT.warn("StateTransfer doesn't support Java version '"+version+"'. Try to use settings for '1.4.x'.");
            version="1.4";
        }
        if(version.startsWith("1.3")) {
            javaVersion=SUN_IBM_1_3;           
        } else if(version.startsWith("1.4")) {
            javaVersion=SUN_IBM_1_4;
        }
        if(javaVersion==SUN_IBM_1_4) {
            try { 
                Class c=Class.forName(refFacClass);
                PrivilegedAction pa=(PrivilegedAction)c.newInstance();
                refFac=AccessController.doPrivileged(pa);
            } catch(Exception x) {
                CAT.error("StateTransfer can't be initialized for Java version '1.4.x' of vendor 'Sun/IBM'. Try to use settings for 'Sun/IBM 1.3.x'.",x);
                javaVersion=SUN_IBM_1_3;
            }
        }
    }
    
    protected Object allocateNewObject(Class clazz) {
        if(javaVersion==SUN_IBM_1_3) {
            return allocateNewObjectNative(clazz);
        } else {
            return allocateNewObjectSuper(clazz);
        }
    }
    
    //version: 1.3.x
    //vendor: Sun, IBM
    
    protected Object allocateNewObjectNative(Class clazz) {
        try {
            Class ois=ObjectInputStream.class;
            Method meth=ois.getDeclaredMethod("allocateNewObject",new Class[] {Class.class,Class.class});
            meth.setAccessible(true);
            Object obj=meth.invoke(null,new Object[] {clazz,Object.class});
            return obj;
        } catch(Exception x) {
            addException(new StateTransferException(StateTransferException.UNHANDLED_EXCEPTION,clazz.getName(),x));
        }
        return null;
    }
    
    //version: 1.4.x
    //vendor: Sun, IBM
    
    String refFacClass="sun.reflect.ReflectionFactory$GetReflectionFactoryAction";
    Object refFac;
    
    protected Object allocateNewObjectSuper(Class c) {
        try {
            Constructor con=getNoArgConstructor(c);
            Class params[]=new Class[] {Class.class,Constructor.class};
            Method meth=refFac.getClass().getDeclaredMethod("newConstructorForSerialization",params);
            Object args[]=new Object[] {c,con};
            con=(Constructor)meth.invoke(refFac,args);
            Object obj=con.newInstance(new Class[0]);
            return obj;
        } catch(Exception x) {
            addException(new StateTransferException(StateTransferException.UNHANDLED_EXCEPTION,c.getName(),x));
        }
        return null;
    }

    protected Constructor getNoArgConstructor(Class c) {
        try {
            Constructor con=c.getDeclaredConstructor(new Class[0]);
            int mods=con.getModifiers();
            if(!Modifier.isPrivate(mods)) return con;
        } catch(NoSuchMethodException x) {}
        Class sc=c.getSuperclass();
        return getNoArgConstructor(sc);
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

}
