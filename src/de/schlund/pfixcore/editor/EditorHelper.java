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

package de.schlund.pfixcore.editor;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;

import de.schlund.pfixcore.editor.interfaces.*;
import de.schlund.pfixcore.editor.resources.*;
import de.schlund.pfixcore.generator.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixcore.workflow.app.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import de.schlund.util.*;
import de.schlund.util.statuscodes.*;
import org.apache.log4j.*;
import org.apache.xml.serialize.*;
import org.apache.xpath.*;
import org.w3c.dom.*;

/**
 * EditorHelper.java
 *
 *
 * Created: Fri Nov 30 16:40:07 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version
 *
 *
 */

public class EditorHelper {
    private static Category               CAT   = Category.getInstance(EditorHelper.class.getName());
    private static DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();

    public static void doUpdateForAuxDependency(AuxDependency currinc, TargetGenerator tgen) throws Exception {
        String  path    = currinc.getPath();
        TreeSet targets = currinc.getAffectedTargets();
        HashSet pages   = new HashSet();
        for (Iterator i = targets.iterator(); i.hasNext(); ) {
            Target target = (Target) i.next();
            pages.addAll(target.getPageInfos());
        }
        
        Iterator j = pages.iterator();
        Target   toplevel;
        if (j.hasNext()) {
            PageInfo pinfo = (PageInfo) j.next();
            if (pinfo.getTargetGenerator() == tgen) {
                toplevel = tgen.getPageTargetTree().getTargetForPageInfo(pinfo);
                if (toplevel == null) {
                    CAT.error("\n **************** Got 'null' target for PageInfo " + pinfo.getName() + "!! *****************");
                } else {
                    toplevel.getValue();
                }
            }
            for (; j.hasNext(); ) {
                pinfo = (PageInfo) j.next();
                if (pinfo.getTargetGenerator() != tgen) continue;
                toplevel = tgen.getPageTargetTree().getTargetForPageInfo(pinfo);
                if (toplevel == null) {
                    CAT.error("\n **************** Got 'null' target for PageInfo " + pinfo.getName() + "!! *****************");
                } else {
                    EditorPageUpdater.getInstance().addTarget(toplevel);
                }
            }
        }
    }

    public static Node createEmptyPart(Document doc, AuxDependency include) {
        String  name = include.getPart();
        Element root = doc.getDocumentElement();
        Element part = doc.createElement("part");
        part.setAttribute("name", name);
        root.appendChild(doc.createTextNode("\n  "));
        root.appendChild(part);
        root.appendChild(doc.createTextNode("\n"));
        return part;
    }
    
    public static void checkForFile(String path, PfixcoreNamespace[] nspaces) throws Exception {
        File incfile = new File(path);
        if (incfile.exists() && (!incfile.canRead() || !incfile.canWrite() || !incfile.isFile())) {
            throw new XMLException("File " + path + " is not accessible!");
        }
        if (!incfile.exists()) {
            CAT.debug("===> Going to create " + path);
            if (incfile.createNewFile()) {
                Document skel = dbfac.newDocumentBuilder().newDocument();
                Element  root = skel.createElement("include_parts");
                if (nspaces != null) {
                    for (int i = 0; i < nspaces.length; i++) {
                        root.setAttribute("xmlns:" + nspaces[i].getPrefix(), nspaces[i].getUri());
                    }
                }
                skel.appendChild(root);
                root.appendChild(skel.createComment("Append include parts here..."));
                FileWriter    output = new FileWriter(incfile);
                OutputFormat  outfor = new OutputFormat("xml","ISO-8859-1",true);
                XMLSerializer ser    = new XMLSerializer(output, outfor);
                outfor.setPreserveSpace(true); 
                outfor.setIndent(0);
                ser.serialize(skel);
                // CAT.debug("==========> Modtime for file is: " + incfile.lastModified());
            } else {
                throw new XMLException("Couldn't generate new file " + path);
            }
        }
    }
    
