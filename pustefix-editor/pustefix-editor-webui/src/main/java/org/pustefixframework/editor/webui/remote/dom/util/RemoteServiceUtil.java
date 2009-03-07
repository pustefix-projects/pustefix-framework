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

package org.pustefixframework.editor.webui.remote.dom.util;

import org.pustefixframework.editor.common.remote.service.RemoteDynIncludeService;
import org.pustefixframework.editor.common.remote.service.RemoteImageService;
import org.pustefixframework.editor.common.remote.service.RemoteIncludeService;
import org.pustefixframework.editor.common.remote.service.RemotePageService;
import org.pustefixframework.editor.common.remote.service.RemoteProjectService;
import org.pustefixframework.editor.common.remote.service.RemoteSearchService;
import org.pustefixframework.editor.common.remote.service.RemoteTargetService;


public class RemoteServiceUtil {
    private RemoteIncludeService remoteIncludeService;
    private RemoteDynIncludeService remoteDynIncludeService;
    private RemoteImageService remoteImageService;
    private RemoteProjectService remoteProjectService;
    private RemotePageService remotePageService;
    private RemoteTargetService remoteTargetService;
    private RemoteSearchService remoteSearchService;
    
    public void setRemoteIncludeService(RemoteIncludeService remoteIncludeService) {
        this.remoteIncludeService = remoteIncludeService;
    }
    public RemoteIncludeService getRemoteIncludeService() {
        return remoteIncludeService;
    }
    public void setRemoteDynIncludeService(RemoteDynIncludeService remoteDynIncludeService) {
        this.remoteDynIncludeService = remoteDynIncludeService;
    }
    public RemoteDynIncludeService getRemoteDynIncludeService() {
        return remoteDynIncludeService;
    }
    public void setRemoteImageService(RemoteImageService remoteImageService) {
        this.remoteImageService = remoteImageService;
    }
    public RemoteImageService getRemoteImageService() {
        return remoteImageService;
    }
    public void setRemoteProjectService(RemoteProjectService remoteProjectService) {
        this.remoteProjectService = remoteProjectService;
    }
    public RemoteProjectService getRemoteProjectService() {
        return remoteProjectService;
    }
    public void setRemotePageService(RemotePageService remotePageService) {
        this.remotePageService = remotePageService;
    }
    public RemotePageService getRemotePageService() {
        return remotePageService;
    }
    public void setRemoteTargetService(RemoteTargetService remoteTargetService) {
        this.remoteTargetService = remoteTargetService;
    }
    public RemoteTargetService getRemoteTargetService() {
        return remoteTargetService;
    }
    public void setRemoteSearchService(RemoteSearchService remoteSearchService) {
        this.remoteSearchService = remoteSearchService;
    }
    public RemoteSearchService getRemoteSearchService() {
        return remoteSearchService;
    }
}
