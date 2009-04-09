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

package org.pustefixframework.editor.webui.remote.dom.util;

import java.net.MalformedURLException;

import org.pustefixframework.editor.common.remote.service.RemoteDynIncludeService;
import org.pustefixframework.editor.common.remote.service.RemoteImageService;
import org.pustefixframework.editor.common.remote.service.RemoteIncludeService;
import org.pustefixframework.editor.common.remote.service.RemotePageService;
import org.pustefixframework.editor.common.remote.service.RemoteProjectService;
import org.pustefixframework.editor.common.remote.service.RemoteSearchService;
import org.pustefixframework.editor.common.remote.service.RemoteTargetService;

import com.caucho.hessian.client.HessianProxyFactory;


public class RemoteServiceUtil {
    private RemoteIncludeService remoteIncludeService;
    private RemoteDynIncludeService remoteDynIncludeService;
    private RemoteImageService remoteImageService;
    private RemoteProjectService remoteProjectService;
    private RemotePageService remotePageService;
    private RemoteTargetService remoteTargetService;
    private RemoteSearchService remoteSearchService;
    
    public RemoteServiceUtil(String location, String password) {
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        HessianProxyFactory factory = new HessianProxyFactory();
        factory.setUser("hessian");
        factory.setPassword(password);
        setRemoteDynIncludeService(createProxyObject(factory, location, RemoteDynIncludeService.class));
        setRemoteImageService(createProxyObject(factory, location, RemoteImageService.class));
        setRemoteIncludeService(createProxyObject(factory, location, RemoteIncludeService.class));
        setRemotePageService(createProxyObject(factory, location, RemotePageService.class));
        setRemoteProjectService(createProxyObject(factory, location, RemoteProjectService.class));
        setRemoteSearchService(createProxyObject(factory, location, RemoteSearchService.class));
        setRemoteTargetService(createProxyObject(factory, location, RemoteTargetService.class));
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxyObject(HessianProxyFactory factory, String location, Class<? extends T> proxyInterface) {
        try {
            return (T) factory.create(proxyInterface, location + "xml/" + proxyInterface.getName());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unexpected error while creating web service proxy", e);
        }
    }
    
    private void setRemoteIncludeService(RemoteIncludeService remoteIncludeService) {
        this.remoteIncludeService = remoteIncludeService;
    }
    public RemoteIncludeService getRemoteIncludeService() {
        return remoteIncludeService;
    }
    private void setRemoteDynIncludeService(RemoteDynIncludeService remoteDynIncludeService) {
        this.remoteDynIncludeService = remoteDynIncludeService;
    }
    public RemoteDynIncludeService getRemoteDynIncludeService() {
        return remoteDynIncludeService;
    }
    private void setRemoteImageService(RemoteImageService remoteImageService) {
        this.remoteImageService = remoteImageService;
    }
    public RemoteImageService getRemoteImageService() {
        return remoteImageService;
    }
    private void setRemoteProjectService(RemoteProjectService remoteProjectService) {
        this.remoteProjectService = remoteProjectService;
    }
    public RemoteProjectService getRemoteProjectService() {
        return remoteProjectService;
    }
    private void setRemotePageService(RemotePageService remotePageService) {
        this.remotePageService = remotePageService;
    }
    public RemotePageService getRemotePageService() {
        return remotePageService;
    }
    private void setRemoteTargetService(RemoteTargetService remoteTargetService) {
        this.remoteTargetService = remoteTargetService;
    }
    public RemoteTargetService getRemoteTargetService() {
        return remoteTargetService;
    }
    private void setRemoteSearchService(RemoteSearchService remoteSearchService) {
        this.remoteSearchService = remoteSearchService;
    }
    public RemoteSearchService getRemoteSearchService() {
        return remoteSearchService;
    }
}
