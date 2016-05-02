package org.pustefixframework.pfxinternals;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.pustefixframework.xml.tools.XSLInfo;
import org.pustefixframework.xml.tools.XSLInfoFactory;
import org.pustefixframework.xml.tools.XSLInfoParsingException;
import org.pustefixframework.xml.tools.XSLTemplateInfo;
import org.w3c.dom.Element;

import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.AuxDependencyFile;
import de.schlund.pfixxml.targets.AuxDependencyImage;
import de.schlund.pfixxml.targets.AuxDependencyInclude;
import de.schlund.pfixxml.targets.AuxDependencyTarget;
import de.schlund.pfixxml.targets.LeafTarget;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetGenerationException;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.TargetImpl;
import de.schlund.pfixxml.targets.VirtualTarget;

public class TargetsCategory implements Category {

    private XSLInfoFactory xslInfoFactory = new XSLInfoFactory();
    
    public void model(Element parent, HttpServletRequest request, PageContext context) {
        
        TargetGenerator targetGenerator = context.getApplicationContext().getBean(TargetGenerator.class);
        
        Element targetsElem = parent.getOwnerDocument().createElement("targets");
        parent.appendChild(targetsElem);
        addTargetList(targetsElem, targetGenerator);
        
        String targetKey = request.getParameter("target");
        if(targetKey == null) {
            targetKey = "metatags.xsl";
        }
        if(!targetGenerator.getAllTargets().containsKey(targetKey)) {
            if(targetGenerator.getAllTargets().size() > 0) {
                targetKey = targetGenerator.getAllTargets().firstKey();
            } else {
                targetKey = null;
            }
        }
        Target target = null;
        if(targetKey != null) {
            target = (TargetImpl)targetGenerator.getTarget(targetKey);
            try {
                target.getValue();
            } catch(TargetGenerationException x) {
                //ignore as we can still provide the static target information
            }
        }
        dumpTarget(target, targetsElem, new HashSet<String>(), true, targetGenerator);
        
    }
    
    private void addTargetList(Element root, TargetGenerator targetGenerator) {
        Element targetsElem = root.getOwnerDocument().createElement("targetlist");
        root.appendChild(targetsElem);
        Map<String, Target> targets = targetGenerator.getAllTargets();
        Iterator<String> it = targets.keySet().iterator();
        while(it.hasNext()) {
            String key = it.next();
            Target target = targets.get(key);
            Element targetElem = root.getOwnerDocument().createElement("target");
            targetElem.setAttribute("key", target.getTargetKey());
            targetsElem.appendChild(targetElem);
        }
    }
    
    private void dumpTarget(Target target, Element root, Set<String> templates, boolean templateInfo, TargetGenerator targetGenerator) {
        
        Element targetElem = root.getOwnerDocument().createElement("target");
        root.appendChild(targetElem);
        targetElem.setAttribute("key", target.getTargetKey());
        targetElem.setAttribute("type", target.getType().getTag());
        if(target instanceof LeafTarget) {
            targetElem.setAttribute("resource", target.getTargetKey());
        } else {
            targetElem.setAttribute("resource", targetGenerator.getDisccachedir() + "/" + target.getTargetKey());
        }
        if(target instanceof VirtualTarget) {
            VirtualTarget virtual = (VirtualTarget)target;
            Target xmlTarget = virtual.getXMLSource();
            dumpTarget(xmlTarget, targetElem, templates, false, targetGenerator);
            Target xslTarget = virtual.getXSLSource();
            dumpTarget(xslTarget, targetElem, templates, false, targetGenerator);
        }
        
        if(target.getTargetKey().endsWith(".xsl") && templateInfo) {
            addTemplateInfo(target, targetElem, templates, targetGenerator);
        }
        
        Element depsElem = root.getOwnerDocument().createElement("dependencies");
        targetElem.appendChild(depsElem);
        TreeSet<AuxDependency> deps = targetGenerator.getTargetDependencyRelation().getDependenciesForTarget(target);
        if(deps != null) {
            for(AuxDependency aux: deps) {
                if(aux instanceof AuxDependencyTarget) {
                    AuxDependencyTarget auxDepTarget = (AuxDependencyTarget)aux;
                    Target auxTarget = auxDepTarget.getTarget();
                    dumpTarget(auxTarget, depsElem, templates, templateInfo, targetGenerator);
                } else if(aux instanceof AuxDependencyInclude) {
                    Element incElem = root.getOwnerDocument().createElement("include");
                    depsElem.appendChild(incElem);
                    AuxDependencyInclude auxDepInc = (AuxDependencyInclude)aux;
                    incElem.setAttribute("path",auxDepInc.getPath().toString());
                    incElem.setAttribute("part", auxDepInc.getPart());
                    incElem.setAttribute("theme", auxDepInc.getTheme());
                } else if(aux instanceof AuxDependencyImage) {
                    Element imgElem = root.getOwnerDocument().createElement("image");
                    depsElem.appendChild(imgElem);
                    AuxDependencyImage auxDepImg = (AuxDependencyImage)aux;
                    imgElem.setAttribute("path",auxDepImg.getPath().toString());
                }  else if(aux instanceof AuxDependencyFile) {
                    Element fileElem = root.getOwnerDocument().createElement("file");
                    depsElem.appendChild(fileElem);
                    AuxDependencyFile auxDepFile = (AuxDependencyFile)aux;
                    fileElem.setAttribute("path", auxDepFile.getPath().toString());
                } 
            }
        } 
    }
    
    private void addTemplateInfo(Target target, Element root, Set<String> templates, TargetGenerator targetGenerator) {
        
        Resource res;
        if(target instanceof LeafTarget) {
            res = ResourceUtil.getResource(target.getTargetKey());
        } else {
            res = ResourceUtil.getFileResource(targetGenerator.getDisccachedir(), target.getTargetKey());
        }
        if(res.exists()) {
            root.setAttribute("url", res.toURI().toASCIIString());
            if(!templates.contains(target.getTargetKey())) {
                try {
                    XSLInfo info = xslInfoFactory.getXSLInfo(res);
                    Element templatesElem = root.getOwnerDocument().createElement("templates");
                    templatesElem.setAttribute("url", res.toURI().toASCIIString());
                    templatesElem.setAttribute("targetKey", target.getTargetKey());
                    root.getOwnerDocument().getDocumentElement().getElementsByTagName("targets").item(0).appendChild(templatesElem);
                    for(String include: info.getIncludes()) {
                        Element includeElem = root.getOwnerDocument().createElement("include");
                        templatesElem.appendChild(includeElem);
                        includeElem.setAttribute("href", include);
                        
                    }
                    for(String imp: info.getImports()) {
                        Element importElem = root.getOwnerDocument().createElement("import");
                        templatesElem.appendChild(importElem);
                        importElem.setAttribute("href", imp);
                    }
                    for(XSLTemplateInfo xi: info.getTemplates()) {
                        Element templateElem = root.getOwnerDocument().createElement("template");
                        templatesElem.appendChild(templateElem);
                        if(xi.getName() != null && !xi.getName().equals("")) templateElem.setAttribute("name", xi.getName());
                        if(xi.getMatch() != null && !xi.getMatch().equals("")) templateElem.setAttribute("match", xi.getMatch());
                    }
                } catch(XSLInfoParsingException x) {
                    x.printStackTrace();
                }
                templates.add(target.getTargetKey());
            }
            
        }
        
    }
    
}
