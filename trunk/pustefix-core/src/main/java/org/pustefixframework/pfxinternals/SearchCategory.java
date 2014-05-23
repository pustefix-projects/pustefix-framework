package org.pustefixframework.pfxinternals;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.pustefixframework.pfxinternals.search.FullTextSearch;
import org.w3c.dom.Element;

import de.schlund.pfixcore.util.ModuleInfo;

public class SearchCategory implements Category {

    @Override
    public void model(Element parent, HttpServletRequest request, PageContext context) {
        
        Element root = parent.getOwnerDocument().createElement("search");
        parent.appendChild(root);
    
        Element modulesElem = parent.getOwnerDocument().createElement("modules");
        root.appendChild(modulesElem);
        Set<String> modules = ModuleInfo.getInstance().getModules();
        SortedSet<String> sortedModules = new TreeSet<String>();
        sortedModules.addAll(modules);

        for(String module: sortedModules) {
            Element elem = parent.getOwnerDocument().createElement("module");
            elem.setAttribute("name", module);
            modulesElem.appendChild(elem);
        }
        
        String action = request.getParameter("action");
        if("search".equals(action)) {
            doSearch(root, request);
        }
    }
    
    private void doSearch(Element root, HttpServletRequest req) {
        
        boolean paramErrors = false;
        
        //Read file pattern param
        
        String filePattern = req.getParameter("filepattern");
        if(filePattern != null) {
            root.setAttribute("filepattern", filePattern);
        }
            
        String fileRegexPattern = "";
        if(filePattern != null) {
            String[] tmpFilePatterns = filePattern.split(",");
            for(String tmpFilePattern: tmpFilePatterns) {
                tmpFilePattern = tmpFilePattern.trim();
                if(tmpFilePattern.length() > 0) {
                    tmpFilePattern = tmpFilePattern.replace(".", "\\.").replace("+", "\\+").replace("*", ".*").replace("?", ".");
                    fileRegexPattern = fileRegexPattern + (fileRegexPattern.length() == 0 ? "" : "|") + "(" + tmpFilePattern + ")";
                }
            }
        }
        if(fileRegexPattern.length() == 0) {
            root.setAttribute("filepatternerror", "You have to enter one or more file name patterns");
            paramErrors = true;
        }
        
        Pattern fileRegexPatternComp = null;
        try {
            fileRegexPatternComp = Pattern.compile(fileRegexPattern, Pattern.CASE_INSENSITIVE);
        } catch(PatternSyntaxException x) {
            root.setAttribute("filepatternerror", x.getMessage());
            paramErrors = true;
        }
        
        //Read search text params
        
        String textPattern = req.getParameter("textpattern");
        
        boolean textPatternCase = false;
        if("true".equals(req.getParameter("textpatterncase"))) {
            textPatternCase = true;
        }
        
        boolean textPatternRegex = false;
        if("true".equals(req.getParameter("textpatternregex"))) {
            textPatternRegex = true;
        }
        
        if(textPattern != null) {
            root.setAttribute("textpattern", textPattern);
        }
        root.setAttribute("textpatterncase", String.valueOf(textPatternCase));
        root.setAttribute("textpatternregex", String.valueOf(textPatternRegex));
        
        Pattern textRegexPatternComp = null;
        if(textPattern != null && textPattern.length() > 0) {
            if(!textPatternRegex) {
                textPattern = Pattern.quote(textPattern);
            }
            try {
                if(textPatternCase) {
                    textRegexPatternComp = Pattern.compile(textPattern);
                } else {
                    textRegexPatternComp = Pattern.compile(textPattern, Pattern.CASE_INSENSITIVE);
                }
            } catch(PatternSyntaxException x) {
                root.setAttribute("textpatternerror", x.getMessage());
                paramErrors = true;
            }
        }
        
        //Read search scope params
        
        boolean searchWebapp = false;
        if("true".equals(req.getParameter("searchwebapp"))) {
            searchWebapp = true;
        }
        boolean searchModules = false;
        if("true".equals(req.getParameter("searchmodules"))) {
            searchModules = true;
        }
        boolean searchClasspath = false;
        if("true".equals(req.getParameter("searchclasspath"))) {
            searchClasspath = true;
        }
        String searchModule = req.getParameter("searchmodule");
        if("All modules".equals(searchModule)) {
            searchModule = null;
        }
        
        root.setAttribute("searchwebapp", String.valueOf(searchWebapp));
        root.setAttribute("searchmodules", String.valueOf(searchModules));
        root.setAttribute("searchclasspath", String.valueOf(searchClasspath));
        if(searchModule != null) {
            root.setAttribute("searchmodule", searchModule);
        }
        
        
        if(paramErrors) {
            return;
        }
        
        //Search
        
        FullTextSearch search = new FullTextSearch();
        search.search(root, fileRegexPatternComp, textRegexPatternComp, searchWebapp, searchModules, searchModule, searchClasspath);
        
    }
    
}
