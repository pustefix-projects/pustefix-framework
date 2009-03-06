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

package org.pustefixframework.config.directoutputservice.parser.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.pustefixframework.config.contextxmlservice.SSLOption;
import org.pustefixframework.config.contextxmlservice.parser.internal.ServletManagerConfigImpl;
import org.pustefixframework.config.directoutputservice.DirectOutputPageRequestConfig;
import org.pustefixframework.config.directoutputservice.DirectOutputServiceConfig;

import de.schlund.pfixxml.resources.FileResource;

/**
 * Stores configuration for a Pustefix servlet
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DirectOutputServiceConfigImpl extends ServletManagerConfigImpl implements
        SSLOption, DirectOutputServiceConfig {
    private final static Logger LOG = Logger.getLogger(DirectOutputServiceConfigImpl.class);

    private String servletName = null;

    private boolean editMode = false;

    private String externalName;
    
    private boolean sync = true;

    private String authConstraintRef;
    
    private HashMap<String, DirectOutputPageRequestConfig> pages = new HashMap<String, DirectOutputPageRequestConfig>();
    
    private List<DirectOutputPageRequestConfig> cachePages = null;
    
    private Set<FileResource> fileDependencies = new HashSet<FileResource>();

    private long loadTime = 0;

    public void setServletName(String name) {
        this.servletName = name;
    }

    public String getServletName() {
        return this.servletName;
    }

    public void setEditMode(boolean enabled) {
        this.editMode = enabled;
    }

    public boolean isEditMode() {
        return this.editMode;
    }

    public void setExternalServletName(String externalName) {
        this.externalName = externalName;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.DirectOutputServletConfig#getExternalServletName()
     */
    public String getExternalServletName() {
        return this.externalName;
    }
    
    public void setSynchronized(boolean sync) {
        this.sync = sync;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.DirectOutputServletConfig#isSynchronized()
     */
    public boolean isSynchronized() {
        return sync;
    }

    public void setAuthConstraintRef(String authConstraintRef) {
        this.authConstraintRef = authConstraintRef;
    }
    
    public String getAuthConstraintRef() {
        return authConstraintRef;
    }
    
    public void addPageRequest(DirectOutputPageRequestConfig config) {
        if (this.pages.containsKey(config.getPageName())) {
            LOG.warn("Overwriting configuration for direct output pagerequest" + config.getPageName());
        }
        this.pages.put(config.getPageName(), config);
        this.cachePages = null;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.DirectOutputServletConfig#getPageRequests()
     */
    public List<DirectOutputPageRequestConfig> getPageRequests() {
        List<DirectOutputPageRequestConfig> list = this.cachePages;
        if (list == null) {
            list = new ArrayList<DirectOutputPageRequestConfig>();
            for (Iterator<Entry<String, DirectOutputPageRequestConfig>> i = this.pages.entrySet().iterator(); i.hasNext();) {
                Entry<String, DirectOutputPageRequestConfig> entry = i.next();
                list.add(entry.getValue());
            }
            list = Collections.unmodifiableList(list);
            this.cachePages = list;
        }
        return list;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.DirectOutputServletConfig#getPageRequest(java.lang.String)
     */
    public DirectOutputPageRequestConfig getPageRequest(String page) {
        return this.pages.get(page);
    }
    
    public boolean needsReload() {
        for (FileResource file : fileDependencies) {
            if (file.lastModified() > loadTime) {
                return true;
            }
        }
        return false;
    }

}
