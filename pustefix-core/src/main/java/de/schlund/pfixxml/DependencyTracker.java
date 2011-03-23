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

package de.schlund.pfixxml;


import org.apache.log4j.Logger;

import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.DependencyType;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.VirtualTarget;
import de.schlund.pfixxml.util.XsltContext;

public class DependencyTracker {
    private final static Logger LOG = Logger.getLogger(DependencyTracker.class);
    
    /** xslt extension */
    public static String logImage(XsltContext context, Resource path,
                                  String parent_part_in, String parent_theme_in,
                                  TargetGenerator targetGen, String targetKey, String type) throws Exception {

        if (targetKey.equals("__NONE__")) {
            return "0";
        }

        VirtualTarget   target    = (VirtualTarget) targetGen.getTarget(targetKey);

        String parent_path  = "";
        String parent_part  = "";
        String parent_theme = "";

        if (IncludeDocumentExtension.isIncludeDocument(context)) {
            parent_path  = IncludeDocumentExtension.getSystemId(context);
            parent_part  = parent_part_in;
            parent_theme = parent_theme_in;
        }
        
        if (target == null) {
            LOG.error("Error adding Dependency: target not found (targetGen=" + targetGen + ", targetKey=" + targetKey + ")");
            return "1";
        }
        
        Resource relativeParent = parent_path.equals("") ? null : ResourceUtil.getResource(parent_path);
        try {
            logTyped(type, path, "", "", relativeParent, parent_part, parent_theme, target);
            return "0";
        } catch (Exception e) {
            LOG.error("Error adding Dependency: ",e); 
            return "1"; 
        }
    }
    
    public static void logTyped(String type, Resource path, String part, String theme,
                                Resource parent_path, String parent_part, String parent_theme,
                                VirtualTarget target) {
        if (LOG.isDebugEnabled()) {
            String project = target.getTargetGenerator().getName();
            LOG.debug("Adding dependency to AuxdependencyManager :+\n"+
                      "Type       = " + type + "\n" +
                      "Path       = " + path.toURI().toString() + "\n" +
                      "Part       = " + part + "\n" +
                      "Theme      = " + theme + "\n" +
                      "ParentPath = " + ((parent_path == null)? "null" : parent_path.toURI().toString()) + "\n" +
                      "ParentPart = " + parent_part + "\n" +
                      "ParentProd = " + parent_theme + "\n" +
                      "Project    = " + project + "\n");
        }
        DependencyType  thetype   = DependencyType.getByTag(type);
        if (thetype == DependencyType.TEXT) {
            target.getAuxDependencyManager().addDependencyInclude(path, part, theme, parent_path, parent_part, parent_theme);
        } else if (thetype == DependencyType.IMAGE) {
            target.getAuxDependencyManager().addDependencyImage(path, parent_path, parent_part, parent_theme);
        } else {
            throw new RuntimeException("Unknown dependency type '" + type + "'!");
        }
    }
}
