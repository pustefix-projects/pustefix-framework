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
 */
package de.schlund.pfixxml.config.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.schlund.pfixxml.config.PageAccessConfig;

/**
 * @author mleidig@schlund.de
 */
public class PageAccessConfigImpl implements PageAccessConfig {

    private String names;
    private Set<String> pageNames;
    private List<Pattern> pagePatterns;
    
    public PageAccessConfigImpl() {
        pageNames=new HashSet<String>();
        pagePatterns=new ArrayList<Pattern>();
    }
    
    public void setNames(String names) {
        this.names=names;
        String[] values=names.split("\\s*[\\s,]\\s*");
        for(String str:values) {
            if(!str.equals("")) {
                if(str.matches("\\w+")) {
                    pageNames.add(str);
                } else {
                    try {
                        Pattern pat=Pattern.compile(str);
                        pagePatterns.add(pat);
                    } catch(PatternSyntaxException syntaxEx) {
                        //check if simple glob pattern and convert to regexp
                        String convStr=str.replace("?",".");
                        convStr=convStr.replace("*",".*");
                        try {
                            Pattern pat=Pattern.compile(convStr);
                            pagePatterns.add(pat);
                        } catch(PatternSyntaxException x) {
                            throw new IllegalArgumentException("Name is neither a valid pagename nor a valid pattern: "+str,x);
                        }
                    }
                }
            }
        }
    }
    
    public String getNames() {
        return names;
    }
    
    public boolean containsPage(String pageName) {
        if(pageNames.contains(pageName)) return true;
        for(Pattern pattern:pagePatterns) {
            if(pattern.matcher(pageName).matches()) return true;
        }
        return false;
    }  
    
}
