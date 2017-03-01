package org.pustefixframework.example;

import javax.servlet.http.HttpSession;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.app.StaticState;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.ResultDocument;

public class ParameterState extends StaticState {

    @Override
    public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {

        ResultDocument resDoc = super.getDocument(context, preq);
        Document doc = resDoc.getSPDocument().getDocument();
        RequestParam param = preq.getRequestParam("foo");
        if(param != null) {
            Element elem = doc.createElement("param");
            elem.setAttribute("name", "foo");
            elem.setTextContent(param.getValue());
            doc.getDocumentElement().appendChild(elem);
        }
        HttpSession session = preq.getSession(false);
        Integer counter = (Integer)session.getAttribute("counter");
        if(counter == null) {
            counter = new Integer(1);
        } else {
            counter += 1;
        }
        session.setAttribute("counter", counter);
        Element elem = doc.createElement("counter");
        elem.setTextContent(String.valueOf(counter));
        doc.getDocumentElement().appendChild(elem);
        return resDoc;
    }

}