    private static String constructBackupDir(EditorSessionStatus ess, AuxDependency inc) {
            String path = inc.getPath();
            if (inc.getType().equals(DependencyType.TEXT)) {
                String part = inc.getPart();
                String prod = inc.getProduct();
                return ess.getBackupDir() + "/" + path + "/" + part + "/" + prod;
            } else {
                return ess.getBackupDir() + "/" + path;
            }
    }

    private static File constructBackupFile(EditorSessionStatus ess, AuxDependency inc) {
        String user = ess.getUser().getId();
        String time = new Date().toString();
        String dir  = constructBackupDir(ess, inc);
        String name = time + " [" + user + "]";
        File   tmp  = new File(dir);
        if (tmp.exists() || tmp.mkdirs()) {
            return new File(dir + "/" + name);
        } else {
            CAT.warn("Couldn't create backup dir " + dir); 
            return null;
        }
    }
    
    public static void createBackupImage(EditorSessionStatus ess, File from) {
        AuxDependency inc  = ess.getCurrentImage();
        File          file = constructBackupFile(ess, inc);
        if (file != null) {
            try {
                FileInputStream  fin  = new FileInputStream(from);
                FileOutputStream fout = new FileOutputStream(file);
                byte[] b   = new byte[4096];
                int    num = 0;
                while ((num = fin.read(b)) != -1) {
                    fout.write(b, 0, num);
                }
            } catch (Exception e) {
                CAT.warn("Couldn't serialize into backup file " + file.getAbsolutePath()); 
            }
        }
    }
    
    public static void createBackup(EditorSessionStatus ess, AuxDependency inc, Node tosave) {
        // CAUTION: Don't do it like below, the inc has to be supplied by the caller!!!
        // AuxDependency inc  = ess.getCurrentInclude();
        File file = constructBackupFile(ess, inc);
        if (file != null) {
            try {
                FileWriter    output = new FileWriter(file);
                OutputFormat  outfor = new OutputFormat("xml","ISO-8859-1",true);
                XMLSerializer ser    = new XMLSerializer(output, outfor);
                outfor.setPreserveSpace(true); 
                outfor.setIndent(0);
                ser.serialize((Element) tosave);
            } catch (Exception e) {
                CAT.warn("Couldn't serialize into backup file " + file.getAbsolutePath()); 
            }
        }
    }

    
    
    public static String[] getBackupNames(EditorSessionStatus ess, AuxDependency inc) {
        String   dir     = constructBackupDir(ess, inc);
        File     file    = new File(dir);
        File[]   entries = file.listFiles();
        if (entries != null && entries.length > 0) {
            Comparator comp = new ReverseCreationComp();
            Arrays.sort(entries, comp);
            String[] names = new String[entries.length];
            for (int i = 0; i < entries.length; i++) {
                names[i] = entries[i].getName();
            }
            return names;
        } else {
            return null;
        }
    }

    public static File getBackupImageFile(EditorSessionStatus ess, AuxDependency aux,
                                          String filename) throws Exception {
        String name = constructBackupDir(ess, aux) +  "/" + filename;
        File   file = new File(name);
        if (file.exists() && file.canRead() && file.isFile()) {
            return file;
        } else {
            return null;
        }
    }

    public static Node getBackupContent(EditorSessionStatus ess, AuxDependency inc,
                                        String filename, boolean kill) throws Exception {
        String name = constructBackupDir(ess, inc) +  "/" + filename;
        File   file = new File(name);
        if (file.exists() && file.canRead() && file.isFile()) {
            DocumentBuilder domp   = dbfac.newDocumentBuilder();
            Document        doc    = domp.parse(file);
            if (kill) {
                file.delete();
            }
            return doc.getDocumentElement();
        } else {
            return null;
        }
    }

    public static void renderBackupOptions(EditorSessionStatus ess, AuxDependency inc,
                                           ResultDocument resdoc, Element root) {
        String[] names = getBackupNames(ess, inc);
        if (names != null && names.length > 0) {
            for (int i = 0; i < names.length; i++) {
                resdoc.addTextChild(root, "option", names[i]);
            } 
        }
    }
    
