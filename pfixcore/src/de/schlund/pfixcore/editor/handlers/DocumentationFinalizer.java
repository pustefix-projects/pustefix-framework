package de.schlund.pfixcore.editor.handlers;

import de.schlund.pfixcore.editor.*;
import de.schlund.pfixcore.editor.resources.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixcore.workflow.app.*;

import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;

import org.w3c.dom.*;

/**
 * @author zaich
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class DocumentationFinalizer extends ResdocSimpleFinalizer {

	private String currdoc = null;
	protected void renderDefault(IWrapperContainer container) throws Exception {
            
            Context context = container.getAssociatedContext();
            ContextResourceManager crm = context.getContextResourceManager();
            EditorSessionStatus esess = EditorRes.getEditorSessionStatus(crm);
            ResultDocument resdoc = container.getAssociatedResultDocument();
            EditorProduct eprod = esess.getProduct();
            TargetGenerator tgen = eprod.getTargetGenerator();
            currdoc = esess.getCurrentDocumentationId();
            
            esess.insertStatus(resdoc, resdoc.createNode("cr_editorsession"));
            
            Element root = resdoc.createNode("all_documentation");
            
            String[] id = eprod.getDocumentation().getDocumentationIds();
            String[] values = eprod.getDocumentation().getDocumentationValues();
            
            this.renderAllDocumentation(id, root, container, values);
            
	}

	private void renderAllDocumentation(String[] docs, Element root, IWrapperContainer container, String[] values)
            throws Exception {
            
            Context context = container.getAssociatedContext();
            ContextResourceManager crm = context.getContextResourceManager();
            EditorSessionStatus esess = EditorRes.getEditorSessionStatus(crm);
            ResultDocument resdoc = container.getAssociatedResultDocument();
            EditorProduct eprod = esess.getProduct();
            TargetGenerator tgen = eprod.getTargetGenerator();
            currdoc = esess.getCurrentDocumentationId();

            Element elem = null;

            if (docs.length > 0) {
                String oldstring = docs[0].substring(0, docs[0].indexOf("%"));
                    
                Element neu = resdoc.createSubNode(root, "stylesheet");
                neu.setAttribute("file", oldstring);
                    
                for (int i = 0; i < docs.length; i++) {
                        
                    String newstring = docs[i].substring(0, docs[i].indexOf("%"));
                        
                    if (!oldstring.equals(newstring)) {
                        neu = resdoc.createSubNode(root, "stylesheet");
                        neu.setAttribute("file", newstring);
                        oldstring = newstring;
                    }

                    String            key     = docs[i];
                    CoreDocumentation coredoc = eprod.getDocumentation().getCoreDocumentationForId(key);
                    elem = resdoc.createSubNode(neu, "template_doc");
                    elem.setAttribute("id", key);
                    elem.setAttribute("value", coredoc.getValue());
                    elem.setAttribute("name", coredoc.getName());
                    elem.setAttribute("match", coredoc.getMatch());
                    elem.setAttribute("mode", coredoc.getMode());
                    
                    if (eprod.getDocumentation().getDocumentationNode(docs[i]).getLength() < 1) {
                        elem.setAttribute("doku", "notfound");
                    }
                    
                    // Add nodelist if currdoc = id;
                    if (docs[i].equals(currdoc)) {
                        this.setCurrentDocumentation(container, elem);
                    }
                }
            }
	}

    public void setCurrentDocumentation(IWrapperContainer container, Element root)
        throws Exception {

        Context context = container.getAssociatedContext();
        ContextResourceManager crm = context.getContextResourceManager();
        EditorSessionStatus esess = EditorRes.getEditorSessionStatus(crm);
        ResultDocument resdoc = container.getAssociatedResultDocument();
        EditorProduct eprod = esess.getProduct();
        TargetGenerator tgen = eprod.getTargetGenerator();
        String tmpdoc = esess.getCurrentDocumentationId();

        if (tmpdoc != null) {

            Element elStylesheet = ResultDocument.addTextChild(root, "stylesheet", eprod.getDocumentation().getSimpleStylesheet(tmpdoc));
					
            Element elMode = ResultDocument.addTextChild(root, "mode", eprod.getDocumentation().getMode(tmpdoc));
				
            Element elModus = ResultDocument.addTextChild(root, "modus", eprod.getDocumentation().getModus(tmpdoc));

            NodeList nlist = eprod.getDocumentation().getDocumentationNode(tmpdoc);
            Document doc = eprod.getDocumentation().getCurrentDoc(tmpdoc);

            root.setAttribute("active", "true");

            for (int count = 0; count < nlist.getLength(); count++) {

                Node node = nlist.item(count);

                Document incdoc = root.getOwnerDocument();
                Node newnode;
                try {
                    newnode = incdoc.importNode(node, true);
                    root.appendChild(newnode);
                } catch (Exception exc) {
                    // System.out.println("Hallo" + exc.getMessage());
                }

            } // end for-schleife  
        } // end currdoc != null
    }

    public void onSuccess(IWrapperContainer container) throws Exception {
        renderDefault(container);
    }

}
