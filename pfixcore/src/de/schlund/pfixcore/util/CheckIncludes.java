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
package de.schlund.pfixcore.util;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import de.schlund.pfixxml.util.Path;
import de.schlund.pfixxml.util.Xml;
import java.io.*;
import java.util.*;
import org.apache.log4j.xml.*;
import org.w3c.dom.*;

/**
 * CheckIncludes.java
 *
 *
 * Created: Tue Apr  8 15:42:11 2003
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version $Id$
 */

public class CheckIncludes {
    private static final String XPATH = "/include_parts/part/theme";
    
    private HashMap generators       = new HashMap();
    private TreeSet includefilenames = new TreeSet();
    private TreeSet imagefilenames   = new TreeSet();
    private TreeSet unavail          = new TreeSet();
    private TreeSet includes;
    private String  pwd;
    private String  outfile;

    static {
//        dbfac.setNamespaceAware(true);
    }
    
    public CheckIncludes(String pwd, String outfile, File allprj, File allincs, File allimgs) throws Exception {
        this.pwd     = pwd;
        this.outfile = outfile;
        String         line;
        BufferedReader input;
        
        input = new BufferedReader(new FileReader(allincs));
        while ((line = input.readLine()) != null) {
            // line = new File(pwd + line).getCanonicalPath();
            includefilenames.add(PathFactory.getInstance().createPath(line));
        }
        input.close();

        input = new BufferedReader(new FileReader(allimgs));
        while ((line = input.readLine()) != null) {
            // line = new File(pwd + line).getCanonicalPath();
            imagefilenames.add(PathFactory.getInstance().createPath(line));
        }
        input.close();
        
        input = new BufferedReader(new FileReader(allprj));
        while ((line = input.readLine()) != null) {
            // line = pwd + line;
            TargetGenerator gen = new TargetGenerator(PathFactory.getInstance().createPath(line));
            generators.put(pwd + line, gen);
        }
        input.close();

        includes = AuxDependencyFactory.getInstance().getAllAuxDependencies();
        for (Iterator i = includes.iterator(); i.hasNext();) {
            AuxDependency aux = (AuxDependency) i.next();
            unavail.add(aux);
        }
    }

    public void doCheck() throws Exception {
        Document doc         = Xml.createDocument();

        Element  check_root  = doc.createElement("checkresult");
        check_root.setAttribute("xmlns:ixsl","http://www.w3.org/1999/XSL/Transform");
        check_root.setAttribute("xmlns:pfx", "http://www.schlund.de/pustefix/core");
        check_root.setAttribute("xmlns:hlp", "http://www.schlund.de/pustefix/onlinehelp");
        Element  files_root  = doc.createElement("includefiles");
        Element  images_root = doc.createElement("images");
        Element  prj_root    = doc.createElement("projects");
        doc.appendChild(check_root);
        check_root.appendChild(files_root);
        check_root.appendChild(images_root);
        check_root.appendChild(prj_root);
        
        checkForUnusedIncludes(doc, files_root);
        checkForUnusedImages(doc, images_root);

        checkForUnavailableIncludes(doc, prj_root);
        
        Xml.serialize(doc, outfile, true, true);
    }
    
    public static void main(String[] args) throws Exception {
        String output    = args[0];
        String allprjarg = args[1];
        String allincarg = args[2];
        String allimgarg = args[3];

        String dir = new File(".").getCanonicalPath() + "/";
        
        DOMConfigurator.configure(dir + "core/conf/generator_quiet.xml");
        PathFactory.getInstance().init(dir);
        
        CheckIncludes instance = new CheckIncludes(dir, output, new File(allprjarg), new File(allincarg), new File(allimgarg));
        instance.doCheck();

    }

    private void checkForUnavailableIncludes(Document result, Element res_root) throws Exception {
        for (Iterator i = generators.keySet().iterator(); i.hasNext();) {
            String          depend  = (String) i.next();
            TargetGenerator gen     = (TargetGenerator) generators.get(depend);
            // TreeSet         deps = gen.getDependencyRefCounter().getAllDependencies();
            TreeSet         deps    = TargetDependencyRelation.getInstance().getProjectDependencies(gen);
            if (deps == null) {
                return;
            }

            for (Iterator j = deps.iterator(); j.hasNext();) {
                AuxDependency aux = (AuxDependency) j.next();
                if (!unavail.contains(aux) || aux.getType().equals(DependencyType.FILE) || aux.getType().equals(DependencyType.TARGET)) {
                    j.remove();
                }
            }
            
            if (!deps.isEmpty()) {
                Element prj_elem = result.createElement("project");
                String  name     = depend.substring(pwd.length());
                prj_elem.setAttribute("name", name);
                res_root.appendChild(prj_elem);
                
                for (Iterator j = deps.iterator(); j.hasNext(); ) {
                    AuxDependency aux = (AuxDependency) j.next();
                    Element elem = result.createElement("MISSING");
                    prj_elem.appendChild(elem);
                    elem.setAttribute("type", aux.getType().toString());
                    if (aux.getType().equals(DependencyType.TEXT)) {
                        AuxDependencyInclude a = (AuxDependencyInclude) aux;
                        elem.setAttribute("path", a.getPath().getRelative());
                        elem.setAttribute("part", a.getPart());
                        elem.setAttribute("theme", a.getTheme());
                    } else if (aux.getType().equals(DependencyType.IMAGE)) {
                        AuxDependencyImage a = (AuxDependencyImage) aux;
                        elem.setAttribute("path", a.getPath().getRelative());
                    }
                }
            }
        }
    }

