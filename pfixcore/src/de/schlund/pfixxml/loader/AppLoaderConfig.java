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

import org.apache.log4j.Category;
import java.io.*;
import java.util.*;

/**
 * AppLoaderConfig.java 
 * 
 * Created: 29.04.2003
 * 
 * @author mleidig
 */
public class AppLoaderConfig {

    private Category CAT=Category.getInstance(getClass().getName());
    //private final static String CONFIG_FILE="../apploader.conf";
    private AppLoader loader;
   
    protected AppLoaderConfig(AppLoader loader) {
        this.loader=loader;
    }
    
    protected void init(Properties globProps) {
        try {
            //read properties
            String fileName=globProps.getProperty("apploader.propertyfile");
            Properties props=new Properties();
            if(fileName!=null) {
                try {
                    props.load(new FileInputStream(fileName));
                } catch(IOException x) {
                    throw new AppLoaderConfigException("AppLoader config file '"+fileName+"' can't be loaded.");
                }
            } else {
                throw new AppLoaderConfigException("AppLoader config file property isn't set.");
            }
            //apploader
            String name="apploader.mode";
            String val=props.getProperty(name);
            if(val!=null) {
                if(val.equals("on")) {
                    loader.setEnabled(true);
                } else if(val.equals("off")) {
                    loader.setEnabled(false);
                    return;
                } else {
                    throw new AppLoaderConfigException("Property '"+name+"' needs value 'on|off'.");
                }    
            } else {
                throw new AppLoaderConfigException("Property '"+name+"' needed.");
            }
            //cmdlistener
            name="apploader.cmdlistener.mode";
            val=props.getProperty(name);
            if(val!=null) {
                boolean active=false;
                if(val.equals("on")) {
                    active=true;
                } else if(val.equals("off")) {
                    active=false;
                } else {
                    throw new AppLoaderConfigException("Property '"+name+"' needs value 'on|off'.");
                }    
                name="apploader.cmdlistener.port";
                val=props.getProperty(name);
                if(val!=null) {
                    try {
                        int port=Integer.parseInt(val);
                        if(active) {
                            CommandListener cl=new CommandListener(port);
                            loader.setCommandListener(cl);
                        }
                    } catch(NumberFormatException x) {
                        throw new AppLoaderConfigException("Property '"+name+"' has invalid value.");
                    }
                } else {
                    throw new AppLoaderConfigException("Property '"+name+"' needed.");
                }
            } else {
                throw new AppLoaderConfigException("Property '"+name+"' needed.");
            }
            //trigger
            name="apploader.trigger.type";
            val=props.getProperty(name);
            if(val!=null) {
                if(val.equals("auto")) {
                    loader.setTrigger(AppLoader.AUTO_TRIGGER);
                } else if(val.equals("manual")) {
                    loader.setTrigger(AppLoader.MANUAL_TRIGGER);
                } else {
                    throw new AppLoaderConfigException("Property '"+name+"' needs value 'auto|manual'.");
                }
            } else {
                throw new AppLoaderConfigException("Property '"+name+"' needed.");
            }
            String ttype=val;
            name="apploader.trigger.interval";
            val=props.getProperty(name);
            if(val!=null) {
                try {
                    int interval=Integer.parseInt(val)*1000;
                    loader.setInterval(interval);
                } catch(NumberFormatException x) {
                    throw new AppLoaderConfigException("Property '"+name+"' has invalid value.");
                }
            } else {
                if(ttype.equals("auto")) throw new AppLoaderConfigException("Property '"+name+"' needed.");
            }
            //policy
            name="apploader.policy.inconsistency.";
            Enumeration enm=props.propertyNames();
            while(enm.hasMoreElements()) {
                String key=(String)enm.nextElement();
                if(key.startsWith(name)) {
                    String typeName=key.substring(name.length());
                    int type;
                    if(typeName.equals("possible")) {
                        type=AppLoader.INCONSISTENCY_POSSIBLE;
                    } else if(typeName.equals("probable")) {
                        type=AppLoader.INCONSISTENCY_PROBABLE;
                    } else {
                        throw new AppLoaderConfigException("Property '"+name+"' needs extension 'possible|probable'.");
                    }
                    val=props.getProperty(key);
                    int action;
                    if(val!=null) {
                        if(val.equals("reload")) {
                            action=AppLoader.RELOAD_ACTION;
                        } else if(val.equals("restart")) {
                            action=AppLoader.RESTART_ACTION;
                        } else if(val.equals("ignore")) {
                            action=AppLoader.IGNORE_ACTION;
                        } else {
                            throw new AppLoaderConfigException("Property '"+key+"' needs value 'reload|restart|ignore'.");
                        }
                    } else {
                        throw new AppLoaderConfigException("Property '"+key+"' needed.");
                    }
                    loader.setPolicy(type,action);
                }
            }
            //repository
            name="apploader.repository";
            val=props.getProperty(name);
            if(val!=null && !val.equals("")) {
                File file=new File(val);
                if(file.exists()) {
                    loader.setRepository(file);
                } else {
                    throw new AppLoaderConfigException("Property '"+name+"' needs existing directory as value.");
                }
            } else {
                throw new AppLoaderConfigException("Property '"+name+"' needed.");
            }
            //package
            name="apploader.package.include.";
            enm=props.propertyNames();
            while(enm.hasMoreElements()) {
                String key=(String)enm.nextElement();
                if(key.startsWith(name)) {
                    val=props.getProperty(key);
                    if(val!=null && !val.equals("")) {
                        loader.includePackage(val);
                    } else {
                        throw new AppLoaderConfigException("Property '"+key+"' needs package name as value.");
                    }
                }
            } 
            //class
            name="apploader.class.exclude.";
            enm=props.propertyNames();
            while(enm.hasMoreElements()) {
                String key=(String)enm.nextElement();
                if(key.startsWith(name)) {
                    val=props.getProperty(key);
                    if(val!=null && !val.equals("")) {
                        loader.excludeClass(val);
                    } else {
                        throw new AppLoaderConfigException("Property '"+key+"' needs class name as value.");
                    }
                }
            }
            //traverse
            String exName="apploader.traverse.excludepackage";
            String inName="apploader.traverse.includeclass";
            enm=props.propertyNames();
            while(enm.hasMoreElements()) {
                String key=(String)enm.nextElement();
                if(key.startsWith(exName) || key.startsWith(inName)) {
                    val=props.getProperty(key);
                    if(val!=null && !val.equals("")) {
                        if(key.startsWith(exName)) {
                            loader.excludeTraversePackage(val);
                        } else if(key.startsWith(inName)) {
                            loader.includeTraverseClass(val); 
                        }
                    }
                }
            }
        } catch(AppLoaderConfigException x) {
            CAT.error("Error while reading AppLoader configuration.",x);
            loader.setEnabled(false);
        }
    }
}
