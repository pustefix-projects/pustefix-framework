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
import java.io.*;
import java.text.*;
import java.util.*;
import java.net.URLEncoder;
import java.net.URLDecoder;
import javax.xml.parsers.*;
import org.apache.log4j.*;
import org.apache.oro.text.regex.*;
import org.apache.xml.serialize.*;
import org.apache.xpath.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * IncludesUploadHandler.java
 *
 *
 * Created: Wed Dec 19 12:34:24 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class IncludesUploadHandler extends EditorStdHandler {
    private static DocumentBuilderFactory dbfac     = DocumentBuilderFactory.newInstance();
    private static Category               EDITOR    = Category.getInstance("LOGGER_EDITOR");
    private static Category               SERIALIZER = Category.getInstance("LOGGER_SERIALIZER");
    private static Category               CAT       = Category.getInstance(IncludesUploadHandler.class.getName());
    private static PatternCompiler        pc        = new Perl5Compiler();
    private static Substitution           nbspsubst = new StringSubstitution("&#160;");
    private static Pattern                nbspsign;
    private        PatternMatcher         pm;
    private static final String           DEF_TEXT  =       "<lang name=\"default\">\n      </lang>";
    private static final String           DEF_TEXT_APPLET = "<lang name=|default|>\n      </lang>";    
    
    public IncludesUploadHandler() throws MalformedPatternException {
        char[] nbsp = {'\u00a0'};
        nbspsign = pc.compile(new String(nbsp));
        dbfac.setNamespaceAware(true);
    }

    public AuxDependency getCurrentInclude(EditorSessionStatus esess) {
        return esess.getCurrentInclude();
    }
    
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm     = context.getContextResourceManager();
        EditorSessionStatus    esess   = EditorRes.getEditorSessionStatus(crm);
        IncludesUpload         upl     = (IncludesUpload) wrapper;
        AuxDependency          currinc = getCurrentInclude(esess);
        TargetGenerator        tgen    = esess.getProduct().getTargetGenerator();
        EditorUser             euser   = esess.getUser();
        
        if (currinc != null) {
            String  product = currinc.getProduct();
            Element thepart = EditorHelper.getIncludePart(tgen, currinc);
            if (thepart != null) {
                pm = new Perl5Matcher();
                StringWriter  output = new StringWriter();
                OutputFormat  outfor = new OutputFormat("xml","ISO-8859-1",true);
                XMLSerializer ser    = new XMLSerializer(output, outfor);
                NodeList      nl     = XPathAPI.selectNodeList(thepart, "./product[@name = '" + product + "']/node()");
                outfor.setPreserveSpace(true); 
                outfor.setIndent(0);
                outfor.setOmitXMLDeclaration(true);
                if (nl.getLength() > 0) {
                    DocumentFragment frag = thepart.getOwnerDocument().createDocumentFragment();
                    for (int i = 0; i < nl.getLength(); i++) {
                        frag.appendChild(nl.item(i));
                    }
                    ser.serialize(frag);
                    String text = output.toString();
                    if (text != null) {
                        text = Util.substitute(pm, nbspsign, nbspsubst, text, Util.SUBSTITUTE_ALL);
                        text = text.trim();
                        
                    } else {
                        text=DEF_TEXT_APPLET;
                    }
                    upl.setStringValContent(text);
                    if (SERIALIZER.isDebugEnabled())
                        SERIALIZER.debug("\n==========================================================\n"
                                         + "| User      >>> " + euser.getName() + "\n" 
                                         + "| Path@Part >>> " + currinc.getPath() + "@" + currinc.getPart() + "\n"
                                         + text + "\n");
                } else {
                    upl.setStringValContent(DEF_TEXT);
                }
            } else {
                upl.setStringValContent(DEF_TEXT);
            }
        }
    }

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm      = context.getContextResourceManager();
        EditorSessionStatus    esess    = EditorRes.getEditorSessionStatus(crm);
        TargetGenerator        tgen     = esess.getProduct().getTargetGenerator();
        IncludesUpload         upl      = (IncludesUpload) wrapper;
        AuxDependency          currinc  = getCurrentInclude(esess);
        String                 currpart = currinc.getPart();
        String                 currprod = currinc.getProduct();
        String                 currpath = currinc.getPath();
        Boolean                doupl    = upl.getHaveUpload();
        Boolean                backup   = upl.getHaveBackup();
        String                 backfile = upl.getBackup();
        
        if (doupl != null && doupl.booleanValue() == true) {
            StatusCodeFactory sfac     = new StatusCodeFactory("pfixcore.editor.includesupload");
            Object            LOCK     = FileLockFactory.getInstance().getLockObj(currpath);
            String            content  = null;
            Node              impnode  = null;
            
            if (backup != null && backup.booleanValue() == true && backfile != null) {
                impnode = EditorHelper.getBackupContent(esess, currinc, backfile, true);
            } else {
                PfixcoreNamespace[] nspaces = esess.getProduct().getPfixcoreNamespace();
                String ns = "";
                for (int i = 0; i < nspaces.length; i++) {
                    PfixcoreNamespace nsp = nspaces[i];
                    ns = ns + "xmlns:" + nsp.getPrefix() + "=\"" + nsp.getUri() + "\" ";
                }
                
                content = upl.getContent();
                
                content = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                    "<product " + ns + " name=\"" + currprod + "\">\n      " + content + "\n    </product>";
                DocumentBuilder domp = dbfac.newDocumentBuilder();
                try {
                    impnode = (Node) domp.parse(new InputSource(new StringReader(content))).getDocumentElement();
                } catch (SAXParseException e) {
                    int line = (e.getLineNumber() - 1);
                    int col  = (e.getColumnNumber() - 1);
                    if (line == 1) { // Fixup hack. :-)
                        col = col - 6;
                    }
                    Date       now    = new Date();
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS", Locale.GERMAN);
                    String     msg    = e.getMessage() + " (Line " + line + " / Col " + col + ")";
                    msg += " [" + format.format(now) + "]";
                    CAT.warn("PARSE ERROR: " + e.toString());
                    upl.setStringValExceptionMsg(msg);
                    StatusCode scode = sfac.getStatusCode("PARSE_ERR");
                    upl.addSCodeContent(scode);
                  
                    return;
                }
            }
            

            if (impnode != null) {
                synchronized(LOCK) {
                    PfixcoreNamespace[] nspaces = esess.getProduct().getPfixcoreNamespace();
                    EditorHelper.checkForFile(currpath, nspaces);
                    Document incdoc  = EditorHelper.getIncludeDocument(tgen, currinc, true);
                    Node     newnode;
                    try {
                        newnode = incdoc.importNode(impnode, true);
                        Element rootnode = incdoc.getDocumentElement();
                        if (nspaces != null) {
                            for (int i = 0; i < nspaces.length; i++) {
                                ((Element) newnode).removeAttribute("xmlns:" + nspaces[i].getPrefix());
                                rootnode.setAttribute("xmlns:" + nspaces[i].getPrefix(), nspaces[i].getUri());
                            }
                        }
                    } catch (DOMException e) {
                        CAT.warn("IMPORT ERROR: " + e.toString());
                        upl.setStringValExceptionMsg(e.getMessage());
                        StatusCode scode = sfac.getStatusCode("PARSE_ERR");
                        upl.addSCodeContent(scode);
                        return;
                    }
                    
                    Node partnode = EditorHelper.getIncludePart(incdoc, currinc);
                    if (partnode == null) {
                        partnode = EditorHelper.createEmptyPart(incdoc, currinc);
                    }
                    
                    NodeList nl = XPathAPI.selectNodeList(partnode, "./product[@name ='" + currprod + "']");
                    if (nl.getLength() == 1) {
                        EditorHelper.createBackup(esess, currinc, nl.item(0));
                        partnode.replaceChild(newnode, nl.item(0));
                    } else if (nl.getLength() > 1) {
                        throw new XMLException("FATAL ERROR: Product " + currprod + " of part " +
                                               currpart + " is multiple times defined!");
                    } else { // it's new
                        partnode.appendChild(incdoc.createTextNode("\n    "));
                        partnode.appendChild(newnode);
                        partnode.appendChild(incdoc.createTextNode("\n  "));
                    }

                    FileOutputStream output = new FileOutputStream(currpath);
                    OutputFormat     outfor = new OutputFormat("xml","ISO-8859-1",true);
                    XMLSerializer    ser    = new XMLSerializer(output, outfor);
                    outfor.setIndent(0);
                    outfor.setPreserveSpace(true);
                    ser.serialize(incdoc);                    
                    EDITOR.warn("TXT: " + esess.getUser().getId() + ": " +
                                currpart + "@" + currpath + " [" + currprod + "]");
                    // We need to make sure that the modtime will be different
                    // FIXME !! FIXME !! We need this! But why:-)
                    EditorHelper.resetIncludeDocumentTarget(tgen, currinc);
                }
                // make sure that the new include is used at least once before we leave here.
                EditorHelper.doUpdateForAuxDependency(currinc, tgen);
            }
         
        }
    }

    public boolean isActive(Context context) {
        ContextResourceManager crm      = context.getContextResourceManager();
        EditorSessionStatus    esess    = EditorRes.getEditorSessionStatus(crm);
        AuxDependency          currinc  = getCurrentInclude(esess);
        if (currinc == null) {
            return false;
        } else {
            return esess.isOwnLock(currinc);
        }
    }

}// IncludesUploadHandler
