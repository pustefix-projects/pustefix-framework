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
import java.util.Vector;

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

import de.schlund.pfixcore.beans.InitResource;
import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.frontend.resources.ProjectsResource;
import de.schlund.pfixcore.editor2.frontend.util.EditorResourceLocator;
import de.schlund.pfixcore.workflow.Context;

public class ContextSearchImpl implements ContextSearch {

    private final static PerFieldAnalyzerWrapper analyzer = PreDoc.ANALYZER;

    private Context context;
    private Query   lastQuery;
    private Hit[]   hits;
    String          content, tags, attribkey, attribvalue, comments;

    public void resetData() {
        content = null;
        tags = null;
        attribkey = null;
        attribvalue = null;
        comments = null;
        hits = null;
        lastQuery = null;
    }

    public String getAttribkey() {
        return attribkey;
    }

    public String getAttribvalue() {
        return attribvalue;
    }

    public String getComments() {
        return comments;
    }

    public String getContent() {
        return content;
    }

    public String getTags() {
        return tags;
    }

    public void search(String content, String tags, String attribkey, String attribvalue, String comments) throws IOException, ParseException {
        IndexReader reader = IndexReader.open(PfixQueueManager.lucene_data_path);
        this.content = content;
        this.tags = tags;
        this.attribkey = attribkey;
        this.attribvalue = attribvalue;
        this.comments = comments;

        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery query = new BooleanQuery();
        if (content != null) query.add(QueryParser.parse(content, PreDoc.CONTENTS, analyzer), true, false);
        if (tags != null) query.add(QueryParser.parse(tags, PreDoc.TAGS, analyzer), true, false);
        if (attribkey != null) query.add(QueryParser.parse(attribkey, PreDoc.ATTRIBKEYS, analyzer), true, false);
        if (attribvalue != null) query.add(QueryParser.parse(attribvalue, PreDoc.ATTRIBVALUES, analyzer), true, false);
        if (comments != null) query.add(QueryParser.parse(comments, PreDoc.COMMENTS, analyzer), true, false);
        Hits hits = searcher.search(query);
        transformHits(hits);
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

    @InitResource
    public void init(Context context) throws Exception {
        this.context = context;
        resetData();
    }

    private void transformHits(Hits hits) throws IOException {
        ProjectsResource pcon = EditorResourceLocator.getProjectsResource(context);
        Project currentProject = pcon.getSelectedProject();

        Document doc;
        Vector<Hit> temp = new Vector<Hit>();
        String[] token;
        for (int i = 0; i < hits.length(); i++) {
            doc = hits.doc(i);

            token = splitPath(doc.get(PreDoc.PATH));
            if (currentProject != null && currentProject.hasIncludePart(token[0], token[1], token[2]) == false) {
                continue;
            }
            temp.add(new Hit(doc, hits.score(i)));
        }
        this.hits = temp.toArray(new Hit[0]);
    }

    @InsertStatus
    public void serializeSearch(Element elem) throws Exception {
        if (hits != null) {
            Element newelem;
            for (Hit doc : hits) {
                newelem = elem.getOwnerDocument().createElement("hit");
                elem.appendChild(newelem);
                newelem.setAttribute("score", doc.getScore() + "");
                newelem.setAttribute("filename", doc.getFilename());
                newelem.setAttribute("part", doc.getPartname());
                newelem.setAttribute("product", doc.getProductname());
                newelem.setAttribute("path", doc.getPath());
            }
        }
        if (lastQuery != null) elem.setAttribute("lastQuery", lastQuery.toString());
    }

    private class Hit {
        private double score;
        private String filename;
        private String partname;
        private String productname;

        /**
         * @param filename
         * @param partname
         * @param productname
         * @param score
         */
        public Hit(String filename, String partname, String productname, double score) {
            this.filename = filename;
            this.partname = partname;
            this.productname = productname;
            this.score = score;
        }

        public Hit(Document lucenedoc, double score) {
            String[] token = splitPath(lucenedoc.get(PreDoc.PATH));
            this.filename = token[0];
            this.partname = token[1];
            this.productname = token[2];
            this.score = score;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getPartname() {
            return partname;
        }

        public void setPartname(String partname) {
            this.partname = partname;
        }

        public String getProductname() {
            return productname;
        }

        public void setProductname(String productname) {
            this.productname = productname;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public String getPath() {
            StringBuffer sb = new StringBuffer(getFilename());
            sb.append("/");
            sb.append(getPartname());
            sb.append("/");
            sb.append(getProductname());
            return sb.toString();
        }
    }
}
