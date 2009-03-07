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

package org.pustefixframework.editor.backend.remote;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.common.exception.EditorSearchIndexException;
import org.pustefixframework.editor.common.exception.EditorSearchQueryException;
import org.pustefixframework.editor.common.remote.service.RemoteSearchService;
import org.pustefixframework.editor.common.remote.transferobjects.SearchResultRecordTO;
import org.pustefixframework.editor.common.remote.transferobjects.SearchResultTO;

import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;
import de.schlund.pfixcore.lucefix.PfixQueueManager;
import de.schlund.pfixcore.lucefix.PreDoc;


public class RemoteSearchServiceImpl implements RemoteSearchService {
    
    private ProjectFactoryService projectFactoryService;
    
    private final static PerFieldAnalyzerWrapper analyzer = PreDoc.ANALYZER;
    
    @Inject
    public void setProjectFactoryService(ProjectFactoryService projectFactoryService) {
        this.projectFactoryService = projectFactoryService;
    }

    public SearchResultTO search(String content, String tags, String attributeKey, String attributeValue, String comment) throws EditorSearchIndexException, EditorSearchQueryException {
        BooleanQuery query = new BooleanQuery();
        Hit[] hits;
        try {
            IndexReader reader = IndexReader.open(PfixQueueManager.lucene_data_path);
            IndexSearcher searcher = new IndexSearcher(reader);
            
            if (content != null) query.add(QueryParser.parse(content, PreDoc.CONTENTS, analyzer), true, false);
            if (tags != null) query.add(QueryParser.parse(tags, PreDoc.TAGS, analyzer), true, false);
            if (attributeKey != null) query.add(QueryParser.parse(attributeKey, PreDoc.ATTRIBKEYS, analyzer), true, false);
            if (attributeValue != null) query.add(QueryParser.parse(attributeValue, PreDoc.ATTRIBVALUES, analyzer), true, false);
            if (comment != null) query.add(QueryParser.parse(comment, PreDoc.COMMENTS, analyzer), true, false);
            Hits hits2 = searcher.search(query);
            
            hits = transformHits(hits2);
        } catch (IOException e) {
            throw new EditorSearchIndexException(e);
        } catch (ParseException e) {
            throw new EditorSearchQueryException(e);
        }

        LinkedList<SearchResultRecordTO> results = new LinkedList<SearchResultRecordTO>();
        for (Hit hit : hits) {
            SearchResultRecordTO to = new SearchResultRecordTO();
            to.filename = hit.getFilename();
            to.path = hit.getPath();
            to.part = hit.getPartname();
            to.theme = hit.getProductname();
            to.score = hit.getScore();
            results.add(to);
        }
        SearchResultTO to = new SearchResultTO();
        to.records = results;
        to.query = query.toString();
        
        return to;
    }
    
    private Hit[] transformHits(Hits hits) throws IOException {
        Document doc;
        Vector<Hit> temp = new Vector<Hit>();
        String[] token;
        for (int i = 0; i < hits.length(); i++) {
            doc = hits.doc(i);

            token = splitPath(doc.get(PreDoc.PATH));
            if (projectFactoryService.getProject().hasIncludePart(token[0], token[1], token[2]) == false) {
                continue;
            }
            temp.add(new Hit(doc, hits.score(i)));
        }
        return temp.toArray(new Hit[0]);
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
