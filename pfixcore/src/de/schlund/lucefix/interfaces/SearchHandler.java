package de.schlund.lucefix.interfaces;

import de.schlund.lucefix.ContextSearch;
import de.schlund.pfixcore.editor.handlers.EditorStdHandler;
import de.schlund.pfixcore.editor.resources.EditorRes;
import de.schlund.pfixcore.editor.resources.EditorSearch;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;

public class SearchHandler extends EditorStdHandler {

    private static final String CSEARCH = "de.schlund.lucefix.ContextSearch";

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        
        
        ContextSearch csearch = (ContextSearch) context.getContextResourceManager().getResource(
                CSEARCH);
        Search search = (Search) wrapper;


        if (search.getDoit() != null && search.getDoit().booleanValue()) {

            String content = search.getContents();
            String tags = search.getTags();
            String attribKey = search.getAttribkeys();
            String attribValue = search.getAttribvalues();
            String comments = search.getComments();

            csearch.search(content, tags, attribKey, attribValue, comments);
        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        // TODO Auto-generated method stub

    }
}