    public static void renderSingleTarget(Target target, ResultDocument resdoc, Element root) {
        Element elem = resdoc.createSubNode(root, "target");
        elem.setAttribute("type", target.getType().toString());
        elem.setAttribute("name", target.getTargetKey());

        if (target.getType() == TargetType.XML_VIRTUAL || target.getType() == TargetType.XSL_VIRTUAL) {
            renderSingleTarget(target.getXMLSource(), resdoc, elem);
            renderSingleTarget(target.getXSLSource(), resdoc, elem);
        }
    }

    public static void renderAffectedPages(EditorSessionStatus ess, AuxDependency aux,
                                           ResultDocument resdoc, Element root) throws Exception {
        TreeSet targets = aux.getAffectedTargets();        
        HashSet prods   = new HashSet();
        TreeSet pages   = new TreeSet();

        prods.addAll(Arrays.asList(EditorProductFactory.getInstance().getAllEditorProducts()));

        for (Iterator i = targets.iterator(); i.hasNext(); ) {
            Target target = (Target) i.next();
            pages.addAll(target.getPageInfos());
        }
        renderAffectedPages(ess, pages, prods, resdoc, root);
    }

    public static void renderAffectedPages(EditorSessionStatus ess, Target target,
                                           ResultDocument resdoc, Element root) throws Exception {
        TargetType type  = target.getType();
        HashSet    prods = new HashSet();
        TreeSet    pages = target.getPageInfos();
        
        if (type.equals(TargetType.XML_LEAF) || type.equals(TargetType.XSL_LEAF)) {
            prods.addAll(Arrays.asList(EditorProductFactory.getInstance().getAllEditorProducts()));
        } else {
            prods.add(ess.getProduct());
        }
        renderAffectedPages(ess, pages, prods, resdoc, root);
    }

    
    private static void renderAffectedPages(EditorSessionStatus ess, TreeSet pages, HashSet prods,
                                                ResultDocument resdoc, Element root) {
        
        for (Iterator i = prods.iterator(); i.hasNext(); ) {
            EditorProduct   prod = (EditorProduct) i.next();
            TargetGenerator gen  = prod.getTargetGenerator();
            
            Element elem    = resdoc.createSubNode(root, "product");
            elem.setAttribute("name", prod.getName());
            elem.setAttribute("comment", prod.getComment());

            for (Iterator j = pages.iterator(); j.hasNext(); ) {
                PageInfo        pinfo = (PageInfo) j.next();
                TargetGenerator pgen  = pinfo.getTargetGenerator();
                if (pgen == gen) {
                    Element newnode  = resdoc.createSubNode(elem, "page");
                    newnode.setAttribute("name", pinfo.getName());
                    Target  toplevel = pgen.getPageTargetTree().getTargetForPageInfo(pinfo);
                    try {
                        if (toplevel.needsUpdate()) {
                            EditorPageUpdater.getInstance().addTarget(toplevel);
                            newnode.setAttribute("uptodate", "false");
                        } else {
                            newnode.setAttribute("uptodate", "true");
                        }
                    } catch (Exception e) {
                        newnode.setAttribute("uptodate", "???");
                    }
                }
            }
        }
    }

    public static void resetIncludeDocumentTarget(TargetGenerator tgen, AuxDependency include) throws Exception {
        if (include.getType() != DependencyType.TEXT) {
            throw new XMLException("Dependency is not of Type TEXT");
        }
        String docroot = tgen.getDocroot();
        String path    = include.getPath();
        if (!path.startsWith(docroot)) {
            throw new XMLException("Dependency path " + path + " is not in a subdir of the docroot " + docroot);
        }
        String name    = path.substring(docroot.length());
       /* Target target  = tgen.createXMLLeafTarget(name);
        target.resetModTime();*/
        IncludeDocument includeDocument = IncludeDocumentFactory.getInstance().getIncludeDocument(path, true);
        includeDocument.resetModTime();
        
        CAT.debug("==========> After reset: Modtime for IncludeDocument: " + includeDocument.getModTime());
    }
    
