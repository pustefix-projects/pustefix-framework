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
 */

package de.schlund.pfixcore.editor2.frontend.util;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import de.schlund.pfixcore.editor2.core.spring.EditorApplicationContext;
import de.schlund.pfixcore.editor2.core.spring.PustefixTargetUpdateServiceImpl;
import de.schlund.pfixxml.PathFactory;

/**
 * Utility class used to create a Spring ApplicationContext at startup and
 * retrieve it later
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 * @see de.schlund.pfixcore.editor2.core.spring.EditorApplicationContext
 */
public final class EditorApplicationContextFactory implements Runnable {
    private static EditorApplicationContextFactory instance = new EditorApplicationContextFactory();

    private EditorApplicationContext appContext;

    private Properties initProps;

    private boolean initialized = false;
    
    private boolean runningInit = false;
    
    private boolean initFailed = false;

    private Object initLock = new Object();

    private EditorApplicationContextFactory() {
        // Exists only to force singleton pattern
    }

    public static EditorApplicationContextFactory getInstance() {
        return instance;
    }

    /**
     * Creates the Spring ApplicationContext
     * 
     * @param props
     *            Properties used for configuration
     * @throws Exception
     *             If any error occurs during initialization
     */
    public void init(Properties props) throws Exception {
        this.runningInit = true;
        this.initProps = props;
        Thread thread = new Thread(this, "editor-init");
        thread.start();
    }

    /**
     * Returns the Spring ApplicationContext for the editor application
     * 
     * @return Spring ApplicationContext
     */
    public ApplicationContext getApplicationContext() {
        while (this.runningInit) {
            synchronized (this.initLock) {
                try {
                    this.initLock.wait();
                } catch (InterruptedException e) {
                    // Continue
                }
            }
        }
        if (this.initialized) {
            return this.appContext;
        } else if (this.initFailed) {
            throw new RuntimeException("Editor initialization failed!");
        } else {
            throw new RuntimeException("Editor has not been initialized yet or is not active in this environment.");
        }
    }

    public void run() {
        try {
            String configFile = this.initProps.getProperty("de.schlund.pfixcore.editor2.springconfig");
            
            if (configFile == null) {
                String err = "Property de.schlund.pfixcore.editor2.springconfig not set!";
                Logger.getLogger(this.getClass()).fatal(err);
            }
            try {
                EditorApplicationContext context = new EditorApplicationContext(configFile, PathFactory.getInstance().createPath("").getBase().getAbsolutePath());
                this.appContext = context;
                Logger.getLogger(this.getClass()).info("Initialized ApplicationContext for editor");
            } catch (RuntimeException e) {
                String err = "Initialization of ApplicationContext for editor failed!";
                Logger.getLogger(this.getClass()).fatal(err, e);
                return;
            }

            // Active / deactivate background generation of targets
            String generatorProp = this.initProps.getProperty("de.schlund.pfixcore.editor2.updatetargets");
            boolean generatorFlag = false;

            // System.out.println("############ in EACF: generatorProp: " + generatorProp);

            if (generatorProp == null || generatorProp.equals("1") || generatorProp.equalsIgnoreCase("true")) {
                generatorFlag = true;
            }

            // There should be only one instance of the update service,
            // but you can never be sure enough.
            // Note that we look for the concrete implementation, not the
            // interface.
            String beanNames[] = this.appContext.getBeanDefinitionNames(PustefixTargetUpdateServiceImpl.class);
            for (int i = 0; i < beanNames.length; i++) {
                // System.out.println("############ in EACF: calling setEnabled: " + generatorFlag);
                ((PustefixTargetUpdateServiceImpl) this.appContext.getBean(beanNames[i])).enableAutoUpdating(generatorFlag);
            }

            this.initialized = true;
        } finally {
            if (!this.initialized) {
                this.initFailed = false;
            }
            this.runningInit = false;
            synchronized (this.initLock) {
                this.initLock.notifyAll();
            }
        }
    }
}
