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
import de.schlund.pfixxml.PathFactory;

/**
 * Utility class used to create a Spring ApplicationContext at startup and
 * retrieve it later
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 * @see de.schlund.pfixcore.editor2.core.spring.EditorApplicationContext
 */
public final class EditorApplicationContextFactory {
    private static EditorApplicationContextFactory instance = new EditorApplicationContextFactory();

    private EditorApplicationContext appContext;

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
        String configFile = props
                .getProperty("de.schlund.pfixcore.editor2.springconfig");

        if (configFile == null) {
            String err = "Property de.schlund.pfixcore.editor2.springconfig not set!";
            Logger.getLogger(this.getClass()).error(err);
            throw new Exception(err);
        }

        EditorApplicationContext context = new EditorApplicationContext(
                configFile, PathFactory.getInstance().createPath("")
                        .getBase().getAbsolutePath());
        this.appContext = context;
        Logger.getLogger(this.getClass()).info("Initialized ApplicationContext for editor");
    }
    
    /**
     * Returns the Spring ApplicationContext for the editor application
     * 
     * @return Spring ApplicationContext
     */
    public ApplicationContext getApplicationContext() {
        return this.appContext;
    }
}
