package de.schlund.pfixcore.editor.handlers;



import java.util.ArrayList;

import org.apache.oro.text.perl.Perl5Util;
import org.w3c.dom.Element;

import de.schlund.pfixcore.editor.resources.CRTestcase;
import de.schlund.pfixcore.editor.resources.EditorRes;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixcore.workflow.app.IWrapperContainer;
import de.schlund.pfixcore.workflow.app.ResdocSimpleFinalizer;
import de.schlund.pfixxml.ResultDocument;

/**
 * @author jh
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TestcaseOverviewFinalizer extends ResdocSimpleFinalizer {

    /**
     * @see de.schlund.pfixcore.workflow.app.ResdocSimpleFinalizer#renderDefault(IWrapperContainer)
     */
    protected void renderDefault(IWrapperContainer container)
        throws Exception {
        Context context = container.getAssociatedContext();
        ContextResourceManager crm = context.getContextResourceManager();
        CRTestcase crtc = EditorRes.getCRTestcase(crm);
        ResultDocument resdoc = container.getAssociatedResultDocument();
        
        ArrayList results = crtc.getTestResult();
//        System.out.println("Result:"+results.size()+"-->"+((String[])results.get(0))[0]);
        Element ele = resdoc.createNode("test_results");
        for(int i = 0; i < results.size(); i++) {
            Element el = resdoc.createNode("Test");
            el.setAttribute("id", ""+i);
            String[] text = (String[]) results.get(i);
            for(int j=0; j<text.length; j++) {
                //System.out.println(j+"--->"+text[j]);
                String str = text[j];
                Perl5Util perl = new Perl5Util();
                ArrayList lines = new ArrayList();
                perl.split(lines, "/\n/", str);
                Element elem = resdoc.createNode("step");
                elem.setAttribute("id", ""+j);
                for(int k=0; k<lines.size(); k++) {
                    // skip empty lines
                    if(((String) lines.get(k)).equals("")) continue;
                    Element e = resdoc.addTextChild(ele, "line", (String)lines.get(k));
                    e.setAttribute("id", ""+k);
                    elem.appendChild(e); 
                }
                el.appendChild(elem);
            }
            ele.appendChild(el);
        }
    }

}
