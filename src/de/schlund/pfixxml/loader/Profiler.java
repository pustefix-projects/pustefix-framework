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
 * Profiler.java 
 * 
 * Created: 26.05.2003
 * 
 * @author mleidig
 */
public class Profiler {
    
    private Category CAT=Category.getInstance(getClass().getName());
    private boolean debug;
    
    protected Profiler() {
        debug=CAT.isDebugEnabled();
    }
    
    //typecheck
    
    HashSet refs=new HashSet();
    HashMap classLoaders=new HashMap();
    private boolean aclSrc;

    protected void addReferenced(Object obj) {
        if(obj instanceof Class) {
            ClassLoader cl=((Class)obj).getClassLoader();
            String clId="";
            if(cl!=null) clId=String.valueOf(System.identityHashCode(cl));
            refs.add(String.valueOf(System.identityHashCode(obj))+clId);
        } else {
            refs.add(new Integer(System.identityHashCode(obj)));
        }
    }
    
    protected boolean isReferenced(Object obj) {
        if(obj instanceof Class) {
            ClassLoader cl=((Class)obj).getClassLoader();
            String clId="";
            if(cl!=null) clId=String.valueOf(System.identityHashCode(cl));
            return refs.contains(String.valueOf(System.identityHashCode(obj))+clId);
        } else {
            return refs.contains(new Integer(System.identityHashCode(obj)));
        }
    }
    
    protected ThreadGroup getRootThreadGroup(ThreadGroup group) {
            ThreadGroup parent=group.getParent();
            if(parent==null) return group;
            return getRootThreadGroup(parent);
    }

    protected void doThreadCheck() {
            Thread thread=Thread.currentThread();
            ThreadGroup group=thread.getThreadGroup();
            ThreadGroup root=getRootThreadGroup(group);
            Thread[] all=new Thread[root.activeCount()];
            int max=root.enumerate(all);
            for(int i=0;i<max;i++) {
                    doTypeCheck(all[i]);
            }
    }

    protected void doTypeCheck() {
        AppLoader loader=AppLoader.getInstance();
	    Iterator it=loader.getReloaders();
	    while(it.hasNext()) {
            Object obj=it.next();
	        doTypeCheck(obj);
	    }
        doThreadCheck();
        doTypeCheck(AppLoader.getInstance());
    }
    
    protected void doTypeCheck(Object obj) {
    	boolean isClass=obj instanceof Class;
        Class clazz=obj.getClass();
		String clazzName=clazz.getName();
        if(clazzName.equals("de.schlund.pfixxml.loader.AppClassLoader")) aclSrc=true; 
        if(isReferenced(obj)) return;
        addReferenced(obj);
		if(isClass) {
			clazz=(Class)obj;
            if(aclSrc) {
                clazzName="(ACL)(java.lang.Class)"+clazz.getName();
            } else {
                clazzName="(java.lang.Class)"+clazz.getName();
            }
		}
        ClassLoader classLoader=clazz.getClassLoader();
	    HashMap classes=(HashMap)classLoaders.get(classLoader);
	    Integer count=null;
	    if(classes==null) {
		    classes=new HashMap();
		    classLoaders.put(classLoader,classes);
		    count=new Integer(1);
        } else {
		    count=(Integer)classes.get(clazzName);
		    if(count==null) {
			    count=new Integer(1);
		    } else {
			    count=new Integer(count.intValue()+1);
		    }
        }
	    classes.put(clazzName,count);
	    if(isClass) return;
        if(clazzName.startsWith("java.lang") && !(clazzName.equals("java.lang.Object")||clazzName.equals("java.lang.Thread"))) return;
        if(isObjectArray(clazz)) {
	        int len=Array.getLength(obj);
	        for(int i=0;i<len;i++) {
                Object value=Array.get(obj,i);
		        if(value!=null) doTypeCheck(value);
	        }
	    } else {
            while(clazz!=null) {
		        Field[] fields=clazz.getDeclaredFields();
                for(int i=0;i<fields.length;i++) {
                    String name=fields[i].getName();
                    try {
			            if(!fields[i].getType().isPrimitive()) {
                            if(!fields[i].isAccessible()) fields[i].setAccessible(true);
                            Object value=fields[i].get(obj);
                            if(value!=null) doTypeCheck(value);
                        } 
                    } catch(IllegalAccessException x) {
                        CAT.warn("Member is final",x);
		            }
		        }
		        clazz=clazz.getSuperclass();
            }
        }
        if(clazzName.equals("de.schlund.pfixxml.loader.AppClassLoader")) aclSrc=false;
    }

