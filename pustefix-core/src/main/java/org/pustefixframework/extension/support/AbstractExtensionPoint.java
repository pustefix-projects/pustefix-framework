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

package org.pustefixframework.extension.support;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pustefixframework.extension.Extension;
import org.pustefixframework.extension.ExtensionPoint;


/**
 * Abstract implementation of a generic extension point, handling
 * extension registratrion and deregistration.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class AbstractExtensionPoint <T1 extends AbstractExtensionPoint<T1, T2>, T2 extends Extension> implements ExtensionPoint<T2> {

    protected String id;
    protected String type;
    protected String version = "0.0.0";
    
    protected Set<T2> extensions = new LinkedHashSet<T2>();
    
    protected Set<ExtensionPointRegistrationListener<? super T1, ? super T2>> listeners = new LinkedHashSet<ExtensionPointRegistrationListener<? super T1, ? super T2>>();
    
    protected String cardinality;
    protected int cardinalityMin = 0;
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    /**
     * Maximum number of extensions that can be active at the same time.
     * A value of -1 means the number of active extensions is not limited.
     */
    protected int cardinalityMax = -1;

    public String getId() {
        return id;
    }

    /**
     * Sets the unique id, identifying this extension point.
     * 
     * @param id identifier that should be used for this extension point.
     */
    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    /**
     * Sets the type of this extension point. The type represents
     * the type of extension this extension point expects.
     * 
     * @param type type identifier
     */
    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Sets the version of this extension point. The version number
     * can be used to distinguish older and newer variants of the
     * same extension point.
     * 
     * @param version version string
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * Returns the cardinality of this extension point. The cardinality
     * determines the number of extension that can be active at a extension
     * point.
     * 
     * @return cardinality for this extension point.
     */
    public String getCardinality() {
        return cardinality;
    }
    
    /**
     * Sets the cardinality for this extension point. Valid cardinalities are
     * "0..n" (default), "1..n", "0..1" or "1".
     * 
     * @param cardinality string representing the cardinality
     */
    public void setCardinality(String cardinality) {
        if (cardinality.trim().equals("1")) {
            this.cardinality = cardinality;
            this.cardinalityMin = 1;
            this.cardinalityMax = 1;
            return;
        }
        
        int doublePointIndex = cardinality.indexOf("..");
        if (doublePointIndex == -1) {
            throw new IllegalArgumentException("\"" + cardinality + "\" is not a valid cardinality.");
        }
        int cmin, cmax;
        String firstPart = cardinality.substring(0, doublePointIndex).trim();
        if (firstPart.equals("0")) {
            cmin = 0;
        } else if (firstPart.equals("1")) {
            cmin = 1;
        } else {
            throw new IllegalArgumentException("\"" + cardinality + "\" is not a valid cardinality.");
        }
        String lastPart = cardinality.substring(doublePointIndex + 2, cardinality.length()).trim();
        if (lastPart.equals("1")) {
            cmax = 1;
        } else if (lastPart.equals("n")) {
            cmax = -1;
        } else {
            throw new IllegalArgumentException("\"" + cardinality + "\" is not a valid cardinality.");
        }
        this.cardinality = cardinality;
        this.cardinalityMin = cmin;
        this.cardinalityMax = cmax;
    }
    
    public void registerExtension(T2 extension) {
        if (extension == null) {
            throw new NullPointerException("extension parameter may not be null");
        }
        if (!getType().equals(extension.getType())) {
            throw new IllegalArgumentException("Registering extension of type " + extension.getType() + " at extension point of type " + getType() + " is not supported.");
        }
        synchronized (extensions) {
            if (extensions.contains(extension)) {
                return;
            }
            synchronized (listeners) {
                for (ExtensionPointRegistrationListener<? super T1, ? super T2> listener : listeners) {
                    try {
                        listener.beforeRegisterExtension(thisToT1(), extension);
                    } catch (Throwable e) {
                        logger.warn("Error while calling listener's beforeRegisterExtension method", e);
                    }
                }
                extensions.add(extension);
                for (ExtensionPointRegistrationListener<? super T1, ? super T2> listener : listeners) {
                    try {
                        listener.afterRegisterExtension(thisToT1(), extension);
                    } catch (Throwable e) {
                        logger.warn("Error while calling listener's afterRegisterExtension method", e);
                    }
                }
            }
        }
    }
    
    public void unregisterExtension(T2 extension) {
        synchronized (extensions) {
            if (!extensions.contains(extension)) {
                return;
            }
            synchronized (listeners) {
                for (ExtensionPointRegistrationListener<? super T1, ? super T2> listener : listeners) {
                    try {
                        listener.beforeUnregisterExtension(thisToT1(), extension);
                    } catch (Throwable e) {
                        logger.warn("Error while calling listener's beforeUnregisterExtension method", e);
                    }
                }
                extensions.remove(extension);
                for (ExtensionPointRegistrationListener<? super T1, ? super T2> listener : listeners) {
                    try {
                        listener.afterUnregisterExtension(thisToT1(), extension);
                    } catch (Throwable e) {
                        logger.warn("Error while calling listener's afterUnregisterExtension method", e);
                    }
                }
            }
        }
    }
    
    /**
     * Allows an extension to notify the extension point about a change
     * in its internal state. The extension point will then call the
     * <code>updateExtension</code> method of all registration listeners.
     * If <code>extension</code> is not a registered extension, no actions
     * will be performed.
     * 
     * @param extension the extension that has changed its internal state 
     */
    public void updateExtension(T2 extension) {
        synchronized (extensions) {
            if (!extensions.contains(extension)) {
                return;
            }
            synchronized (listeners) {
                for (ExtensionPointRegistrationListener<? super T1, ? super T2> listener : listeners) {
                    try {
                        listener.updateExtension(thisToT1(), extension);
                    } catch (Throwable e) {
                        logger.warn("Error while calling listener's updateExtension method", e);
                    }
                }
            }
        }
    }
    
    /**
     * Returns an unmodifiable copy of the list of all extensions that 
     * are registered at this extension point at the moment. As this list
     * is a copy, changes that occur after calling this method will not
     * appear in the list. If the cardinality of this extension point
     * requires at least one extension and no extension is registered, an
     * {@link IllegalStateException} is thrown. If the cardinality allows
     * only one extension to be active but more than one extension have
     * been registered, the extension that has been registered first
     * is returned.
     * 
     * @return list of the active extensions for this extension point.
     * 
     * @throws IllegalStateException if at least one extension is required
     *  but no extension has been registered.
     */
    public Collection<T2> getExtensions() {
        synchronized (extensions) {
            if (extensions.size() < cardinalityMin) {
                throw new IllegalStateException("getExtension() for extension point \"" + getId() + "\" has been called while " + extensions.size() + " extension(s) were present but a minimum of " + cardinalityMin + " extension(s) is required.");
            }
            if (cardinalityMax == -1 || extensions.size() <= cardinalityMax) {
                return Collections.unmodifiableList(new LinkedList<T2>(extensions));
            } else {
                LinkedList<T2> list = new LinkedList<T2>();
                int count = cardinalityMax;
                for (T2 extension : extensions) {
                    if (count == 0) {
                        break;
                    }
                    list.add(extension);
                    count++;
                }
                return Collections.unmodifiableList(list);
            }
        }
    }
    
    /**
     * Registers a change listener for this extension point.
     * 
     * @param listener listener that is notified about registrations and
     *  deregistrations at this extension point.
     */
    public void registerListener(ExtensionPointRegistrationListener<? super T1, ? super T2> listener) {
        synchronized (listeners) {  
            listeners.add(listener);
        }
    }
    
    /**
     * Unregisters a change listener from this extension point.
     * 
     * @param listener listener that has previously been registered at this
     *  extension point.
     */
    public void unregisterListener(ExtensionPointRegistrationListener<? super T1, ? super T2> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    @SuppressWarnings("unchecked")
    private T1 thisToT1() {
        return (T1) this;
    }
}
