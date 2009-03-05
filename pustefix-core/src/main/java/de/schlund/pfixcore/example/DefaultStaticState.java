package de.schlund.pfixcore.example;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.app.StaticState;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;

public class DefaultStaticState extends StaticState {
    
    private TestData data;
    
    @Override
    public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {
        ResultDocument doc = super.getDocument(context, preq);
        doc.getRootElement().setAttribute("data", data.getText());
        return doc;
    }
    
    public void setData(TestData data) {
        this.data = data;
    }

}
