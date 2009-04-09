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

package org.pustefixframework.editor.webui.remote.dom;

import java.util.Collection;
import java.util.LinkedList;

import org.pustefixframework.editor.common.dom.AbstractIncludeFile;
import org.pustefixframework.editor.common.dom.IncludeFile;
import org.pustefixframework.editor.common.dom.IncludePart;
import org.pustefixframework.editor.common.remote.service.RemoteCommonIncludeService;
import org.pustefixframework.editor.common.remote.transferobjects.IncludeFileTO;
import org.pustefixframework.editor.common.util.XMLSerializer;
import org.pustefixframework.editor.webui.remote.dom.util.RemoteServiceUtil;
import org.w3c.dom.Document;



public abstract class CommonIncludeFileImpl extends AbstractIncludeFile {
    
    protected RemoteServiceUtil remoteServiceUtil;
    protected String path;

    public CommonIncludeFileImpl(RemoteServiceUtil remoteServiceUtil, String path) {
        this.remoteServiceUtil = remoteServiceUtil;
        this.path = path;
    }
    
    public Document getContentXML() {
        return this.getContentXML(false);
    }
    
    public Document getContentXML(boolean forceUpdate) {
        String xml = getRemoteService().getIncludeFileXML(path, forceUpdate);
        if (xml != null) {
            return (Document) (new XMLSerializer()).deserializeNode(xml);
        } else {
            return null;
        }
    }
    
    public String getPath() {
        return path;
    }
    
    public IncludePart createPart(String name) {
        return newIncludePartInstance(name);
    }

    public IncludePart getPart(String name) {
        if (hasPart(name)) {
            return newIncludePartInstance(name);
        }
        return null;
    }

    public Collection<IncludePart> getParts() {
        LinkedList<IncludePart> parts = new LinkedList<IncludePart>();
        for (String partName : getIncludeFileTO().parts) {
            parts.add(newIncludePartInstance(partName));
        }
        return parts;
    }

    public boolean hasPart(String name) {
        return getIncludeFileTO().parts.contains(name);
    }
    
    public long getSerial() {
        return getIncludeFileTO().serial;
    }
    
    protected IncludeFileTO getIncludeFileTO() {
        return getRemoteService().getIncludeFile(path);
    }
    
    @Override
    public int compareTo(IncludeFile file) {
        if (file instanceof CommonIncludeFileImpl) {
            CommonIncludeFileImpl f = (CommonIncludeFileImpl) file;
            if (this.remoteServiceUtil.equals(f.remoteServiceUtil)) {
                return super.compareTo(file);
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof CommonIncludeFileImpl) {
            CommonIncludeFileImpl f = (CommonIncludeFileImpl) obj;
            return this.remoteServiceUtil.equals(f.remoteServiceUtil);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return ("INCLUDEFILE: " + super.hashCode() + remoteServiceUtil.hashCode()).hashCode();
    }

    protected abstract RemoteCommonIncludeService getRemoteService();
    
    protected abstract IncludePart newIncludePartInstance(String name);
    
}
