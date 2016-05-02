package org.pustefixframework.pfxinternals;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;

import de.schlund.pfixxml.IncludeSizeParser;
import de.schlund.pfixxml.IncludeSizeParser.IncludeStatistics;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetGenerationException;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.TargetImpl;

public class IncludesCategory implements Category {

    @Override
    public void model(Element parent, HttpServletRequest req, PageContext context) {
       
        TargetGenerator targetGenerator = context.getApplicationContext().getBean(TargetGenerator.class);
        
        Element targetsElem = parent.getOwnerDocument().createElement("targets");
        parent.appendChild(targetsElem);
        addTopLevelTargetList(targetsElem, targetGenerator);
        
        String targetKey = req.getParameter("target");
        if(targetKey != null) {
            Target target = (TargetImpl)targetGenerator.getTarget(targetKey);
            if(target != null) {
                try {
                    target.getValue();
                } catch(TargetGenerationException x) {
                    //ignore as we can still provide the static target information
                }
                FileResource cacheDir = targetGenerator.getDisccachedir();
                try {
                    File xslFile = new File(cacheDir.getFile(), targetKey);
                    if(xslFile.exists()) {
                        IncludeStatistics stats = IncludeSizeParser.parse(xslFile);
                        IncludeSizeParser.SortBy sortBy = null;
                        String sortByParam = req.getParameter("sortby");
                        if(sortByParam != null) {
                            try {
                            sortBy = IncludeSizeParser.SortBy.valueOf(sortByParam.toUpperCase());
                            } catch(IllegalArgumentException x) {
                                //ignore illegal values
                            }
                        }
                        String json = stats.getJSON(sortBy);
                        Element statsElem = parent.getOwnerDocument().createElement("includestatistics");
                        if(sortByParam != null) {
                            statsElem.setAttribute("sortby", sortByParam);
                        }
                        parent.appendChild(statsElem);
                        statsElem.setTextContent(json);
                    }
                } catch(IOException x) {
                    x.printStackTrace();
                }
                Element targetElem = parent.getOwnerDocument().createElement("target");
                targetsElem.appendChild(targetElem);
                targetElem.setAttribute("key", target.getTargetKey());
            }
        }
    }
    
    private void addTopLevelTargetList(Element root, TargetGenerator targetGenerator) {
        Element targetsElem = root.getOwnerDocument().createElement("targetlist");
        root.appendChild(targetsElem);
        Set<Target> targets = targetGenerator.getPageTargetTree().getToplevelTargets();
        Iterator<Target> it = targets.iterator();
        while(it.hasNext()) {
            Target target = it.next();
            Element targetElem = root.getOwnerDocument().createElement("target");
            targetElem.setAttribute("key", target.getTargetKey());
            targetsElem.appendChild(targetElem);
        }
    }
    
}
