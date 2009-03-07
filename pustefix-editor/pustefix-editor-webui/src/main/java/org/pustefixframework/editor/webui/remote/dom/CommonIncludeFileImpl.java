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

package org.pustefixframework.editor.webui.remote.dom;

import java.util.Collection;
import java.util.LinkedList;

import org.pustefixframework.editor.common.dom.AbstractIncludeFile;
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
        return (Document) (new XMLSerializer()).deserializeNode(getRemoteService().getIncludeFileXML(path, forceUpdate));
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
    
    protected abstract RemoteCommonIncludeService getRemoteService();
    
    protected abstract IncludePart newIncludePartInstance(String name);
    
}
