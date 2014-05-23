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

import org.apache.lucene.queryParser.ParseException;
import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.common.exception.EditorSearchIndexException;
import org.pustefixframework.editor.common.exception.EditorSearchQueryException;
import org.pustefixframework.editor.generated.EditorStatusCodes;
import org.pustefixframework.editor.webui.resources.ProjectsResource;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.lucefix.wrappers.Search;
import de.schlund.pfixcore.workflow.Context;

public class SearchHandler implements IHandler {

    private ProjectsResource projectsResource;
    
    private ContextSearch searchResource;

    public void handleSubmittedData(Context context, IWrapper wrapper) throws ParseException {
        Search search = (Search) wrapper;

        if (search.getDoit() != null && search.getDoit().booleanValue()) {
            String content = search.getContents();
            String tags = search.getTags();
            if (tags != null) {
                // replace ":" with "\:" - needed for searching for "pfx:button"
                tags = tags.replace(":", "\\:");
            }
            String attribKey = search.getAttribkeys();
            String attribValue = search.getAttribvalues();
            String comments = search.getComments();
            try {
                searchResource.search(content, tags, attribKey, attribValue, comments);
            } catch (EditorSearchIndexException e) {
                search.addSCodeDoit(EditorStatusCodes.LUCEFIX_INDEX_NOT_INITED);
            } catch (EditorSearchQueryException tmce) {
                search.addSCodeDoit(EditorStatusCodes.LUCEFIX_TOO_MANY_CLAUSES);
            }
        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
    }

    public boolean isActive(Context context) throws Exception {
        ProjectsResource pcon = projectsResource;
        if (pcon == null) return false;
        Project currentProject = pcon.getSelectedProject();
        return currentProject != null;
    }

    public boolean needsData(Context context) throws Exception {
        return false;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    @Inject
    public void setProjectsResource(ProjectsResource projectsResource) {
        this.projectsResource = projectsResource;
    }
    
    @Inject
    public void setSearchResource(ContextSearch searchResource) {
        this.searchResource = searchResource;
    }
}
