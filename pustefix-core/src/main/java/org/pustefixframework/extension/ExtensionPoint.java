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

package org.pustefixframework.extension;


/**
 * Base interface for all extension points in Pustefix. An extension point
 * is a point in an application or module, where it can be extended by other
 * modules.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface ExtensionPoint <T extends Extension> {
    /**
     * Returns the unique identifer for this extension point. The identifier
     * is used by extensions to find a specific extension point and thus
     * has to be globally unique. Using a Java-style naming scheme is
     * encouraged (e.g. "com.example.myapp.MyExtensionPoint").
     * 
     * @return identifier for this extension point
     */
    String getId();
    
    /**
     * Returns the version of this extension point. The version can be used
     * by extensions to extend a certain version (or version range) of an 
     * extension point.
     * 
     * @return version number of this extension point
     */
    String getVersion();
    
    /**
     * Returns the type of this extension point. The type specifies which
     * kind of extension this extension point expects and might be used to
     * cast this instance to a specific sub-class.
     * 
     * @return type of this extension point
     */
    String getType();

    /**
     * Registers an extension at this extension point. If the extension
     * is already registered, no action is performed.
     * 
     * @param extension the extension that is being registered
     * 
     * @throws IllegalArgumentException if extension is not of the same
     *  type as this extension point. 
     */
    void registerExtension(T extension);

    /**
     * Unregisters an extension that has been registered earlier. If the
     * extension is not registered, no action action is performed.
     * 
     * @param extension the extension that should be removed from the
     *  list of registered extensions
     */
    public void unregisterExtension(T extension);

}
