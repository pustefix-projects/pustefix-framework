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
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.ClassPath;
import org.apache.bcel.util.SyntheticRepository;
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
    private SyntheticRepository  repository;
    
    public AppClassLoader(java.lang.ClassLoader parent) {
        super(parent);
        this.parent=parent;
        debug = CAT.isDebugEnabled();
        try {
            repository = SyntheticRepository.getInstance(new ClassPath(AppLoader.getInstance().getRepository().getCanonicalPath()));
        } catch (IOException ex) {
            throw new RuntimeException(ex.toString());
        }
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
                return defineClass(name,data,0,data.length);
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
            JavaClass klass    = repository.loadClass(name);
            ClassGen  klassgen = new ClassGen(klass);
            if (klassgen.isInterface()) {
                if(debug) CAT.debug("**** Is an interface: " + name);
            } else {
                if(debug) CAT.debug("**** Is a class: " + name);
                if (klassgen.containsMethod("<init>", "()V") == null) {
                    if(debug) CAT.debug("Didn't find empty constructor, creating one for " + name);
                    klassgen.addEmptyConstructor(Constants.ACC_PRIVATE);
                } else {
                    if(debug) CAT.debug("Already has empty constructor: " + name);
                }
            }
            klass = klassgen.getJavaClass();
            data = klass.getBytes();
            file = new File(AppLoader.getInstance().getRepository(),name.replace('.','/') + ".class");
            synchronized(modTimes) {
                modTimes.put(file,new Long(file.lastModified()));
            }
        } catch(ClassNotFoundException x) {
            CAT.error("Can't get Class for file '" + file + "'.",x);
        }
        return data;
    }
	
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
