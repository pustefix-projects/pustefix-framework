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

import COM.rsa.jsafe.*;
import java.io.*;
import java.lang.ClassLoader;
import java.net.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * AppClassLoader.java 
 * 
 * Created: 29.04.2003
 * 
 * @author mleidig
 */
public class AppClassLoader extends java.lang.ClassLoader {

    private Category    CAT = Category.getInstance(getClass().getName());
    private boolean     debug;
    private ClassLoader parent;
    private HashMap     modTimes = new HashMap();
    private HashSet appClasses=new HashSet();
    
    public AppClassLoader(java.lang.ClassLoader parent) {
        super(parent);
        this.parent=parent;
        debug = CAT.isDebugEnabled();
        AppLoader.getInstance().addToHistory(this,"Created");
    }

	public void finalize() {
		AppLoader.getInstance().addToHistory(this,"Finalized");
	}

    protected HashSet getAppClasses() {
        return appClasses;
    }

    public synchronized Class loadClass(String name) throws ClassNotFoundException {

        AppLoader loader=AppLoader.getInstance();
        //load from cache
        if(debug) CAT.debug("Try to load from cache: "+name);
        Class c=findLoadedClass(name);
        if(c!=null) {
            if(debug) CAT.debug("Cache contains class: "+name);
            return c;
        } else {
            if(debug) CAT.debug("Cache doesn't contain class: "+name);
        }

        //load from system
        try {
            if(debug) CAT.debug("Try to load with system classloader: "+name);
            c=findSystemClass(name);
            if(debug) CAT.debug("System classloader found class: "+name);
            return c;
        } catch(ClassNotFoundException x) {
            if(debug) CAT.debug("System classloader didn't find class: "+name);
        }

        //load from repository
        if(debug) CAT.debug("Try to load with AppClassLoader: "+name);
        String pack=getPackageName(name);
        if(loader.isIncludedPackage(pack)) {
            byte[] data=getClassData(name);
            if(data==null) {
                if(debug) CAT.debug("AppClassLoader didn't find class: "+name);
            } else {	
                if(debug) CAT.debug("AppClassLoader found class: "+name);
                definePackage(name);
                c=defineClass(name,data,0,data.length);
                appClasses.add(c);
                return c;
            }
        } else {
            if(debug) CAT.debug("No inclusion found for package: "+pack);
        }

        //load from parent
        if(parent!=null) {
            try {
                if(debug) CAT.debug("Try to load with parent classloader: "+name);
                c=parent.loadClass(name);
                if(debug) CAT.debug("Parent classloader found class: "+name);
                return c;
            } catch(ClassNotFoundException x) {
                if(debug) CAT.debug("Parent classloader didn't find class: "+name);
            }
        }
	
        throw new ClassNotFoundException(name);
    }
    
    
    protected void definePackage(String className) {
        int ind=className.lastIndexOf('.');
        if(ind!=-1) {
            String pkgname=className.substring(0,ind);
            Package pkg=getPackage(pkgname);
            if(pkg==null) {
                definePackage(pkgname,null,null,null,null,null,null,null);
            }
        }
    }
    
    protected byte[] getClassData(String name) {
        byte[] data = null;
        File file   = null;
        try {
            String pathName=name.replace('.','/');
            /**
            //BCEL
            JavaClass klass    = repository.loadClass(name);
            ClassGen  klassgen = new ClassGen(klass);
            if (klassgen.isInterface()) {
                if(debug) CAT.debug("**** Is an interface: " + name);
            } else {
                if(debug) CAT.debug("**** Is a class: " + name);
                int ind=name.indexOf('$');
                if(ind>-1) {
                    //Handle inner classes
                    String outerPath=pathName.substring(0,ind);
                    String sign="(L"+outerPath+";)V";
                    if(klassgen.containsMethod("<init>",sign)==null) {
                        if(debug) CAT.debug("Didn't find default constructor, creating one for inner class "+name);
                        String outer=name.substring(0,ind);
                        createDefaultConstructor(name,outer,klassgen);
                    }
                } else {
                    //Handle normal classes
                    if (klassgen.containsMethod("<init>", "()V") == null) {
                        if(debug) CAT.debug("Didn't find empty constructor, creating one for " + name);
                        klassgen.addEmptyConstructor(Constants.ACC_PRIVATE);
                    } else {
                        if(debug) CAT.debug("Already has empty constructor: " + name);
                    }
                }
            }
            klass = klassgen.getJavaClass();
            data = klass.getBytes();
            */
            file = new File(AppLoader.getInstance().getRepository(),pathName + ".class");
            FileInputStream fis=new FileInputStream(file);
            data=new byte[fis.available()];
            fis.read(data);
            synchronized(modTimes) {
                modTimes.put(file,new Long(file.lastModified()));
            }
        } catch(IOException x) {
            CAT.warn("Can't get IO for file '"+file+"'.");
            CAT.debug(x);
        }
        /**
        //BCEL
        } catch(ClassNotFoundException x) {
            CAT.warn(x.getMessage());
            CAT.debug("Can't get Class for file '" + file + "'.",x);
        }
        */
        return data;
    }
	
/**
    //BCEL
    private void createDefaultConstructor(String className,String outerClassName,ClassGen classGen) {
        ConstantPoolGen poolGen=classGen.getConstantPool();
        InstructionFactory factory=new InstructionFactory(classGen,poolGen);
        InstructionList insList=new InstructionList();
        MethodGen method=new MethodGen(0,Type.VOID,new Type[] {new ObjectType(outerClassName)},new String[] {"arg0"},"<init>",className,insList,poolGen);
        InstructionHandle insHandle0=insList.append(InstructionFactory.createLoad(Type.OBJECT,0));
        insList.append(factory.createInvoke("java.lang.Object","<init>",Type.VOID,Type.NO_ARGS,Constants.INVOKESPECIAL));
        insList.append(InstructionFactory.createLoad(Type.OBJECT,0));
        insList.append(InstructionFactory.createLoad(Type.OBJECT,1));
        insList.append(factory.createFieldAccess(className,"this$0",new ObjectType(outerClassName),Constants.PUTFIELD));
        InstructionHandle insHandle9=insList.append(InstructionFactory.createReturn(Type.VOID));
        method.setMaxStack();
        method.setMaxLocals();
        classGen.addMethod(method.getMethod());
        insList.dispose();
    }
*/
    
    public boolean modified() {
        synchronized(modTimes) {
            Iterator it=modTimes.keySet().iterator();
            while(it.hasNext()) {
                File file=(File)it.next();
                long modOld=((Long)modTimes.get(file)).longValue();
                long modNew=file.lastModified();
                if(modOld!=modNew) return true;
            }
            return false;
        }
    }
    
    protected String getPackageName(String className) {
        int ind=className.lastIndexOf('.');
        if(ind>0) {
            return className.substring(0,ind);
        } 
        return "";
    }

    public URL getResource(String name) {
        return parent.getResource(name);
    }
    
    public InputStream getResourceAsStream(String name) {
        return parent.getResourceAsStream(name);
    }
   
}
