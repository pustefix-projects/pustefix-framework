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



import de.schlund.lucefix.core.PfixQueueManager;
import de.schlund.lucefix.core.TripelImpl;
import de.schlund.pfixcore.editor.EditorHelper;
import de.schlund.pfixcore.editor.EditorPageUpdater;
import de.schlund.pfixcore.editor.EditorUser;
import de.schlund.pfixcore.editor.FileLockFactory;
import de.schlund.pfixcore.editor.PfixcoreNamespace;
import de.schlund.pfixcore.editor.interfaces.IncludesUpload;
import de.schlund.pfixcore.editor.resources.EditorRes;
import de.schlund.pfixcore.editor.resources.EditorSessionStatus;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.util.Path;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;
import de.schlund.util.statuscodes.StatusCode;
import de.schlund.util.statuscodes.StatusCodeFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.apache.log4j.Category;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.StringSubstitution;
import org.apache.oro.text.regex.Substitution;
import org.apache.oro.text.regex.Util;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

/**
 * @author jh
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class XMLUploadHandler extends EditorStdHandler {
    private static Category               EDITOR          = Category.getInstance("LOGGER_EDITOR");
    private static Category               SERIALIZER      = Category.getInstance("LOGGER_SERIALIZER");
    private static Category               CAT             = Category.getInstance(XMLUploadHandler.class.getName());
    private static PatternCompiler        pc              = new Perl5Compiler();
    private static Substitution           nbspsubst       = new StringSubstitution("&#160;");
    private static Pattern                nbspsign;
    private static final String           DEF_TEXT        = "<lang name=\"default\">\n      </lang>";
    private static final String           DEF_TEXT_APPLET = "<lang name=|default|>\n      </lang>";

    public XMLUploadHandler() throws MalformedPatternException {
        char[] nbsp = { '\u00a0' };
        nbspsign = pc.compile(new String(nbsp));
    }
    /**
     * @see de.schlund.pfixcore.generator.IHandler#handleSubmittedData(de.schlund.pfixcore.workflow.Context, de.schlund.pfixcore.generator.IWrapper)
     */
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm = context.getContextResourceManager();
        EditorSessionStatus esess = EditorRes.getEditorSessionStatus(crm);
        TargetGenerator tgen = esess.getProduct().getTargetGenerator();
        IncludesUpload upl = (IncludesUpload) wrapper;
        AuxDependency currinc = getCurrentInclude(esess);
        String currpart = currinc.getPart();
        String currprod = currinc.getProduct();
        Path currpath = currinc.getPath();
        Boolean doupl = upl.getHaveUpload();
        Boolean backup = upl.getHaveBackup();
        String backfile = upl.getBackup();

        if (doupl != null && doupl.booleanValue() == true) {
            StatusCodeFactory sfac = new StatusCodeFactory("pfixcore.editor.includesupload");
            Object LOCK = FileLockFactory.getInstance().getLockObj(currpath);
            String content = null;
            Node impnode = null;

            checkAccess(esess);

            if (backup != null && backup.booleanValue() == true && backfile != null) {
                impnode = EditorHelper.getBackupContent(esess, currinc, backfile, true);
            } else {
                PfixcoreNamespace[] nspaces = esess.getProduct().getPfixcoreNamespace();
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < nspaces.length; i++) {
                    PfixcoreNamespace nsp = nspaces[i];
                    sb.append("xmlns:" + nsp.getPrefix() + "=\"" + nsp.getUri() + "\" ");
                }
                String ns = sb.toString();
                content = upl.getContent();

                content =
                    "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
                        + "<product "
                        + ns
                        + " name=\""
                        + currprod
                        + "\">\n      "
                        + content
                        + "\n    </product>";
                try {
                    impnode = Xml.parseStringMutable(content).getDocumentElement();
                } catch (SAXParseException e) {
                    int line = (e.getLineNumber() - 1);
                    int col = (e.getColumnNumber() - 1);
                    if (line == 1) { // Fixup hack. :-)
                        col = col - 6;
                    }
                    Date now = new Date();
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS", Locale.GERMAN);
                    String msg = e.getMessage() + " (Line " + line + " / Col " + col + ")";
                    msg += " [" + format.format(now) + "]";
                    CAT.warn("PARSE ERROR: " + e.toString());
                    upl.setStringValExceptionMsg(msg);
                    StatusCode scode = sfac.getStatusCode("PARSE_ERR");
                    upl.addSCodeContent(scode);

                    return;
                }
            }

            if (impnode != null) {
                synchronized (LOCK) {
                    PfixcoreNamespace[] nspaces = esess.getProduct().getPfixcoreNamespace();
                    EditorHelper.checkForFile(currpath, nspaces);
                    Document incdoc = EditorHelper.getIncludeDocument(tgen, currinc);
                    Node newnode;
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

                    List nl = XPath.select(partnode, "./product[@name ='" + currprod + "']");
                    if (nl.size() == 1) {
                        EditorHelper.createBackup(esess, currinc, (Node) nl.get(0));
                        partnode.replaceChild(newnode, (Node) nl.get(0));
                    } else if (nl.size() > 1) {
                        throw new XMLException("FATAL ERROR: Product " + currprod + " of part " + currpart + " is multiple times defined!");
                    } else { // it's new
                        partnode.appendChild(incdoc.createTextNode("\n    "));
                        partnode.appendChild(newnode);
                        partnode.appendChild(incdoc.createTextNode("\n  "));
                    }
                    EDITOR.debug("lucefix: editor says we shoud refresh this: " + currpath.getRelative() + "|" + currpart + "|" + currprod);
                    PfixQueueManager.getInstance(null).queue(new TripelImpl(currprod,currpart,currpath.getRelative()));

                    Xml.serialize(incdoc, currpath.resolve(), false, true);
                    EDITOR.warn("TXT: " + esess.getUser().getId() + ": " + currpart + "@" + currpath.getRelative() + " [" + currprod + "]");
                    // We need to make sure that the modtime will be different
                    // FIXME !! FIXME !! We need this! But why:-)
                    EditorHelper.resetIncludeDocumentTarget(tgen, currinc);
                }
                // make sure that the new include is used at least once before we leave here.
                HashSet uptarg = esess.getTargetsForDelayedUpdate();
                if (uptarg != null) {
                    Iterator iter = uptarg.iterator();
                    if (iter.hasNext()) {
                        Target tmp = (Target) iter.next();
                        tmp.getValue();
                    }
                    while (iter.hasNext()) {
                        Target tmp = (Target) iter.next();
                        EditorPageUpdater.getInstance().addTarget(tmp);
                    }
                    esess.setTargetsForDelayedUpdate(null);
                }
                EditorHelper.doUpdateForAuxDependency(currinc, tgen);
            }

        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm = context.getContextResourceManager();
        EditorSessionStatus esess = EditorRes.getEditorSessionStatus(crm);
        IncludesUpload upl = (IncludesUpload) wrapper;
        AuxDependency currinc = getCurrentInclude(esess);
        TargetGenerator tgen = esess.getProduct().getTargetGenerator();
        EditorUser euser = esess.getUser();

        if (currinc != null) {
            String product = currinc.getProduct();
            Element thepart = EditorHelper.getIncludePart(tgen, currinc);
            if (thepart != null) {
                Element ele = (Element) XPath.selectNode(thepart, "./product[@name = '" + product + "']");
                if (ele != null) {
                    PfixcoreNamespace[] nspaces = esess.getProduct().getPfixcoreNamespace();
                    if (nspaces != null) {
                        // make sure we have the ns decl in the root element and thus trip it before the 
                        // text field is filled.
                        for (int i = 0; i < nspaces.length; i++) {
                            ele.setAttribute("xmlns:" + nspaces[i].getPrefix(), nspaces[i].getUri());
                        }
                    }
                    
                    Perl5Matcher pm = new Perl5Matcher();
                    String text = Xml.serialize(ele, false, false);
                    text = Xml.stripElement(text);
                    text = Util.substitute(pm, nbspsign, nbspsubst, text, Util.SUBSTITUTE_ALL);
                    text = text.trim();
                    upl.setStringValContent(text);
                    if (SERIALIZER.isDebugEnabled())
                        SERIALIZER.debug("\n==========================================================\n"
                                         + "| User      >>> "
                                         + euser.getUserInfo().getName()
                                         + "\n"
                                         + "| Path@Part >>> "
                                         + currinc.getPath().getRelative()
                                         + "@"
                                         + currinc.getPart()
                                         + "\n"
                                         + text
                                         + "\n");
                } else {
                    upl.setStringValContent(DEF_TEXT);
                }
            } else {
                upl.setStringValContent(DEF_TEXT);
            }
        }
    }

    public boolean isActive(Context context) {
        ContextResourceManager crm = context.getContextResourceManager();
        EditorSessionStatus esess = EditorRes.getEditorSessionStatus(crm);
        AuxDependency currinc = getCurrentInclude(esess);
        if (currinc == null) {
            return false;
        } else {
            return esess.isOwnLock(currinc);
        }
    }

    public abstract void checkAccess(EditorSessionStatus esess) throws Exception;
    public abstract AuxDependency getCurrentInclude(EditorSessionStatus esess);

}
