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

import de.schlund.util.FactoryInit;
import de.schlund.pfixxml.serverutil.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Category;

/**
 * AppLoader.java 
 * 
 * Created: 29.04.2003
 * 
 * @author mleidig
 */
public class AppLoader implements FactoryInit,Runnable {
 
    private Category                CAT           = Category.getInstance(getClass().getName());
    private static AppLoader        instance      = new AppLoader();
    private AppClassLoader          loader;
    private ArrayList               loaderHistory = new ArrayList();
    private static SimpleDateFormat dateFormat    = new SimpleDateFormat();
    private WeakHashMap             reloaders     = new WeakHashMap();
    
    public static final int AUTO_TRIGGER=0;
    public static final int MANUAL_TRIGGER=1;
    public static final int INCONSISTENCY_POSSIBLE=2;
    public static final int INCONSISTENCY_PROBABLE=3;
    public static final int RELOAD_ACTION=4;
    public static final int RESTART_ACTION=5;
    public static final int IGNORE_ACTION=6;
    
    private boolean enabled;
    private int trigger;
    private CommandListener listener;
    private int interval;
    private HashSet incPacks=new HashSet();
    private HashSet excPacks=new HashSet();
    private HashSet excClasses=new HashSet();
    private ArrayList travExcludes=new ArrayList();
    private HashSet travIncludes=new HashSet();
    private HashMap policies=new HashMap();
    private File repository;
    private Thread modThread;
    private boolean isLoading;
    
    public static AppLoader getInstance() {
        return instance;
    }
    
    AppLoader() {}
  
    public void init(Properties globProps) {
        AppLoaderConfig config=new AppLoaderConfig(this);
        config.init(globProps);
        if(isEnabled()) {
            try {
                ObjectBuilder builder=ObjectBuilder.getInstance();
            } catch(ObjectBuilderException x) {
                CAT.error("Can't initialize AppLoader.");
                setEnabled(false);
                return;
            }
            loader=new AppClassLoader(getClass().getClassLoader());
            if(trigger==AUTO_TRIGGER) startModificationThread();
            if(listener!=null) listener.start();
        }
        CAT.info(this);
    }
  
    protected void startModificationThread() {
        modThread=new Thread(this);
        modThread.start();
    }
    
    protected void stopModificationThread() {
        if(modThread!=null) modThread.interrupt();
        modThread=null;
    }
  
