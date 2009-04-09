/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixxml.config;

import javax.servlet.ServletContext;

/**
 * Used to set global configuration values. Should only be used from initialization
 * code. Settings can be read by all code using {@link de.schlund.pfixxml.config.GlobalConfig}.
 * Only one of {@link #setDocroot(String)} and {@link #setServletContext(ServletContext)}
 * may be used and this method may only be called once. Otherwise an
 * {@link java.lang.IllegalStateException} will be thrown.
 * {@link #setDocroot(String)} is be preferred to {@link #setServletContext(ServletContext)}
 * which should only be used in WAR mode, when the docroot is not available.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class GlobalConfigurator {
    
    /**
     * Sets path to Pustefix docroot.
     * 
     * @param path Absolute path to docroot. Must be an absolute pathname with no
     *             trailing slash.
     */
    public static void setDocroot(String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Path has to be absolute");
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        GlobalConfig.setDocroot(path);
    }
    
    /**
     * Sets servlet context. May only be used in WAR-mode when Pustefix is not
     * shared across several servlet contexts.
     * 
     * @param context the only servlet context used by this instance of Pustefix
     */
    public static void setServletContext(ServletContext context) {
        GlobalConfig.setServletContext(context);
    }
    
}
