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

package de.schlund.pfixxml;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.TargetGeneratorFactory;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;

/**
 * Initializes the TargetGenerators of the projects on server startup.
 * This is only useful in production mode, when the editor factory is
 * deactivated and thus does not do this job.
 * Set the property "projectinit.projectlist" to specify a comma-seperated
 * list of all projects which should be initialized on startup.
 * If no such property is specified, all known projects are initialized.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ProjectInitFactory {
    private final static String PROJECTS_XML = "servletconf/projects.xml";
    
    private static ProjectInitFactory instance = new ProjectInitFactory();
    
    public static ProjectInitFactory getInstance() {
        return instance;
    }

    public void init(Properties props) throws Exception {
        FileResource projectsFile = ResourceUtil.getFileResourceFromDocroot(PROJECTS_XML);
        Document doc = Xml.parseMutable(projectsFile);

        String projectlist = props.getProperty("projectinit.projectlist");
        if (projectlist == null) {
            // Get a list of all TargetGenerators used by any project
            List<Node> dependNodes = XPath.select(doc.getDocumentElement(),
                    "project/depend/text()");
            for (Iterator<Node> i = dependNodes.iterator(); i.hasNext();) {
                Node dependNode = i.next();
                String dependPath = dependNode.getNodeValue();
                // Create TargetGenerator to make sure it is cached
                // by TargetGeneratorFactory
                TargetGeneratorFactory.getInstance().createGenerator(
                        ResourceUtil.getFileResourceFromDocroot(dependPath));
            }
        } else {
            // Get TargetGenerator for each project specified
            StringTokenizer tokenizer = new StringTokenizer(projectlist, ",");
            while (tokenizer.hasMoreTokens()) {
                String projectName = tokenizer.nextToken().trim();
                if (projectName.equals("")) {
                    continue;
                }
                Node dependNode = XPath.selectNode(doc.getDocumentElement(),
                        "project[@name='" + projectName + "']/depend/text()");
                if (dependNode == null) {
                    continue;
                }
                String dependPath = dependNode.getNodeValue();
                if (dependPath == null || dependPath.equals("")) {
                    continue;
                }
                // Create TargetGenerator to make sure it is cached
                // by TargetGeneratorFactory
                TargetGeneratorFactory.getInstance().createGenerator(
                        ResourceUtil.getFileResourceFromDocroot(dependPath));
            }
        }
    }
}
