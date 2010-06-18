package org.pustefixframework.util;
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

import org.osgi.framework.Bundle;

/**
 * Helper class checking if bundle is Pustefix bundle, i.e. contains an application
 * or module descriptor file.
 * 
 * @author mleidig@schlund.de
 *
 */
public class PustefixBundleDetection {

    public final static String PUSTEFIX_CONFIG_PATH = "META-INF";
    public final static String PUSTEFIX_CONFIG_FILE_MODULE = "pustefix-module.xml";
    public final static String PUSTEFIX_CONFIG_FILE_APPLICATION = "pustefix-application.xml";
    
    public static boolean isPustefixApplication(Bundle bundle) {
        if (bundle.findEntries(PUSTEFIX_CONFIG_PATH, PUSTEFIX_CONFIG_FILE_APPLICATION, false) != null) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isPustefixModule(Bundle bundle) {
        if (bundle.findEntries(PUSTEFIX_CONFIG_PATH, PUSTEFIX_CONFIG_FILE_MODULE, false) != null) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isPustefixBundle(Bundle bundle) {
        return isPustefixApplication(bundle) || isPustefixModule(bundle);
    }
    
}
