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
 *
 */

package de.schlund.pfixcore.lucefix;

import java.io.IOException;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.generated.EditorStatusCodes;

import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.frontend.resources.ProjectsResource;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.lucefix.wrappers.Search;
import de.schlund.pfixcore.workflow.Context;

public class SearchHandler implements IHandler {

    private static final String CSEARCH = "de.schlund.pfixcore.lucefix.ContextSearch";
    
    private ProjectsResource projectsResource;

    public void handleSubmittedData(Context context, IWrapper wrapper) throws ParseException {

        ContextSearch csearch = (ContextSearch) context.getContextResourceManager().getResource(CSEARCH);
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
                csearch.search(content, tags, attribKey, attribValue, comments);
            } catch (IOException e) {
                search.addSCodeDoit(EditorStatusCodes.LUCEFIX_INDEX_NOT_INITED);
            } catch (BooleanQuery.TooManyClauses tmce) {
                search.addSCodeDoit(EditorStatusCodes.LUCEFIX_TOO_MANY_CLAUSES);
            }
        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        ContextSearch csearch = (ContextSearch) context.getContextResourceManager().getResource(CSEARCH);
        Search search = (Search) wrapper;

        String tmp;

        if ((tmp = csearch.getAttribkey()) != null) {
            search.setAttribkeys(tmp);
        }
        if ((tmp = csearch.getAttribvalue()) != null) {
            search.setAttribvalues(tmp);
        }
        if ((tmp = csearch.getContent()) != null) {
            search.setContents(tmp);
        }
        if ((tmp = csearch.getComments()) != null) {
            search.setComments(tmp);
        }
        if ((tmp = csearch.getTags()) != null) {
            search.setTags(tmp);
        }
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
}
