package de.schlund.lucefix;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.w3c.dom.Element;

import de.schlund.lucefix.core.PfixQueueManager;
import de.schlund.lucefix.core.PreDoc;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

public class ContextSearchImpl implements ContextSearch {
    
    private static Analyzer analyzer = new GermanAnalyzer();
    private Hits hits;
    private Query lastQuery;



    public void search(String content, String tags, String attribkey, String attribvalue, String comments) throws IOException, ParseException {
        IndexReader reader = IndexReader.open(PfixQueueManager.lucene_data_path);
        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery query = new BooleanQuery();
        if (content != null)
            query.add(QueryParser.parse(content, PreDoc.CONTENTS,analyzer),true,false);
        if (tags != null)
            query.add(QueryParser.parse(tags,PreDoc.TAGS,analyzer),true,false);
        if (attribkey != null)
            query.add(QueryParser.parse(attribkey,PreDoc.ATTRIBKEYS,analyzer),true,false);
        if (attribvalue != null)
            query.add(QueryParser.parse(attribvalue,PreDoc.ATTRIBVALUES,analyzer),true,false);
        if (comments != null)
            query.add(QueryParser.parse(comments,PreDoc.COMMENTS,analyzer),true,false);
        hits = searcher.search(query);
        lastQuery = query;
        
        
        
    }

    public void init(Context context) throws Exception {
        reset();
    }

    public void insertStatus(ResultDocument resdoc, Element elem) throws Exception {
        if (hits != null) {
            int hitSize = hits.length();
            Element newelem;
            Document doc;
            for (int i = 0; i < hitSize; i++){
                doc = hits.doc(i);
                newelem = resdoc.createSubNode(elem,"hit");
                newelem.setAttribute("index", i+"");
                newelem.setAttribute("contents", doc.get(PreDoc.CONTENTS));
                newelem.setAttribute("comments", doc.get(PreDoc.COMMENTS));
                newelem.setAttribute("tags",doc.get(PreDoc.TAGS));
                newelem.setAttribute("attribkeys", doc.get(PreDoc.ATTRIBKEYS));
                newelem.setAttribute("attribvalues", doc.get(PreDoc.ATTRIBVALUES));
                newelem.setAttribute("filename", doc.get(PreDoc.FILENAME));
                newelem.setAttribute("path", doc.get(PreDoc.PATH));
            }
        }
        if (lastQuery != null)
            elem.setAttribute("lastQuery", lastQuery.toString());
        
    }

    public void reset() throws Exception {
        hits = null;
        lastQuery = null;
    }

}
