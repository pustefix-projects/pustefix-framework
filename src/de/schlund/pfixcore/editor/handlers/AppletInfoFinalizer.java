package de.schlund.pfixcore.editor.handlers;

import de.schlund.pfixcore.editor.*;
import de.schlund.pfixcore.editor.interfaces.*;
import de.schlund.pfixcore.editor.resources.*;
import de.schlund.pfixcore.generator.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixcore.workflow.Navigation.*;
import de.schlund.pfixcore.workflow.app.*;

import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;

import de.schlund.util.*;
import de.schlund.util.statuscodes.*;

import java.util.*;
import java.io.*;

import org.apache.xpath.*;

import org.w3c.dom.*;

import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixcore.util.*;
import de.schlund.util.FactoryInit;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import java.util.*;
import org.apache.log4j.*;
import org.w3c.dom.*;
import org.apache.xml.serialize.*;
import java.io.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import org.apache.xpath.*;

/**
 * @author zaich
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class AppletInfoFinalizer extends ResdocSimpleFinalizer {

	private String currdoc = null;
	private static DocumentBuilderFactory dbfac =
		DocumentBuilderFactory.newInstance();

	protected void renderDefault(IWrapperContainer container) throws Exception {


                System.out.println("Hallo -- Ich bin drin");

            
		Context context = container.getAssociatedContext();
		ContextResourceManager crm = context.getContextResourceManager();
		EditorSessionStatus esess = EditorRes.getEditorSessionStatus(crm);
		ResultDocument resdoc = container.getAssociatedResultDocument();		
		// TargetGenerator tgen = eprod.getTargetGenerator();

                System.out.println("Hallo -- Ich bin drin");
                
		// currdoc = esess.getCurrentDocumentationId();

		// esess.insertStatus(resdoc, resdoc.createNode("cr_editorsession"));

		Element root = resdoc.createNode("WeihnachtsmanninBlau");

		// String[] id = eprod.getDocumentation().getDocumentationIds();
		// String[] values = eprod.getDocumentation().getDocumentationValues();

		// this.renderAllDocumentation(id, root, container, values);


                  Element neu = resdoc.createSubNode(root, "stylesheet");
			neu.setAttribute("Bla", "Blabla");
                        

                

	}




    	public void onSuccess(IWrapperContainer container) throws Exception {
		renderDefault(container);
                System.out.println("Blaaaa on Succes");
	}


    
}