    public static Document getIncludeDocument(TargetGenerator tgen, AuxDependency include, boolean mutable) throws Exception {
        if (include.getType() != DependencyType.TEXT) {
            throw new XMLException("Dependency is not of Type TEXT");
        }
        String docroot = tgen.getDocroot();
        String path    = include.getPath();
        if (!path.startsWith(docroot)) {
            throw new XMLException("Dependency path " + path + " is not in a subdir of the docroot " + docroot);
        }
        File   file    = new File(path);
        if (!file.exists()) {
            return null;
        }
        String name    = path.substring(docroot.length());
        CAT.debug("**************** getIncludeDocument: " + name);
        Object LOCK    = FileLockFactory.getInstance().getLockObj(path);
        synchronized (LOCK) {
           /* Target target = tgen.createXMLLeafTarget(name);
            if (target != null) {
                CAT.debug("==========> Modtime for Target: " + target.getModTime());
                CAT.debug("==========> Modtime for file:   " + file.lastModified());
                Document incdoc = (Document) ((Document) target.getValue()).cloneNode(true);*/
                
           // run xerces here only !!!     
            
                
            /*IncludeDocument includeDocument = IncludeDocumentFactory.getInstance().getIncludeDocument(path);
            if(includeDocument != null) {
                CAT.debug("==========> Modtime for IncludeDocument: " + includeDocument.getModTime());
                CAT.debug("==========> Modtime for file:   " + file.lastModified());
                Document doc = (Document)includeDocument.getDocument();
                Document iDocument = (Document)doc.cloneNode(true);
                Element  root   = iDocument.getDocumentElement();
                root.removeAttribute("incpath");
                return iDocument;
            } else {
                return null;
            }*/
            Document doc = null;
            if(!mutable) {
                IncludeDocument iDoc = IncludeDocumentFactory.getInstance().getIncludeDocument(path, false);
                doc = iDoc.getDocument();
            } else {
               /* DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                if(!docBuilderFactory.isNamespaceAware())
                    docBuilderFactory.setNamespaceAware(true);
                if(docBuilderFactory.isValidating())
                    docBuilderFactory.setValidating(false);
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                doc = docBuilder.parse(path);*/
                IncludeDocument iDoc = IncludeDocumentFactory.getInstance().getIncludeDocument(path, true);
                doc = (Document)iDoc.getDocument().cloneNode(true);
                Element root = doc.getDocumentElement();
                root.removeAttribute("incpath");
            }
            return doc;
        }
    }

    public static Element getIncludePart(Document doc, AuxDependency include) throws Exception {
        String   part = include.getPart();
        String   path = include.getPath();
        NodeList nl   = XPathAPI.selectNodeList(doc, "/include_parts/part[@name = '" + part + "']");
        if (nl.getLength() == 0) {
            return null;
        } else if (nl.getLength() == 1) {
            return (Element) nl.item(0);
        } else {
            throw new XMLException("FATAL: Part " + part + " in include file " + path + " is multiple times defined!");
        }
    }
    
    public static Element getIncludePart(TargetGenerator tgen, AuxDependency include) throws Exception {
        Document doc = getIncludeDocument(tgen, include, true);
        if (doc == null) {
            return null;
        } else {
            return getIncludePart(doc, include);
        }
    }


    public static void renderBranchOptions(EditorSessionStatus esess, AuxDependency inc,
                                           ResultDocument resdoc, Element root) throws Exception {

        String          path      = inc.getPath();
        String          part      = inc.getPart();
        TargetGenerator tgen      = esess.getProduct().getTargetGenerator();
        Element         elem      = getIncludePart(tgen, inc);
        if (elem == null) return;
        
        NodeList        nl        = XPathAPI.selectNodeList(elem, "./product");
        HashSet         affedprod = new HashSet(); 
            
        for (int i = 0; i < nl.getLength(); i++) {
            Element tmp  = (Element) nl.item(i);
            String  prod = tmp.getAttribute("name");
            if (prod == null) continue;
            
            AuxDependency test =
                AuxDependencyFactory.getInstance().getAuxDependency(DependencyType.TEXT, path, part, prod);
            
            EditorProduct[] eprods = null;
            if (prod.equals("default")) {
                eprods = EditorProductFactory.getInstance().getAllEditorProducts();
            } else {
                EditorProduct tmpprod  = EditorProductFactory.getInstance().getEditorProduct(prod);
                if (tmpprod != null) {
                    eprods = new EditorProduct[] {tmpprod};
                }
            }
            
            if (eprods != null) {
                for (int j = 0; j < eprods.length; j++) {
                    EditorProduct epr    = eprods[j];
                    TreeSet       allinc =
                        epr.getTargetGenerator().getDependencyRefCounter().getDependenciesOfType(DependencyType.TEXT);
                    if (allinc.contains(test)) {
                        affedprod.add(epr);
                    }
                }
            }
        }
        
        for (Iterator k = affedprod.iterator(); k.hasNext(); ) {
            EditorProduct tmp = (EditorProduct) k.next();
            Element usedby = resdoc.createSubNode(root, "usedbyproduct");
            usedby.setAttribute("name", tmp.getName());
            usedby.setAttribute("comment", tmp.getComment());
        }
    }