    protected boolean isObjectArray(Class clazz) {
        if(clazz.isArray()) {
            String name=clazz.getName();
            int ind=name.lastIndexOf('[');
            if(name.charAt(ind+1)=='L') return true; 
        }
        return false;
    }

    public String getTypeCheckInfo() {
        StringBuffer sb=new StringBuffer();
        int total=0;
        int appcl=0;
        Iterator it=classLoaders.keySet().iterator();
        sb.append("\n");
        while(it.hasNext()) {
            ClassLoader cl=(ClassLoader)it.next();
            if(cl==null) {
                sb.append("*** Bootstrap classloader ***\n"); 
	        } else {
                if(cl instanceof AppClassLoader) {
                    appcl++;
                } 
                sb.append("*** "+cl.getClass().getName()+"@"+cl.hashCode()+" ***\n");
            }
	        HashMap map=(HashMap)classLoaders.get(cl);
	        if(map!=null) {
                int num=0;
                Set keys=map.keySet();
                ArrayList al=new ArrayList(keys);
                Collections.sort(al);
                Iterator cit=al.iterator();
                while(cit.hasNext()) {
	                String c=(String)cit.next();
                    Integer cnt=(Integer)map.get(c);
                    num+=cnt.intValue();
                    sb.append(c+"  "+cnt+"\n");
                }
                sb.append("(Object count: "+num+")\n\n");
                total+=num;
            }
        }
        sb.append("(Total object count: "+total+")\n");
        if(appcl>1) {
            sb.append("\nWARNING: Found application classes loaded by "+appcl+
                " different AppClassLoader instances (see above).\n");
        }
        return sb.toString();
    }
	
    //classinfo
    
    public String getClassInfo(String className) {
        try {
            Class clazz=AppLoader.getInstance().loadClass(className);
            StringBuffer sb=new StringBuffer();
            sb.append("\n*** Class hierarchy ***\n[C]"+clazz.getName()+"\n");
            sb.append(walkClasses(clazz));
            sb.append("\n*** Class members ***\n"+walkFields(clazz));
            return sb.toString();
        } catch(ClassNotFoundException x) {
            return "Class '"+className+"' not found!";
        }
    }
    
    int level=0;
    String indent="\t";
    
    protected String indent(String str) {
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<level;i++) sb.append(indent);
        sb.append(str+"\n");
        return sb.toString();
    }

    protected String walkClasses(Class clazz) {
        StringBuffer sb=new StringBuffer();
        level++;
        Class sup=clazz.getSuperclass();
        if(sup!=null) {
            sb.append(indent("[S]"+sup.getName()));
            sb.append(walkClasses(sup));
        }
        Class[] ins=clazz.getInterfaces();
        if(ins.length>0) {
            for(int i=0;i<ins.length;i++) {
                Class c=ins[i];
                sb.append(indent("[I]"+c.getName()));
                sb.append(walkClasses(c));
            }
        }
        level--;
        return sb.toString();
    }
    
    protected String walkFields(Class clazz) {
        StringBuffer sb=new StringBuffer();
        while(clazz!=null) {
            Field[] fields=clazz.getDeclaredFields();
            for(int i=0;i<fields.length;i++) {
                try {
                    String name=fields[i].getName();
                    if(!fields[i].isAccessible()) fields[i].setAccessible(true);
                    sb.append("[F]"+name+"  [T]"+fields[i].getType().getName()+"\n");
                } catch(Exception x) {
                    x.printStackTrace();
                }
            }    
            clazz=clazz.getSuperclass();
        }
        return sb.toString();
    }
    
}
