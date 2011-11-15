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

package de.schlund.pfixcore.lucefix;

import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.common.exception.EditorSearchIndexException;
import org.pustefixframework.editor.common.exception.EditorSearchQueryException;
import org.pustefixframework.editor.common.remote.transferobjects.SearchResultRecordTO;
import org.pustefixframework.editor.common.remote.transferobjects.SearchResultTO;
import org.pustefixframework.editor.webui.resources.ProjectsResource;
import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixcore.editor2.core.spring.ProjectPool;

public class ContextSearch {

    private ProjectsResource projectsResource;

    private ProjectPool projectPool;
    
    private SearchResultTO searchResult;

    @Inject
    public void setProjectsResource(ProjectsResource projectsResource) {
        this.projectsResource = projectsResource;
    }

    @Inject
    public void setProjectPool(ProjectPool projectPool) {
        this.projectPool = projectPool;
    }
    
    public void resetData() {
        this.searchResult = null;
    }
    
    public void search(String content, String tags, String attributeKey, String attributeValue, String comment) throws EditorSearchIndexException, EditorSearchQueryException {
        Project project = projectsResource.getSelectedProject();
        if (project == null) {
            return;
        }
        
        searchResult = projectPool.getRemoteServiceUtil(project).getRemoteSearchService().search(content, tags, attributeKey, attributeValue, comment);
    }

    @InsertStatus
    public void insertStatus(Element elem) throws Exception {
        if (searchResult != null) {
            Element newelem;
            for (SearchResultRecordTO r : searchResult.records) {
                newelem = elem.getOwnerDocument().createElement("hit");
                elem.appendChild(newelem);
                newelem.setAttribute("score", r.score + "");
                newelem.setAttribute("filename", r.filename);
                newelem.setAttribute("part", r.part);
                newelem.setAttribute("product", r.theme);
                newelem.setAttribute("path", r.path);
            }
            elem.setAttribute("lastQuery", searchResult.query);
        }
    }

}