    public static void renderIncludeContent(TargetGenerator tgen, AuxDependency include,
                                            ResultDocument resdoc, Element root) throws Exception {
        Element part = getIncludePart(tgen, include); 
        if (part != null) {
            Node cinfo = resdoc.getSPDocument().getDocument().importNode(part, true);
            root.appendChild(cinfo);
        }
    }

    public static void renderTargetContent(Target target, ResultDocument resdoc, Element root) {
        String     key      = target.getTargetKey();
        String     cache    = target.getTargetGenerator().getDisccachedir();
        String     path     = target.getTargetGenerator().getDocroot();
        TargetType type     = target.getType();
        String     filename;

        if (type == TargetType.XML_LEAF || type == TargetType.XSL_LEAF) {
            filename = path + key;
        } else {
            filename = cache + key;
        }
        
        try {
            DocumentBuilder domp  = dbfac.newDocumentBuilder();
            Document        cdoc  = domp.parse(filename);
            Node            cinfo = resdoc.getSPDocument().getDocument().
                importNode((Node) cdoc.getDocumentElement(), true);
            root.appendChild(cinfo);
        } catch (Exception e) {
            // 
        }
    }
    
    public static void renderAuxfiles(Target target, ResultDocument resdoc, Element root) { 
        TreeSet   allaux = new TreeSet();
        int       count  = 0;
        getAuxdepsForTarget(allaux, target, false, DependencyType.FILE);
        for (Iterator i = allaux.iterator(); i.hasNext(); ) {
            AuxDependency aux  = (AuxDependency) i.next();
            Element       elem = resdoc.createSubNode(root, "auxfile");
            elem.setAttribute("dir", aux.getDir());
            elem.setAttribute("path", aux.getPath());
            elem.setAttribute("count", "" + count++);
        }
    }

    public static void renderImages(Target target, ResultDocument resdoc, Element root) { 
        TreeSet allimgs = new TreeSet();
        TreeSet allinc  = new TreeSet();
        getAuxdepsForTarget(allimgs, target, false, DependencyType.IMAGE);
        // get the includes for this target (so recurse has to be "false" here!
        getAuxdepsForTarget(allinc, target, false, DependencyType.TEXT);
        // we want to iterate over these, but still add more to allinc.
        TreeSet tmp = new TreeSet(allinc);
        for (Iterator i = tmp.iterator(); i.hasNext(); ) {
            AuxDependency aux = (AuxDependency) i.next();
            // Now get all includes recursively
            getAuxdepsForInclude(allinc, aux, true, DependencyType.TEXT);
        }
        // now for all includes, get the images they use
        for (Iterator i = allinc.iterator(); i.hasNext(); ) {
            AuxDependency aux = (AuxDependency) i.next();
            // we put what we find into allimgs
            getAuxdepsForInclude(allimgs, aux, false, DependencyType.IMAGE);
        }
        int j = 0;
        for (Iterator i = allimgs.iterator(); i.hasNext(); ) {
            AuxDependency aux  = (AuxDependency) i.next();
            Element       elem = resdoc.createSubNode(root, "image");
            elem.setAttribute("dir", aux.getDir());
            elem.setAttribute("path", aux.getPath());
            elem.setAttribute("modtime", "" + aux.getModTime());
            elem.setAttribute("count", "" + j++);
        }
    }

