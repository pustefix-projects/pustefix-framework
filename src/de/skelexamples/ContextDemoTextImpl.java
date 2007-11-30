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
 *
 */

package de.skelexamples;

import org.w3c.dom.Element;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

/**
 * A class representing our ContextResource. It has been createdto store the
 * text sent by the form in order to give it out on the second page.
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude </a> 
 * @version $Id$
 */
public class ContextDemoTextImpl implements ContextDemoText {

    /** A private String containing the content of the textfield */
    private String demoText = null;

    /** Setting demoText with the String sent by the form */
    public void setDemoText(String formText) {
        System.out.println("ContextDemoTextImpl::setDemoText");
        demoText = formText;
    }

    /** Certainly we need a getter for the String */
    public String getDemoText() {
        System.out.println("ContextDemoTextImpl::getDemoText");
        return demoText;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.workflow.ContextResource#init(de.schlund.pfixcore.workflow.Context)
     */
    public void init(Context context) throws Exception {

    }

    /**
     * Inserting the String into the Domtree given back to pustefix
     * 
     * @see de.schlund.pfixcore.workflow.ContextResource#insertStatus(
     *      de.schlund.pfixxml.ResultDocument, org.w3c.dom.Element)
     */
    public void insertStatus(ResultDocument resdoc, final Element root)
            throws Exception {
        if (demoText != null) {
            resdoc.createSubNode(root, "demotext").setAttribute("value",
                    demoText);
        }
    }

}
