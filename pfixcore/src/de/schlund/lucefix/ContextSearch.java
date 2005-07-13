package de.schlund.lucefix;

import java.io.IOException;

import org.apache.lucene.queryParser.ParseException;

import de.schlund.pfixcore.workflow.ContextResource;

public interface ContextSearch extends ContextResource {
    
    public void search(String content, String tags, String attribkey, String attribvalue, String comments) throws IOException, ParseException;

}
