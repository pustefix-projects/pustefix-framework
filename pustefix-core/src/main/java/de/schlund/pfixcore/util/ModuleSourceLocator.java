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
package de.schlund.pfixcore.util;

import java.io.File;
import java.net.URL;

/**
 * Implementors can provide a source or unpacked directory for a pustefix module 
 * specified by the jar file's URL. Thus resources are loaded from this location
 * instead of the jar file. 
 * 
 * @author mleidig@schlund.de
 *
 */
public interface ModuleSourceLocator {

    public File getLocation(URL jarUrl);
    
}
