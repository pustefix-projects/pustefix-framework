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
import de.schlund.pfixxml.util.*;
import java.io.*;
import java.util.*;
import org.apache.log4j.*;
import org.w3c.dom.*;

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

    private VirtualTarget  target;
    private HashSet auxset = new HashSet();
    
    public boolean addChild(AuxDependency aux) {
        synchronized (auxset) {
            aux.addTargetDependency(target);
            return auxset.add(aux);
        }
    }
    
    public TreeSet getChildren() {
        synchronized(auxset) {
            return new TreeSet(auxset);
        }
    }

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

    public synchronized void saveAuxdepend() throws IOException  {
        CAT.info("===> Trying to save aux info of Target '" + target.getTargetKey() + "'");
        File path = new File(target.getTargetGenerator().getDisccachedir().resolve(), target.getTargetKey() + ".aux");
        
        Document auxdoc = Xml.createDocument();
        Element  root   = auxdoc.createElement("aux");
        auxdoc.appendChild(root);
        
        saveIt(DEPAUX, auxdoc, root, auxset, null);
        Xml.serialize(auxdoc, path, true, true);
    }

    public synchronized void addDependency(DependencyType type, Path path, String part, String product,
                                           Path parent_path, String parent_part, String parent_product) {
        if (path == null) {
            throw new IllegalArgumentException("path null: part=" + part + " product=" + product);
        }
        AuxDependency child  = null;
        AuxDependency parent = null;
        boolean addtomanager = false;
        
        if (part != null && part.equals("")) part = null;
        if (product != null && product.equals("")) product = null;
        if (parent_part != null && parent_part.equals("")) parent_part = null;
        if (parent_product != null && parent_product.equals("")) parent_product = null;
        CAT.info("Adding Dependency of type '" + type + "' to Target '" + target.getTargetKey() + "':");
        CAT.info("*** [" + path.getRelative() + "][" + part + "][" + product + "][" +
                 ((parent_path == null)? "null" : parent_path.getRelative()) + "][" + parent_part + "][" + parent_product + "]");

        child = AuxDependencyFactory.getInstance().getAuxDependency(type, path, part, product);
        
        if (parent_path != null && parent_part != null && parent_product != null) {
            CAT.debug("*** Found another AuxDependency as Parent...");
            parent = AuxDependencyFactory.getInstance().
                getAuxDependency(DependencyType.TEXT, parent_path, parent_part, parent_product);

            // Check for any loops like Parent -> Aux -> Child_A -> ...-> Child_X -> Parent
            if (!checkLoopFree(parent, child)) {
                throw new RuntimeException("*** FATAL *** Adding " + child + " to Parent " + parent + " would result in a LOOP!");
            }
        } else if (parent_path == null && parent_part == null && parent_product == null) {
            CAT.debug("*** Has no AuxDependency as parent...");
            addtomanager = true;
        }
        
        if (parent != null || addtomanager) {
            // we remove the children (if any) of the "child" AuxDependency, because "child" will be processed
            // now and all current children of "child" will be added again
            child.removeChildren(target);
            
            // Here we make sure that the "child" AuxDep will be added to its parent.
            // So this is where the parent get's its child map rebuild.
            // If there's no parent, add it to the manager itself.
            if (addtomanager) {
                addChild(child);
            } else {
                parent.addChild(child, target);
            }
            // finally we take care of refcounting.
            DependencyRefCounter refcounter = target.getTargetGenerator().getDependencyRefCounter();
            refcounter.ref(child, target);
        } else {
            throw new RuntimeException("AuxDep with parent path/part/product not all == null or all != null: "
                                       + parent_path + "#" + parent_part + "#" + parent_product);
        }
    }

    public void reset() {
        DependencyRefCounter refcounter = target.getTargetGenerator().getDependencyRefCounter();
        refcounter.unref(target);

        synchronized(auxset) {
            for (Iterator i = auxset.iterator(); i.hasNext(); ) {
                AuxDependency aux  = (AuxDependency) i.next();
                if (aux.getType().isDynamic()) {
                    aux.resetTargetDependency(target);
                    i.remove();
                }
            }
        }
    }

    public long getMaxTimestamp(boolean willrebuild) {
        synchronized (auxset) {
            return lookForTimestamp(auxset, willrebuild);
        }
    }

    //
    // Private Stuff
    //

    private long lookForTimestamp(Set in, boolean willrebuild) {
        long max = 0;
        for (Iterator i = in.iterator(); i.hasNext(); ) {
            AuxDependency aux = (AuxDependency) i.next();
            max = Math.max(max, aux.getModTime());
            Set children = aux.getChildren(target);
            if (children != null) {
                max = Math.max(max, lookForTimestamp(children, willrebuild));
            }
        }
        return max;
    }

    private void saveIt(String name, Document doc, Element root, Set in, AuxDependency parent) {
        Path   parent_path    = null;
        String parent_part    = null;
        String parent_product = null;

        if (parent != null) {
            parent_path    = parent.getPath();
            parent_part    = parent.getPart();
            parent_product = parent.getProduct();
        }
        
        for (Iterator i = in.iterator(); i.hasNext(); ) {
            AuxDependency aux = (AuxDependency) i.next();
            if (aux.getType().isDynamic()) {
                Element depaux = doc.createElement(name);
                String  type;
                
                type = aux.getType().getTag();
                root.appendChild(depaux);
                depaux.setAttribute("type", type);
                depaux.setAttribute("path", aux.getPath().getRelative());
                if (aux.getPart() != null)
                    depaux.setAttribute("part", aux.getPart());
                if (aux.getProduct() != null)  
                    depaux.setAttribute("product", aux.getProduct());
                if (parent_path != null) 
                    depaux.setAttribute("parent_path", parent_path.getRelative());
                if (parent_part != null) 
                    depaux.setAttribute("parent_part", parent_part);
                if (parent_product != null) 
                    depaux.setAttribute("parent_product", parent_product);
            }
            Set children = aux.getChildren(target);
            if (children != null && children.size() > 0) {
                saveIt(name, doc, root, children, aux);
            }
        }
    }

    private boolean checkLoopFree(AuxDependency parent, AuxDependency aux) {
        // The simple loop
        if (parent == aux) {
            return false;
        }
        
        // Now iterate over all children of aux recursively and check if any of them is parent.
        Set children = aux.getChildren(target);
        if (children != null) {
            for (Iterator i = children.iterator(); i.hasNext();) {
                AuxDependency child = (AuxDependency) i.next();
                if (child.getType() == DependencyType.TEXT) {
                    if (!checkLoopFree(parent, child)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

}// AuxDependencyManager
