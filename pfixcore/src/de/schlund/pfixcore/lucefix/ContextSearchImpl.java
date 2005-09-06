/*
 * This file is part of PFIXCORE.
 * 
 * PFIXCORE is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * PFIXCORE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package de.schlund.pfixcore.lucefix;

import java.io.IOException;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.w3c.dom.Element;

import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.frontend.resources.ProjectsResource;
import de.schlund.pfixcore.editor2.frontend.util.EditorResourceLocator;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

public class ContextSearchImpl implements ContextSearch {

    private static PerFieldAnalyzerWrapper analyzer   = PreDoc.ANALYZER;

    private Context                        context;
    private Hits                           hits;
    private Query                          lastQuery;
    private boolean                        smallScope = true;



    public void search(String content, String tags, String attribkey, String attribvalue, String comments)
            throws IOException, ParseException {
        IndexReader reader = IndexReader.open(PfixQueueManager.lucene_data_path);
        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery query = new BooleanQuery();
        if (content != null) query.add(QueryParser.parse(content, PreDoc.CONTENTS, analyzer), true, false);
        if (tags != null) query.add(QueryParser.parse(tags, PreDoc.TAGS, analyzer), true, false);
        if (attribkey != null) query.add(QueryParser.parse(attribkey, PreDoc.ATTRIBKEYS, analyzer), true, false);
        if (attribvalue != null) query.add(QueryParser.parse(attribvalue, PreDoc.ATTRIBVALUES, analyzer), true, false);
        if (comments != null) query.add(QueryParser.parse(comments, PreDoc.COMMENTS, analyzer), true, false);
        hits = searcher.search(query);
        lastQuery = query;     
    }

    private static String[] splitPath(String input) {
        String[] retval = new String[3];
        int letzterSlash = input.lastIndexOf("/");
        int vorletzterSlash = input.lastIndexOf("/", letzterSlash - 1);
        retval[0] = input.substring(0, vorletzterSlash);
        retval[1] = input.substring(vorletzterSlash + 1, letzterSlash);
        retval[2] = input.substring(letzterSlash + 1);
        return retval;
    }




    public void init(Context context) throws Exception {
        this.context = context;
        reset();
    }

    public void insertStatus(ResultDocument resdoc, Element elem) throws Exception {
        ProjectsResource pcon = EditorResourceLocator.getProjectsResource(context);
        Project currentProject = pcon.getSelectedProject();

        if (hits != null) {
            int hitSize = hits.length();
            Element newelem;
            Document doc;
            for (int i = 0; i < hitSize; i++) {
                doc = hits.doc(i);
                String[] tokens = splitPath(doc.get(PreDoc.PATH));

                if (smallScope) {
                    // ignore hits from different projects
                    if (currentProject.findIncludePartThemeVariant(tokens[0], tokens[1], tokens[2]) == null) continue;
                }


                newelem = resdoc.createSubNode(elem, "hit");
                newelem.setAttribute("index", i + "");
                newelem.setAttribute("contents", doc.get(PreDoc.CONTENTS));
                newelem.setAttribute("comments", doc.get(PreDoc.COMMENTS));
                newelem.setAttribute("tags", doc.get(PreDoc.TAGS));
                newelem.setAttribute("attribkeys", doc.get(PreDoc.ATTRIBKEYS));
                newelem.setAttribute("attribvalues", doc.get(PreDoc.ATTRIBVALUES));
                newelem.setAttribute("filename", doc.get(PreDoc.FILENAME));
                newelem.setAttribute("path", doc.get(PreDoc.PATH));
                newelem.setAttribute("score", hits.score(i) + "");
                newelem.setAttribute("product", tokens[2]);
                newelem.setAttribute("part", tokens[1]);
            }
        }
        if (lastQuery != null) elem.setAttribute("lastQuery", lastQuery.toString());

    }

    public void reset() throws Exception {
        hits = null;
        lastQuery = null;
    }

}