    protected void setEnabled(boolean enabled) {
        this.enabled=enabled;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
 
    protected void setCommandListener(CommandListener listener) {
        this.listener=listener;
    }
    
    protected void setRepository(File repository) {
        this.repository=repository;
    }

    protected File getRepository() {
        return repository;
    }

    protected void includePackage(String pack) {
        incPacks.add(pack);
    }
    
    protected void excludePackage(String pack) {
        excPacks.add(pack);
    }
    
    protected void excludeClass(String clazz) {
        excClasses.add(clazz);
    }
  
    protected void excludeTraversePackage(String pack) {
        travExcludes.add(pack);
    }
    
    protected boolean needToTraverse(Class clazz) {
        String cn=clazz.getName();
        for(int i=0;i<travExcludes.size();i++) {
            if(cn.startsWith((String)(travExcludes.get(i)))) {
                if(travIncludes.contains(cn)) return true;
                return false;   
            }
        }
        return true;
    }
    
    protected void includeTraverseClass(String clazz) {
        travIncludes.add(clazz);
    }
  
    protected void setPolicy(int type,int action) {
        if((type==INCONSISTENCY_POSSIBLE || type==INCONSISTENCY_PROBABLE) && 
           (action==RELOAD_ACTION || action==RESTART_ACTION || action==IGNORE_ACTION)) {
           policies.put(new Integer(type),new Integer(action));
        }
    }
  
    protected boolean isIncludedPackage(String packName) {
        if(incPacks.contains(packName)) return true;
        int ind=packName.indexOf('.');
        while(ind>-1) {
            String pack=packName.substring(0,ind);
            if(incPacks.contains(pack)) return true;
            ind=packName.indexOf('.',ind+1);
        }
        return false;
    }
    
    protected boolean isExcludedPackage(String packName) {
        if(excPacks.contains(packName)) return true;
        int ind=packName.indexOf('.');
        while(ind>-1) {
            String pack=packName.substring(0,ind);
            if(excPacks.contains(pack)) return true;
            ind=packName.indexOf('.',ind+1);
        }
        return false;
    }
    
    protected boolean isExcludedClass(String className) {
        if(excClasses.contains(className)) return true;
        return false;
    }
    
    public boolean isReloadableClass(String className) {
        String pack=getPackageName(className);
        if(pack==null) return false;
        if(isIncludedPackage(pack) && !isExcludedPackage(pack) && !isExcludedClass(className)) return true;
        return false;
    }
       
    protected String getPackageName(String className) {
        int ind=className.lastIndexOf('.');
        if(ind>0) return className.substring(0,ind);
        return null;
    }
            
    protected void setTrigger(int trigger) {
        int old=this.trigger;
        if(old!=trigger && (trigger==AUTO_TRIGGER || trigger==MANUAL_TRIGGER)) {
            this.trigger=trigger;
            if(isEnabled()) {
                if(old==MANUAL_TRIGGER) {
                    startModificationThread();
                } else {
                    stopModificationThread();
                }
            }
        }
    }
    
    protected void setInterval(int interval) {
        this.interval=interval;
    }
  
    public Class loadClass(String name) throws ClassNotFoundException {
        return loader.loadClass(name);
    }
    
    public void run() {
        Thread theThread=Thread.currentThread();
        while(modThread==theThread) {
            try {
                Thread.sleep(interval);
            } catch(InterruptedException x) {}
            CAT.debug("Look for modified classes.");
            boolean modified=loader.modified();
            if(modified) {
                CAT.debug("Found modified classes.");
                forceReload();
            }
        }
    }
    
    protected AppClassLoader getAppClassLoader() {
        return loader;
    }
    
    public void addReloader(Reloader reloader) {
        reloaders.put(reloader,null);
        //reloaders.add(reloader);
    }
    
    public void removeReloader(Reloader reloader) {
        reloaders.remove(reloader);
    }
    
    public Iterator getReloaders() {
        return reloaders.keySet().iterator();
    }
    
    protected boolean restart() {
        loadingStarted();
		addToHistory(this,"Restart");
        boolean reloaded=reload();
        ArrayList sessions=new ArrayList();
        SessionAdmin admin=SessionAdmin.getInstance();
        Iterator it=admin.getAllSessionIds().iterator();
        while(it.hasNext()) {
            String id=(String)it.next();
            SessionInfoStruct sis=admin.getInfo(id);
            HttpSession session=sis.getSession();
            sessions.add(session);
        }
        it=sessions.iterator();
        while(it.hasNext()) {
            HttpSession session=(HttpSession)it.next();
            CAT.debug("Invalidate session '"+session.getId()+"'.");
            session.invalidate();
        }
        CAT.info("Restarted application.");
        loadingEnded();
        return reloaded;
    }
    
    protected void triggerReloaders() {
        //Iterator it=reloaders.iterator();
        Iterator it=reloaders.keySet().iterator();
        while(it.hasNext()) {
            Reloader reloader=(Reloader)it.next();
            reloader.reload();
        }  
    }
    
    protected boolean reload() {
        loadingStarted();
        CAT.debug("Look for modified classes.");
        boolean modified=loader.modified();
        if(modified) {
			addToHistory(this,"Reload");
            CAT.debug("Found modified classes.");
            forceReload();
            loadingEnded();
            return true;
        } else {
            CAT.info("No modified classes found. Reload is spared.");    
            loadingEnded(); 
            return false;  
        }
    }
    
    protected void forceReload() {
        boolean direct=true;
        if(isLoading()) direct=false;
        if(direct) loadingStarted(); 
        long t1=System.currentTimeMillis();
        AppClassLoader oldLoader=loader;
        loader=new AppClassLoader(getClass().getClassLoader());
        StateTransfer.getInstance().reset();
        triggerReloaders();
        //search classes with no referenced instance
        HashSet loaderClasses=oldLoader.getAppClasses();
        HashSet transferClasses=StateTransfer.getInstance().getAppClasses();
        ArrayList norefs=new ArrayList();
        Iterator it=loaderClasses.iterator();
        while(it.hasNext()) {
            Class clazz=(Class)it.next();
            if(!transferClasses.contains(clazz) && !clazz.isInterface()) {
                norefs.add(clazz);
            }
        }
        it=norefs.iterator();
        while(it.hasNext()) {
            Class clazz=(Class)it.next();
            StateTransfer.getInstance().transfer(clazz); 
        }
        long t2=System.currentTimeMillis();
        CAT.info("Reloaded classes in "+(t2-t1)+" ms.");
        if(direct) loadingEnded();
    }
    
    /**
     * Set flag indicating that AppLoader is currently loading.
     */
    private synchronized void loadingStarted() {
        isLoading=true;
    }
    
    /**
     * Set flag indicating that AppLoader has finished loading.
     */
    private synchronized void loadingEnded() {
        isLoading=false;
    }
    
    /**
     * Returns if AppLoader is currently loading.
     */
    public synchronized boolean isLoading() {
        return isLoading;
    }
    
    public String toString() {
        StringBuffer sb=new StringBuffer();
        sb.append("AppLoader[enabled="+enabled+"][listener="+listener+"][trigger="+trigger+"][interval="+interval+"]");
        sb.append("[policy="+policies+"][repository="+repository+"][includes="+incPacks+"]");
        sb.append("[excludepackages="+travExcludes+"][includeclasses="+travIncludes+"]");
        return sb.toString();
    }
    
    protected void addToHistory(Object source,String event) {
    	loaderHistory.add(new HistoryEntry(source,event));
    }
    
    protected String getHistory() {
    	StringBuffer sb=new StringBuffer();
   		Iterator it=loaderHistory.iterator();
   		while(it.hasNext()) {
   			HistoryEntry entry=(HistoryEntry)it.next();
   			sb.append(entry.toString()+"\n");
   		}
   		return sb.toString();
    }
    
    class HistoryEntry {
    	
    	String srcClassName;
    	int srcHash;
    	String event;
    	long time;
    	
    	HistoryEntry(Object source,String event) {
    		srcClassName=source.getClass().getName();
    		srcHash=source.hashCode();
    		this.event=event;
    		this.time=System.currentTimeMillis();
    	}
    	
    	public String getSource() {
    		return srcClassName+"@"+String.valueOf(srcHash);
    	}
    	
    	public String getEvent() {
    		return event;
    	}
    	
    	public long getTime() {
    		return time;
    	}
    	
    	public String toString() {
    		return dateFormat.format(new Date(getTime()))+"  ["+getSource()+"]  "+getEvent();
    	}
    	
    }
    
}
