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
 
    private Category CAT=Category.getInstance(getClass().getName());
    private static AppLoader instance=new AppLoader();
    private AppClassLoader loader;
    private WeakHashMap reloaders=new WeakHashMap();
    //private ArrayList reloaders=new ArrayList();
    
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
    private ArrayList travExcludes=new ArrayList();
    private HashSet travIncludes=new HashSet();
    private HashMap policies=new HashMap();
    private File repository;
    private Thread modThread;
    
    public static AppLoader getInstance() {
        return instance;
    }
    
    AppLoader() {}
  
    public void init(Properties globProps) {
        AppLoaderConfig config=new AppLoaderConfig(this);
        config.init(globProps);
        if(isEnabled()) {
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
        travIncludes.add(clazz);;
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
    
    public boolean isIncludedClass(String className) {
        String pack=className.substring(0,className.lastIndexOf('.'));
        return isIncludedPackage(pack);
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
        CAT.debug("Look for modified classes.");
        boolean modified=loader.modified();
        if(modified) {
            CAT.debug("Found modified classes.");
            forceReload();
            return true;
        } else {
            CAT.info("No modified classes found. Reload is spared.");     
            return false;  
        }
    }
    
    protected void forceReload() {
            long t1=System.currentTimeMillis();
            loader=new AppClassLoader(getClass().getClassLoader());
            StateTransfer.getInstance().reset();
            triggerReloaders();
            long t2=System.currentTimeMillis();
            CAT.info("Reloaded classes in "+(t2-t1)+" ms.");
    }
    
    public String toString() {
        StringBuffer sb=new StringBuffer();
        sb.append("AppLoader[enabled="+enabled+"][listener="+listener+"][trigger="+trigger+"][interval="+interval+"]");
        sb.append("[policy="+policies+"][repository="+repository+"][includes="+incPacks+"]");
        return sb.toString();
    }
    
}