    public static void renderIncludes(AuxDependency aux, ResultDocument resdoc, Element root) {
        TreeSet   allaux = aux.getChildren();
        ArrayList count  = new ArrayList();
        count.add(new Integer(0));
        if (allaux != null) {
            renderIncludesRec(allaux, resdoc, root, count);
        }
    }
    
    public static void renderIncludes(Target target, ResultDocument resdoc, Element root) { 
        TreeSet   allaux = new TreeSet();
        ArrayList count  = new ArrayList();
        count.add(new Integer(0));
        getAuxdepsForTarget(allaux, target, false, DependencyType.TEXT);
        renderIncludesRec(allaux, resdoc, root, count);
    }

    private static void renderIncludesRec(TreeSet allaux, ResultDocument resdoc, Element root, ArrayList count) {
        for (Iterator i = allaux.iterator(); i.hasNext(); ) {
            AuxDependency aux  = (AuxDependency) i.next();
            if (aux.getType() == DependencyType.TEXT) {
                Integer       cnt  = (Integer) count.get(0);
                TreeSet       sub  = new TreeSet();
                getAuxdepsForInclude(sub,  aux, false, DependencyType.TEXT);
                Element       elem = resdoc.createSubNode(root, "include");
                elem.setAttribute("dir", aux.getDir());
                elem.setAttribute("path", aux.getPath());
                elem.setAttribute("part", aux.getPart());
                elem.setAttribute("count", cnt.toString());
                count.set(0, new Integer(cnt.intValue() + 1));
                if (sub != null && !sub.isEmpty()) {
                    renderIncludesRec(sub, resdoc, elem, count);
                }
            }
        }
    }

    public static void renderIncludesFlatRecursive(Target target, ResultDocument resdoc, Element root) {
        TreeSet allaux = new TreeSet();
        getAuxdepsForTarget(allaux, target, true, DependencyType.TEXT);
        int j = 0;
        for (Iterator i = allaux.iterator(); i.hasNext(); ) {
            AuxDependency aux  = (AuxDependency) i.next();
            Element       elem = resdoc.createSubNode(root, "include");
            elem.setAttribute("dir", aux.getDir());
            elem.setAttribute("path", aux.getPath());
            elem.setAttribute("part", aux.getPart());
            elem.setAttribute("product", aux.getProduct());
            elem.setAttribute("count", "" + j++);
        }
    }

    public static void renderImagesFlatRecursive(Target target, ResultDocument resdoc, Element root) {
        TreeSet allaux = new TreeSet();
        getAuxdepsForTarget(allaux, target, true, DependencyType.IMAGE);
        int j = 0;
        for (Iterator i = allaux.iterator(); i.hasNext(); ) {
            AuxDependency aux  = (AuxDependency) i.next();
            Element       elem = resdoc.createSubNode(root, "image");
            elem.setAttribute("dir", aux.getDir());
            elem.setAttribute("path", aux.getPath());
            elem.setAttribute("modtime", "" + aux.getModTime());
            elem.setAttribute("count", "" + j++);
        }
    }

    public static void renderImagesFlatRecursive(AuxDependency auxin, ResultDocument resdoc, Element root) {
        TreeSet allaux = new TreeSet();
        getAuxdepsForInclude(allaux, auxin, true, DependencyType.IMAGE);
        int j = 0;
        for (Iterator i = allaux.iterator(); i.hasNext(); ) {
            AuxDependency aux  = (AuxDependency) i.next();
            Element       elem = resdoc.createSubNode(root, "image");
            elem.setAttribute("dir", aux.getDir());
            elem.setAttribute("path", aux.getPath());
            elem.setAttribute("modtime", "" + aux.getModTime());
            elem.setAttribute("count", "" + j++);
        }
    }

