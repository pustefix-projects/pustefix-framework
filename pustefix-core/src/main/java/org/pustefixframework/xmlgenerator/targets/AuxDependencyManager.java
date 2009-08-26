/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.pustefixframework.xmlgenerator.targets;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.pustefixframework.resource.FileResource;
import org.pustefixframework.resource.InputStreamResource;
import org.pustefixframework.resource.OutputStreamResource;
import org.pustefixframework.resource.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.schlund.pfixxml.util.ResourceUtils;
import de.schlund.pfixxml.util.Xml;

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
	
    private final static Logger   LOG    = Logger.getLogger(AuxDependencyManager.class);
    private final static String   DEPAUX = "depaux";
    private TargetImpl   target;
    private TargetDependencyRelation targetDependencyRelation;
    
    public AuxDependency root; 
    
    public AuxDependencyManager(Target target) {
    	this.targetDependencyRelation = target.getTargetGenerator().getAuxDependencyFactory().getTargetDependencyRelation();
        this.target = (TargetImpl) target;
        this.root = target.getTargetGenerator().getAuxDependencyFactory().getAuxDependencyRoot();
    }
        
    public synchronized void tryInitAuxdepend() throws Exception {
    	Resource auxfile = target.getTargetAuxResource();
        if (ResourceUtils.exists(auxfile)) {
            Document        doc     = Xml.parseMutable((InputStreamResource)auxfile);
            NodeList        auxdeps = doc.getElementsByTagName(DEPAUX);
            if (auxdeps.getLength() > 0) {
                for (int j = 0; j < auxdeps.getLength(); j++) {
                    String          type           = ((Element) auxdeps.item(j)).getAttribute("type");
                    String          path_attr      = ((Element) auxdeps.item(j)).getAttribute("path");
                    Resource path = null;
                    if(!path_attr.equals("")) {
                    	URI uri = new URI(path_attr);
                    	path = target.getTargetGenerator().getResourceLoader().getResource(uri);
                    }
                    if(path != null) {
                        String          orig_uri       = ((Element) auxdeps.item(j)).getAttribute("orig_uri");
                        URI uri = new URI(orig_uri);
                        //TODO: set original URI
                        //path.setOriginatingURI();
                    }
                    String          part           = ((Element) auxdeps.item(j)).getAttribute("part");
                    String          theme          = ((Element) auxdeps.item(j)).getAttribute("theme");
                    String          parent_attr    = ((Element) auxdeps.item(j)).getAttribute("parent_path");
                    Resource parent_path = null;
                    if(!parent_attr.equals("")) {
                    	URI uri = new URI(parent_attr);
                    	parent_path = target.getTargetGenerator().getResourceLoader().getResource(uri);
                    }
                    String          parent_part    = ((Element) auxdeps.item(j)).getAttribute("parent_part");
                    String          parent_theme = ((Element) auxdeps.item(j)).getAttribute("parent_theme");
                    String          target_attr    = ((Element) auxdeps.item(j)).getAttribute("target");

                    DependencyType thetype        = DependencyType.getByTag(type);
                    if (thetype == DependencyType.TEXT) {
                        addDependencyInclude(path, part, theme, parent_path, parent_part, parent_theme);
                    } else if (thetype == DependencyType.IMAGE) {
                        addDependencyImage(path, parent_path, parent_part, parent_theme);
                    } else if (thetype == DependencyType.TARGET) {
                        addDependencyTarget(target_attr);
                    }
                }
            }
        }
    }
    
    private AuxDependency getParentDependency(Resource parent_path,
            String parent_part, String parent_theme) {
        AuxDependency parent = null;

        if (parent_part != null && parent_part.equals(""))
            parent_part = null;
        if (parent_theme != null && parent_theme.equals(""))
            parent_theme = null;

        if (parent_path != null && parent_part != null && parent_theme != null) {
            LOG.debug("*** Found another AuxDependency as Parent...");
            parent = target.getTargetGenerator().getAuxDependencyFactory()
                    .getAuxDependencyInclude(parent_path, target.getTargetGenerator().getResourceLoader(), parent_part,
                            parent_theme);
        } else if (parent_path == null && parent_part == null
                && parent_theme == null) {
            parent = root;
        }

        if (parent != null) {
            return parent;
        } else {
            throw new IllegalArgumentException(
                    "Mixed null and non-null values for parent arguments!");
        }
    }
    
    public synchronized void addDependencyInclude(Resource path, String part, String theme, Resource parent_path, String parent_part, String parent_theme) {
        if (path == null || part == null || theme == null) {
            throw new IllegalArgumentException("Null pointer is not allowed here");
        }
        
        AuxDependency child  = null;
        AuxDependency parent = null;
        
        if (part != null && part.equals("")) part = null;
        if (theme != null && theme.equals("")) theme = null;
        LOG.info("Adding Dependency of type 'text' to Target '" + target.getFullName() + "':");
        LOG.info("*** [" + path.toString() + "][" + part + "][" + theme + "][" +
                 ((parent_path == null)? "null" : parent_path.toString()) + "][" + parent_part + "][" + parent_theme + "]");

        child = target.getTargetGenerator().getAuxDependencyFactory().getAuxDependencyInclude(path, target.getTargetGenerator().getResourceLoader(), part, theme);
        parent = getParentDependency(parent_path, parent_part, parent_theme);
        
        targetDependencyRelation.addRelation(parent, child, target);
    }
    
    public synchronized void addDependencyImage(Resource path, Resource parent_path, String parent_part, String parent_theme) {
        if (path == null) {
            throw new IllegalArgumentException("Null pointer is not allowed here");
        }
        
        AuxDependency child  = null;
        AuxDependency parent = null;

        LOG.info("Adding Dependency of type 'text' to Target '" + target.getFullName() + "':");
        LOG.info("*** [" + path.toString() + "][" +
                 ((parent_path == null)? "null" : parent_path.toString()) + "][" + parent_part + "][" + parent_theme + "]");

        child = target.getTargetGenerator().getAuxDependencyFactory().getAuxDependencyImage(path, target.getTargetGenerator().getResourceLoader());
        parent = getParentDependency(parent_path, parent_part, parent_theme);
        
        targetDependencyRelation.addRelation(parent, child, target);
    }
    
    public synchronized void addDependencyFile(Resource path) {
        if (path == null) {
            throw new IllegalArgumentException("Null pointer is not allowed here");
        }
        
        AuxDependency child  = null;

        LOG.info("Adding Dependency of type 'text' to Target '" + target.getFullName() + "':");
        LOG.info("*** [" + path.toString() + "]");

        child = target.getTargetGenerator().getAuxDependencyFactory().getAuxDependencyFile(path, target.getTargetGenerator().getResourceLoader());
        
        targetDependencyRelation.addRelation(root, child, target);
    }
    
    public synchronized void addDependencyTarget(String targetkey) {
        if (target == null) {
            throw new IllegalArgumentException("Null pointer is not allowed here");
        }
        
        AuxDependency child  = null;

        LOG.info("Adding Dependency of type 'text' to Target '" + target.getFullName() + "':");
        LOG.info("*** [" + target.getTargetKey() + "]");

        child = target.getTargetGenerator().getAuxDependencyFactory().getAuxDependencyTarget(target.getTargetGenerator(), targetkey);
        
        targetDependencyRelation.addRelation(root, child, target);
    }

    public synchronized void reset() {
    	targetDependencyRelation.resetRelation(target);
    }

    /**
     * Returns the highest (= newest) timestamp of all aux dependencies
     * (include parts, images, files) managed through this manager.
     * This does NOT include any aux targets.
     * 
     * @return Timestamp of latest change in any dependency
     */
    public long getMaxTimestamp() {
        Set<AuxDependency> allaux = targetDependencyRelation.getDependenciesForTarget(target);
        long               max    = 0;
        
        if (allaux != null) {
            for (Iterator<AuxDependency> i = allaux.iterator(); i.hasNext();) {
                AuxDependency aux  = i.next();
                if (aux.getType() != DependencyType.TARGET) {
                    max = Math.max(max, aux.getModTime());
                }
            }
        }
        return max;
    }
    

    public synchronized void saveAuxdepend() throws IOException  {
        LOG.info("===> Trying to save aux info of Target '" + target.getTargetKey() + "'");

        FileResource path = target.getTargetAuxResource();
        File dir = path.getFile().getParentFile();
        
        // Make sure parent directory is existing (for leaf targets)
        if (dir != null) {
            dir.mkdirs();
        }

        HashMap<AuxDependency, HashSet<AuxDependency>> parentchild = 
        	targetDependencyRelation.getParentChildMapForTarget(target);
        
        Document auxdoc   = Xml.createDocument();
        Element  rootelem = auxdoc.createElement("aux");
        
        auxdoc.appendChild(rootelem);

        if (parentchild != null) {
            for (Iterator<AuxDependency> i = parentchild.keySet().iterator(); i.hasNext(); ) {
                AuxDependency parent       = i.next();
                Resource  parent_path  = null;
                String        parent_part  = null;
                String        parent_theme = null;
                
                if (parent != root) {
                    AuxDependencyInclude aux = (AuxDependencyInclude) parent;
                    parent_path  = aux.getPath();
                    parent_part  = aux.getPart();
                    parent_theme = aux.getTheme();
                }
                
                HashSet<AuxDependency> children = parentchild.get(parent);
                
                for (Iterator<AuxDependency> j = children.iterator(); j.hasNext(); ) {
                    AuxDependency  aux  = j.next();
                    DependencyType type = aux.getType();
                    
                    if (type.isDynamic()) {
                        Element depaux = auxdoc.createElement(DEPAUX);
                        rootelem.appendChild(depaux);
                        depaux.setAttribute("type", type.getTag());
                        if (aux.getType() == DependencyType.TEXT) {
                            AuxDependencyInclude a = (AuxDependencyInclude) aux;
                            depaux.setAttribute("path", a.getPath().getURI().toString());
                            if(a.getPath().getOriginalURI() != null) {
                                depaux.setAttribute("orig_uri", a.getPath().getOriginalURI().toString());
                            }
                            depaux.setAttribute("part", a.getPart());
                            depaux.setAttribute("theme", a.getTheme());
                            if (parent_path != null) 
                                depaux.setAttribute("parent_path", parent_path.getURI().toString());
                            if (parent_part != null) 
                                depaux.setAttribute("parent_part", parent_part);
                            if (parent_theme != null) 
                                depaux.setAttribute("parent_theme", parent_theme);
                        } else if (aux.getType() == DependencyType.IMAGE) {
                            AuxDependencyImage a = (AuxDependencyImage) aux;
                            depaux.setAttribute("path", a.getPath().getURI().toString());
                            if(a.getPath().getOriginalURI() != null) {
                                depaux.setAttribute("orig_uri", a.getPath().getOriginalURI().toString());
                            }
                            if (parent_path != null) 
                                depaux.setAttribute("parent_path", parent_path.getURI().toString());
                            if (parent_part != null) 
                                depaux.setAttribute("parent_part", parent_part);
                            if (parent_theme != null) 
                                depaux.setAttribute("parent_theme", parent_theme);
                        } else if (aux.getType() == DependencyType.TARGET) {
                            Target target = ((AuxDependencyTarget) aux).getTarget();
                            depaux.setAttribute("target", target.getTargetKey());
                        }
                    }
                }
            }
        }
        Xml.serialize(auxdoc, (OutputStreamResource)path, true, true);
    }

    public TreeSet<AuxDependency> getChildren() {
        HashMap<AuxDependency, HashSet<AuxDependency>> parentchild = 
        	targetDependencyRelation.getParentChildMapForTarget(target);

        TreeSet<AuxDependency> retval = new TreeSet<AuxDependency>();
        
        if (parentchild != null && parentchild.get(root) != null) {
            retval.addAll(parentchild.get(root));
        }
        
        return retval;
    }
        
}// AuxDependencyManager
