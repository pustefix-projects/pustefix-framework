package de.schlund.pfixxml.loader;

import java.lang.reflect.*;
import java.util.HashMap;

import org.apache.log4j.Category;

public class ObjectBuilder {
    
    private Category CAT=Category.getInstance(getClass().getName());
    
    private static ObjectBuilder instance=new ObjectBuilder();
    private static boolean loaded;
    private static boolean hasError;
    private static ObjectBuilderException error;
    
    private HashMap typeMap;
    
    public boolean hasError() {
        return hasError;
    }
    
    public ObjectBuilderException getError() {
        return error;
    }
    
    private synchronized void load() {
        if(!loaded) {
            CAT.info("Loading shared library 'apploader'.");
            if(!AppLoader.getInstance().isEnabled()) {
                //don't load library, when AppLoader isn't enabled
                hasError=true;
                error=new ObjectBuilderException("Security only allows loading of shared library 'apploader' when AppLoader is enabled.");
                CAT.error(error);
            } else {
                //load library, when AppLoader is enabled
                try {
                    System.loadLibrary("apploader");
                } catch(UnsatisfiedLinkError x) {
                    hasError=true;
                    error=new ObjectBuilderException("Error while trying to load shared library 'apploader'.",x);
                    CAT.error(error,x);
                } 
            }
            initTypeSignatures();
            loaded=true;
        }
    }
    
    private void initTypeSignatures() {
        typeMap=new HashMap();
        typeMap.put("boolean","Z");
        typeMap.put("byte","B");
        typeMap.put("char","C");
        typeMap.put("short","S");
        typeMap.put("int","I");
        typeMap.put("long","J");
        typeMap.put("float","F");
        typeMap.put("double","D");
    }
    
    public static ObjectBuilder getInstance() throws ObjectBuilderException {
        if(!loaded) instance.load();
        if(hasError) throw error;
        return instance;
    }
    
    public String getTypeSignature(Field fld) {
        String name=fld.getType().getName();
        if(name.charAt(0)=='[') {
            int ind=name.lastIndexOf('[');
            char id=name.charAt(ind+1);
            if(id=='L') {
                name=name.replace('.','/');
            }
        } else {
            String type=(String)typeMap.get(name);
            if(type!=null) {
                name=type;
            } else {
                name=name.replace('.','/');
                name="L"+name+";";    
            }
        }
        return name;
    }
       
    public native Object allocateObject(Class clazz);
    
    public native void setObjectField(Object obj,String name,String sign,Object value);
    public native void setBooleanField(Object obj,String name,String sign,boolean value);
    public native void setByteField(Object obj,String name,String sign,byte value);
    public native void setCharField(Object obj,String name,String sign,char value);
    public native void setShortField(Object obj,String name,String sign,short value);
    public native void setIntField(Object obj,String name,String sign,int value);
    public native void setLongField(Object obj,String name,String sign,long value);
    public native void setFloatField(Object obj,String name,String sign,float value);
    public native void setDoubleField(Object obj,String name,String sign,double value);
    
    public native void setStaticObjectField(Class clazz,String name,String sign,Object value);
    public native void setStaticBooleanField(Class clazz,String name,String sign,boolean value);
    public native void setStaticByteField(Class clazz,String name,String sign,byte value);
    public native void setStaticCharField(Class clazz,String name,String sign,char value);
    public native void setStaticShortField(Class clazz,String name,String sign,short value);
    public native void setStaticIntField(Class clazz,String name,String sign,int value);
    public native void setStaticLongField(Class clazz,String name,String sign,long value);
    public native void setStaticFloatField(Class clazz,String name,String sign,float value);
    public native void setStaticDoubleField(Class clazz,String name,String sign,double value);
    
}