    private static void getAuxdepsForTarget(TreeSet bucket, Target target, boolean recurse, DependencyType type) {
        AuxDependencyManager manager = target.getAuxDependencyManager();
        if (manager != null) {
            TreeSet topaux  = manager.getChildren();
            for (Iterator i = topaux.iterator(); i.hasNext(); ) {
                AuxDependency aux = (AuxDependency) i.next();
                if (aux.getType() == type) {
                    bucket.add(aux);
                }
                if (recurse) {
                    getAuxdepsForInclude(bucket, aux, recurse, type);
                }
            }
            if (recurse) {
                if (target.getType() == TargetType.XML_VIRTUAL || target.getType() == TargetType.XSL_VIRTUAL) {
                    getAuxdepsForTarget(bucket, target.getXMLSource(), true, type);
                    getAuxdepsForTarget(bucket, target.getXSLSource(), true, type);
                }
            }
        }
    }

    private static void getAuxdepsForInclude(TreeSet bucket, AuxDependency aux, boolean recurse, DependencyType type) {
        TreeSet children = aux.getChildren();
        if (children != null) {
            for (Iterator i = children.iterator(); i.hasNext(); ) {
                AuxDependency child = (AuxDependency) i.next();
                if (child.getType() == type) {
                    bucket.add(child);
                }
                if (recurse) {
                    getAuxdepsForInclude(bucket, child, recurse, type);
                }
            }
        }
    }

    public static void renderAllIncludesForNavigation(TreeSet includes, ResultDocument resdoc, Element root) {
        String  olddir   = "";
        String  oldpath  = "";
        Element direlem  = null;
        Element pathelem = null;
        for (Iterator i = includes.iterator(); i.hasNext(); ) {
            AuxDependency curr = (AuxDependency) i.next();
            String dir     = curr.getDir();
            String path    = curr.getPath();
            String part    = curr.getPart();
            String product = curr.getProduct();
            if (!olddir.equals(dir) || olddir.equals("")) {
                direlem = resdoc.createSubNode(root, "directory");
                direlem.setAttribute("name", dir);
                olddir = dir;
            }
            if (!oldpath.equals(path) || olddir.equals("")) {
                pathelem = resdoc.createSubNode(direlem, "path");
                pathelem.setAttribute("name", path);
                oldpath = path;
            }
            Element inc = resdoc.createSubNode(pathelem, "include");
            inc.setAttribute("path", path);
            inc.setAttribute("part", part);
            inc.setAttribute("product", product);
        }
    }

    public static void renderAllPatternMatchingIncludes(EditorSearch es, ResultDocument resdoc, Element root, String type) {
        if (es.getStatus() == es.SCODE_OK) {
            TreeSet includes = type.equals(es.INCLUDE) ? es.getResultSet() : es.getDynResultSet();
            String  olddir   = "";
            String  oldpath  = "";
            Element direlem  = null;
            Element pathelem = null;
            for (Iterator i = includes.iterator(); i.hasNext(); ) {
                AuxDependency curr = (AuxDependency) i.next();
                String dir     = curr.getDir();
                String path    = curr.getPath();
                String part    = curr.getPart();
                String product = curr.getProduct();
                if (!olddir.equals(dir) || olddir.equals("")) {
                    direlem = resdoc.createSubNode(root, "directory");
                    direlem.setAttribute("name", dir);
                    olddir = dir;
                }
                if (!oldpath.equals(path) || olddir.equals("")) {
                    pathelem = resdoc.createSubNode(direlem, "path");
                    pathelem.setAttribute("name", path);
                    oldpath = path;
                }
                Element inc = resdoc.createSubNode(pathelem, "include");
                inc.setAttribute("path", path);
                inc.setAttribute("part", part);
                inc.setAttribute("product", product);
                EditorSearchContext[] matches = type.equals(es.INCLUDE) ? es.getSearchContexts(curr) : es.getDynSearchContexts(curr);
                for (int j = 0; j < matches.length; j++) {
                    Element match = resdoc.createSubNode(inc, "match");
                    EditorSearchContext tmp = matches[j];
                    match.setAttribute("match", tmp.getMatch()); 
                    match.setAttribute("pre",   tmp.getPre());
                    match.setAttribute("post",  tmp.getPost());
                }
            }
        }
    }

}// EditorHelper