    private void checkForUnusedImages(Document result, Element res_root) throws Exception {
        IncludeDocumentFactory incfac = IncludeDocumentFactory.getInstance();
        for (Iterator i = imagefilenames.iterator(); i.hasNext();) {
            Path path = (Path) i.next();
            File img  = path.resolve();
            String fullpath = img.getCanonicalPath();

            Element res_image = result.createElement("image");
            res_image.setAttribute("name", path.getRelative());
            
            res_root.appendChild(res_image);
            
            AuxDependency aux =  AuxDependencyFactory.getInstance().getAuxDependencyImage(path);
            if (!includes.contains(aux)) {
                res_image.setAttribute("UNUSED", "true");
                continue;
            } else {
                unavail.remove(aux);
            }
        }
    }
    
    private void checkForUnusedIncludes(Document result, Element res_root) throws Exception {
        for (Iterator i = includefilenames.iterator(); i.hasNext();) {
            Path path = (Path) i.next();
            Document doc;

            Element res_incfile = result.createElement("incfile");
            res_root.appendChild(res_incfile);
            res_incfile.setAttribute("name", path.getRelative());
            
            try {
                doc = Xml.parseMutable(path.resolve());
            } catch (Exception e) {
                Element error = result.createElement("ERROR");
                res_incfile.appendChild(error);
                error.setAttribute("cause", e.getMessage());
                continue;
            }
            
            Element root = doc.getDocumentElement();
            if (!root.getNodeName().equals("include_parts")) {
                Element error = result.createElement("ERROR");
                res_incfile.appendChild(error);
                error.setAttribute("cause", "not an include file (root != include_parts)");
                continue;
            }
            
            NodeList nl = root.getChildNodes();
            for (int j = 0; j < nl.getLength(); j++) {
                if (nl.item(j) instanceof Element) {
                    Element partelem = (Element) nl.item(j);
                    
                    if (!partelem.getNodeName().equals("part")) {
                        Element error = result.createElement("ERROR");
                        res_incfile.appendChild(error);
                        error.setAttribute("cause", "invalid node in include file (child of root != part): " + path.getRelative() + "/" + partelem.getNodeName());
                        continue;
                    }

                    Element res_part = result.createElement("part");
                    res_incfile.appendChild(res_part);
                    res_part.setAttribute("name", partelem.getAttribute("name"));
                    
                    NodeList prodchilds = partelem.getChildNodes();
                    for (int k = 0; k < prodchilds.getLength(); k++) {
                        if (prodchilds.item(k) instanceof Element) {
                            Element themeelem = (Element) prodchilds.item(k);
                            if (!themeelem.getNodeName().equals("theme")) {
                                Element error = result.createElement("ERROR");
                                res_part.appendChild(error);
                                error.setAttribute("cause", "invalid node in part (child of part != theme): " +
                                                   path.getRelative() + "/" + partelem.getNodeName() + "/" + themeelem.getNodeName());
                                continue;
                            }

                            String part    = partelem.getAttribute("name");
                            String theme = themeelem.getAttribute("name");

                            Element res_theme = result.createElement("theme");
                            res_part.appendChild(res_theme);
                            res_theme.setAttribute("name", theme);
                            
                            AuxDependency aux =
                                AuxDependencyFactory.getInstance().getAuxDependencyInclude(path, part, theme);
                            if (!includes.contains(aux)) {
                                res_theme.setAttribute("UNUSED", "true");
                                continue;
                            } else {
                                unavail.remove(aux);
                            }
                            

                            NodeList langchilds = themeelem.getChildNodes();
                            for (int l = 0; l < langchilds.getLength(); l++) {
                                if (langchilds.item(l) instanceof Element) {
                                    Element langelem = (Element) langchilds.item(l);
                                    Node res_lang = result.importNode(langelem, true); 
                                    res_theme.appendChild(res_lang);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
} // CheckIncludes
