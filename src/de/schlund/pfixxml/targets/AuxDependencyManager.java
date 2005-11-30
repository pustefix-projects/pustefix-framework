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

package de.schlund.pfixxml.targets;

import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.util.Path;
import de.schlund.pfixxml.util.Xml;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * AuxDependencyManager.java
 *
 *
 * Created: Tue Jul 17 12:24:15 2001
 *
 * @author <a href="mailto: jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class AuxDependencyManager {
    private static Category CAT    = Category.getInstance(AuxDependencyManager.class.getName());
    private static String   DEPAUX = "depaux";
    private VirtualTarget   target;
    
    public static AuxDependency root = 
        AuxDependencyFactory.getInstance().
        getAuxDependency(DependencyType.TEXT,PathFactory.getInstance().createPath("__ROOT__"), "__ROOT__", "__ROOT__");
    
    public AuxDependencyManager(Target target) {
        this.target = (VirtualTarget) target;
    }
        
    public synchronized void tryInitAuxdepend() throws Exception {
        File auxfile = new File(target.getTargetGenerator().getDisccachedir().resolve(), target.getTargetKey() + ".aux");
        if (auxfile.exists() && auxfile.canRead() && auxfile.isFile()) {
            Document        doc     = Xml.parseMutable(auxfile);
            NodeList        auxdeps = doc.getElementsByTagName(DEPAUX);
            if (auxdeps.getLength() > 0) {
                for (int j = 0; j < auxdeps.getLength(); j++) {
                    String         type           = ((Element) auxdeps.item(j)).getAttribute("type");
                    Path           path           = PathFactory.getInstance().createPath(((Element) auxdeps.item(j)).getAttribute("path"));
                    String         part           = ((Element) auxdeps.item(j)).getAttribute("part");
                    String         product        = ((Element) auxdeps.item(j)).getAttribute("product");
                    String         parent_attr    = ((Element) auxdeps.item(j)).getAttribute("parent_path");
                    Path           parent_path    = "".equals(parent_attr)? null : PathFactory.getInstance().createPath(parent_attr);
                    String         parent_part    = ((Element) auxdeps.item(j)).getAttribute("parent_part");
                    String         parent_product = ((Element) auxdeps.item(j)).getAttribute("parent_product");
                    DependencyType thetype        = DependencyType.getByTag(type);
                    addDependency(thetype, path, part, product, parent_path, parent_part, parent_product);
                }
            }
        }
    }

    public synchronized void addDependency(DependencyType type, Path path, String part, String product,
                                           Path parent_path, String parent_part, String parent_product) {
        if (path == null) {
            throw new IllegalArgumentException("path null: part=" + part + " product=" + product);
        }
        
        AuxDependency child  = null;
        AuxDependency parent = null;
        
        if (part != null && part.equals("")) part = null;
        if (product != null && product.equals("")) product = null;
        if (parent_part != null && parent_part.equals("")) parent_part = null;
        if (parent_product != null && parent_product.equals("")) parent_product = null;
        CAT.info("Adding Dependency of type '" + type + "' to Target '" + target.getFullName() + "':");
        CAT.info("*** [" + path.getRelative() + "][" + part + "][" + product + "][" +
                 ((parent_path == null)? "null" : parent_path.getRelative()) + "][" + parent_part + "][" + parent_product + "]");

        child = AuxDependencyFactory.getInstance().getAuxDependency(type, path, part, product);
        
        if (parent_path != null && parent_part != null && parent_product != null) {
            CAT.debug("*** Found another AuxDependency as Parent...");
            parent = AuxDependencyFactory.getInstance().
                getAuxDependency(DependencyType.TEXT, parent_path, parent_part, parent_product);
        } else if (parent_path == null && parent_part == null && parent_product == null) {
            parent = root;
        }
        
        if (parent != null) {
            TargetDependencyRelation.getInstance().addRelation(parent, child, target);
        } else {
            throw new RuntimeException("*** FATAL *** AuxDep " + child + " for target " + target.getFullName() 
                                       + " with parent path/part/product not all == null or all != null: "
                                       + parent_path + "#" + parent_part + "#" + parent_product);
        }
    }

    public synchronized void reset() {
        TargetDependencyRelation.getInstance().resetRelation(target);
    }

    public long getMaxTimestamp() {
        Set<AuxDependency> allaux = TargetDependencyRelation.getInstance().getDependenciesForTarget(target);
        long               max    = 0;
        
        if (allaux != null) {
            for (Iterator<AuxDependency> i = allaux.iterator(); i.hasNext();) {
                AuxDependency aux  = i.next();
                max = Math.max(max, aux.getModTime());
            }
        }
        return max;
    }
    

    public synchronized void saveAuxdepend() throws IOException  {
        CAT.info("===> Trying to save aux info of Target '" + target.getTargetKey() + "'");

        Set<AuxDependency> allaux = TargetDependencyRelation.getInstance().getDependenciesForTarget(target);
        File               path   = new File(target.getTargetGenerator().getDisccachedir().resolve(),
                                             target.getTargetKey() + ".aux");

        HashMap<AuxDependency, HashSet<AuxDependency>> parentchild = 
            TargetDependencyRelation.getInstance().getParentChildMapForTarget(target);
        
        Document auxdoc   = Xml.createDocument();
        Element  rootelem = auxdoc.createElement("aux");
        
        auxdoc.appendChild(rootelem);

        if (parentchild != null) {
            for (Iterator<AuxDependency> i = parentchild.keySet().iterator(); i.hasNext(); ) {
                AuxDependency parent       = i.next();
                Path          parent_path  = null;
                String        parent_part  = null;
                String        parent_theme = null;
                
                if (parent != root) {
                    parent_path  = parent.getPath();
                    parent_part  = parent.getPart();
                    parent_theme = parent.getTheme();
                }
                
                HashSet<AuxDependency> children = parentchild.get(parent);
                
                for (Iterator<AuxDependency> j = children.iterator(); j.hasNext(); ) {
                    AuxDependency  aux  = j.next();
                    DependencyType type = aux.getType();
                    
                    if (type.isDynamic()) {
                        Element depaux = auxdoc.createElement(DEPAUX);
                        rootelem.appendChild(depaux);
                        depaux.setAttribute("type", type.getTag());
                        depaux.setAttribute("path", aux.getPath().getRelative());
                        if (aux.getPart() != null)
                            depaux.setAttribute("part", aux.getPart());
                        if (aux.getTheme() != null)  
                            depaux.setAttribute("product", aux.getTheme());
                        if (parent_path != null) 
                            depaux.setAttribute("parent_path", parent_path.getRelative());
                        if (parent_part != null) 
                            depaux.setAttribute("parent_part", parent_part);
                        if (parent_theme != null) 
                            depaux.setAttribute("parent_product", parent_theme);
                    }
                }
            }
        }
        Xml.serialize(auxdoc, path, true, true);
    }

    public TreeSet<AuxDependency> getChildren() {
        HashMap<AuxDependency, HashSet<AuxDependency>> parentchild = 
            TargetDependencyRelation.getInstance().getParentChildMapForTarget(target);

        TreeSet<AuxDependency> retval = new TreeSet<AuxDependency>();
        
        if (parentchild != null && parentchild.get(root) != null) {
            retval.addAll(parentchild.get(root));
        }
        
        return retval;
    }
        
}// AuxDependencyManager
