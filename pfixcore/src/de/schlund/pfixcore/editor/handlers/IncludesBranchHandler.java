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

package de.schlund.pfixcore.editor.handlers;
import de.schlund.pfixcore.editor.*;
import de.schlund.pfixcore.editor.interfaces.*;
import de.schlund.pfixcore.editor.resources.*;
import de.schlund.pfixcore.generator.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import de.schlund.util.*;
import de.schlund.util.statuscodes.*;
import de.schlund.util.statuscodes.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.log4j.*;
import org.apache.xml.serialize.*;
import org.apache.xpath.*;
import java.io.*;

/**
 * IncludesBranchHandler.java
 *
 *
 * Created: Wed Dec 13 12:34:24 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class IncludesBranchHandler extends EditorStdHandler {
    private        Category               CAT   = Category.getInstance(this.getClass().getName());
    private static DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    private static final String EMPTY  = "empty";
    private static final String COPY   = "copy";
    private static final String DELETE = "delete";
    
    public AuxDependency getCurrentInclude(EditorSessionStatus esess) {
        return esess.getCurrentInclude();
    }

    public void setCurrentInclude(EditorSessionStatus esess, AuxDependency newinc) {
        esess.setCurrentInclude(newinc);
    }

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm      = context.getContextResourceManager();
        EditorSessionStatus    esess    = EditorRes.getEditorSessionStatus(crm);
        IncludesBranch         branch   = (IncludesBranch) wrapper;
        String                 type     = branch.getType();
        boolean                dobranch = branch.getDoBranch().booleanValue();
        EditorProduct          prod     = esess.getProduct();
        String                 prodname = prod.getName();
        TargetGenerator        tgen     = prod.getTargetGenerator();
        AuxDependency          currinc  = getCurrentInclude(esess);
        String                 path     = currinc.getPath();
        String                 part     = currinc.getPart();

        if (currinc != null && dobranch) {
            if (type.equals(EMPTY) || type.equals(COPY)) {
                if (currinc.getProduct().equals("default")) {
                    Object LOCK = FileLockFactory.getInstance().getLockObj(path);
                    synchronized (LOCK) {
                        EditorHelper.checkForFile(path, prod.getPfixcoreNamespace());

                        Document incdoc   = EditorHelper.getIncludeDocument(tgen, currinc, true);
                        Node     partnode = EditorHelper.getIncludePart(incdoc, currinc);
                        if (partnode == null) {
                            partnode = EditorHelper.createEmptyPart(incdoc, currinc);
                        }

                        NodeList nl = XPathAPI.selectNodeList(partnode, "./product[@name = '" + prodname + "']"); 
                        
                        if (nl.getLength() > 1) {
                            throw new XMLException("FATAL ERROR: Product branch " + prodname + " is multiple times defined!");
                        } else if (nl.getLength() == 0) {
                            Element newbranch = incdoc.createElement("product");
                            newbranch.setAttribute("name", prodname);
                            partnode.appendChild(incdoc.createTextNode("  "));
                            partnode.appendChild(newbranch);
                            partnode.appendChild(incdoc.createTextNode("\n  "));
                            if (type.equals(COPY)) {
                                NodeList defcontent = XPathAPI.selectNodeList(partnode, "./product[@name = 'default']/node()");
                                for (int i = 0; i < defcontent.getLength(); i++) {
                                    newbranch.appendChild(defcontent.item(i).cloneNode(true));
                                }
                            }
                            doSerialize(incdoc, path);
                        }
                        EditorHelper.doUpdateForAuxDependency(currinc, tgen);
                        AuxDependency newinc =
                            AuxDependencyFactory.getInstance().getAuxDependency(DependencyType.TEXT, path, part, prodname);
                        setCurrentInclude(esess, newinc);
                        esess.getLock(newinc);
                    }
                }
            } else if (type.equals(DELETE)) {
                if (currinc.getProduct().equals(prodname)) {
                    Object LOCK = FileLockFactory.getInstance().getLockObj(path);
                    synchronized (LOCK) {
                        Document incdoc   = EditorHelper.getIncludeDocument(tgen, currinc, true);
                        if (incdoc != null) {
                            Node partnode = EditorHelper.getIncludePart(incdoc, currinc);
                            if (partnode != null) {
                                NodeList nl = XPathAPI.selectNodeList(partnode, "./product[@name = '" + prodname + "']");
                                if (nl.getLength() == 1) {
                                    EditorHelper.createBackup(esess, getCurrentInclude(esess), nl.item(0));
                                    partnode.removeChild(nl.item(0));
                                    doSerialize(incdoc, path);
                                }
                                EditorHelper.doUpdateForAuxDependency(currinc, tgen);
                                AuxDependency newinc =
                                    AuxDependencyFactory.getInstance().getAuxDependency(DependencyType.TEXT, path, part, "default");
                                setCurrentInclude(esess, newinc);
                                esess.getLock(newinc);
                            }
                        }
                    }
                }
            }
        }
    }

    private void doSerialize(Document incdoc, String path) throws Exception {
        FileOutputStream output = new FileOutputStream(path);
        OutputFormat     outfor = new OutputFormat("xml","ISO-8859-1",true);
        XMLSerializer    ser    = new XMLSerializer(output, outfor);
        outfor.setIndent(0);
        outfor.setPreserveSpace(true);
        ser.serialize(incdoc);
    }
    
}// IncludesBranchHandler
