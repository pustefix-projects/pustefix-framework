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

import java.io.*;
import java.util.*;
import org.apache.log4j.*;
import org.w3c.dom.*;
import org.apache.xml.serialize.*;

import de.schlund.util.*;

import javax.xml.parsers.*;

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

public class AuxDependencyManager implements DependencyParent {
    private static Category               CAT    = Category.getInstance(AuxDependencyManager.class.getName());
    private static DocumentBuilderFactory dbfac  = DocumentBuilderFactory.newInstance();
    private static String                 DEPAUX = "depaux";

    private Target  target;
    private HashSet auxset = new HashSet();
    
    // DependencyParent interface

    public TreeSet getAffectedTargets() {
        TreeSet tmp = new TreeSet();
        tmp.add(target);
        return tmp;
    }

    public boolean addChild(AuxDependency aux) {
        synchronized (auxset) {
            aux.addDependencyParent(this);
            return auxset.add(aux);
        }
    }
    
    public TreeSet getChildren() {
        synchronized(auxset) {
            return new TreeSet(auxset);
        }
    }

    // ----------------------------------
    
    public AuxDependencyManager(Target target) {
        this.target = target;
        if (!dbfac.isNamespaceAware()) {
            CAT.warn("\n**** Switching DocumentBuilderFactory to be NS-aware ****");
            dbfac.setNamespaceAware(true);
        }
        if (dbfac.isValidating()) {
            CAT.warn("\n**** Switching DocumentBuilderFactory to be non-validating ****");
            dbfac.setValidating(false);
        }
    }
        
    public synchronized void tryInitAuxdepend() throws Exception {
        String auxpath = target.getTargetGenerator().getDisccachedir() + target.getTargetKey() + ".aux";
        File   auxfile = new File(auxpath);
        if (auxfile.exists() && auxfile.canRead() && auxfile.isFile()) {
            DocumentBuilder domp    = dbfac.newDocumentBuilder();
            Document        doc     = domp.parse(auxpath);
            NodeList        auxdeps = doc.getElementsByTagName(DEPAUX);
            if (auxdeps.getLength() > 0) {
                String docroot = target.getTargetGenerator().getDocroot();
                for (int j = 0; j < auxdeps.getLength(); j++) {
                    String type            = ((Element) auxdeps.item(j)).getAttribute("type");
                    Path path              = Path.create(docroot, ((Element) auxdeps.item(j)).getAttribute("path"));
                    String part            = ((Element) auxdeps.item(j)).getAttribute("part");
                    String product         = ((Element) auxdeps.item(j)).getAttribute("product");
                    Path parent_path       = Path.createOpt(docroot, ((Element) auxdeps.item(j)).getAttribute("parent_path"));
                    String parent_part     = ((Element) auxdeps.item(j)).getAttribute("parent_part");
                    String parent_product  = ((Element) auxdeps.item(j)).getAttribute("parent_product");
                    DependencyType thetype = DependencyType.getByTag(type);
                    addDependency(thetype, path, part, product, parent_path, parent_part, parent_product);
                }
            }
        }
    }

    public synchronized void saveAuxdepend() throws ParserConfigurationException, IOException  {
        CAT.info("===> Trying to save aux info of Target '" + target.getTargetKey() + "'");
        String path = target.getTargetGenerator().getDisccachedir() + target.getTargetKey() + ".aux";
        
        Document auxdoc = dbfac.newDocumentBuilder().newDocument();
        Element  root   = auxdoc.createElement("aux");
        auxdoc.appendChild(root);
            
        saveIt(DEPAUX, auxdoc, root, auxset, null);
        
        FileOutputStream output = new FileOutputStream(path);
        OutputFormat     outfor = new OutputFormat("xml","ISO-8859-1",true);
        outfor.setLineWidth(0);
        XMLSerializer ser    =  new XMLSerializer(output, outfor);
        ser.serialize(auxdoc);
    }

    public synchronized void addDependency(DependencyType type, Path path, String part, String product,
                                           Path parent_path, String parent_part, String parent_product) {
        if (path == null) {
            throw new IllegalArgumentException("path null: part=" + part + " product=" + product);
        }
        AuxDependency    aux    = null;
        DependencyParent parent = null;

        if (part != null && part.equals("")) part = null;
        if (product != null && product.equals("")) product = null;
        if (parent_part != null && parent_part.equals("")) parent_part = null;
        if (parent_product != null && parent_product.equals("")) parent_product = null;
        CAT.info("Adding Dependency of type '" + type + "' to Target '" + target.getTargetKey() + "':");
        CAT.info("*** [" + path.getRelative() + "][" + part + "][" + product + "][" +
                 ((parent_path == null)? "null" : parent_path.getRelative()) + "][" + parent_part + "][" + parent_product + "]");

        aux = AuxDependencyFactory.getInstance().getAuxDependency(type, path, part, product);
        
        if (parent_path != null && parent_part != null && parent_product != null) {
            CAT.debug("*** Found another AuxDependency as Parent...");
            parent = AuxDependencyFactory.getInstance().
                getAuxDependency(DependencyType.TEXT, parent_path, parent_part, parent_product);

            // Check for any loops like Parent -> Aux -> Child_A -> ...-> Child_X -> Parent
            if (!checkLoopFree(parent, aux)) {
                throw new RuntimeException("*** FATAL *** Adding " + aux + " to Parent " + parent + " would result in a LOOP!");
            }
        } else if (parent_path == null && parent_part == null && parent_product == null) {
            CAT.debug("*** Has no AuxDependency as parent...");
            parent = this;
        }
        
        if (parent != null) {
            
            // we remove the new aux from every current childs parent
            // map. They will add it as a parent later on themself.
            // After that, we will Reset the children map of the
            // AuxDep. The children will add themself later on their own.
            aux.reset();
            
            // Here we make sure that this AuxDep's parent will get it's children map rebuild.
            // The parent may be another AuxDependency or the Manager (see above).
            parent.addChild(aux);

            // finally we take care of refcounting.
            DependencyRefCounter refcounter = target.getTargetGenerator().getDependencyRefCounter();
            refcounter.ref(aux, target);
        } else {
            throw new RuntimeException("AuxDep with parent path/part/product not all == null or all != null: "
                                       + parent_path + "/" + parent_part + "/" + parent_product);
        }
    }

    public void reset() {
        DependencyRefCounter refcounter = target.getTargetGenerator().getDependencyRefCounter();
        refcounter.unref(target);

        synchronized(auxset) {
            for (Iterator i = auxset.iterator(); i.hasNext(); ) {
                AuxDependency aux  = (AuxDependency) i.next();
                aux.removeDependencyParent(this);
                if (aux.isDynamic()) {
                    i.remove();
                }
            }
        }
    }

    public long getMaxTimestamp() {
        synchronized (auxset) {
            return lookForTimestamp(auxset);
        }
    }

    //
    // Private Stuff
    //

    private long lookForTimestamp(Set in) {
        long max = 0l;
        for (Iterator i = in.iterator(); i.hasNext(); ) {
            AuxDependency aux = (AuxDependency) i.next();
            max = Math.max(max, aux.getModTime());
            Set children = aux.getChildren();
            if (children != null) {
                max = Math.max(max, lookForTimestamp(children));
            }
        }
        return max;
    }

    private void saveIt(String name, Document doc, Element root, Set in, AuxDependency parent) {
        Path parent_path    = null;
        String parent_part    = null;
        String parent_product = null;

        if (parent != null) {
            parent_path    = parent.getPath();
            parent_part    = parent.getPart();
            parent_product = parent.getProduct();
        }

        for (Iterator i = in.iterator(); i.hasNext(); ) {
            AuxDependency aux = (AuxDependency) i.next();
            if (aux.isDynamic()) {
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
            TreeSet children = aux.getChildren();
            if (children != null && children.size() > 0) {
                saveIt(name, doc, root, children, aux);
            }
        }
    }

    private boolean checkLoopFree(DependencyParent parent, AuxDependency aux) {
        // The simple loop
        if (parent == aux) {
            return false;
        }
        
        // Now iterate over all children of aux recursively and check if any of them is parent.
        TreeSet children = aux.getChildren();
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
