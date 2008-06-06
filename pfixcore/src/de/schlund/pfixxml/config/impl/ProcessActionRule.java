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

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.config.ContextXMLServletConfig;

public class ProcessActionRule extends CheckedRule {
    public ProcessActionRule(ContextXMLServletConfig config) {}

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        check(namespace, name, attributes);
        PageRequestConfigImpl pageConfig = (PageRequestConfigImpl) this.getDigester().peek();
        ProcessActionConfigImpl actionConfig = new ProcessActionConfigImpl();
        
        String actionname = attributes.getValue("name");
        actionConfig.setName(actionname);
        
        String pageflow = attributes.getValue("pageflow");
        if (pageflow != null && !pageflow.equals("")) actionConfig.setPageflow(pageflow);
        
        String forcestop = attributes.getValue("forcestop");
        if (forcestop != null) {
            if (forcestop.equals("true") || forcestop.equals("false") || forcestop.equals("step")) {
                actionConfig.setForceStop(forcestop);
            } else  {
                throw new SAXException("Value \"" + forcestop +  "\" is no valid value for 'forcestop'!");
            }
        } else {
            actionConfig.setForceStop("false");
        }
        
        String jumptopage = attributes.getValue("jumptopage");
        if (jumptopage != null && !jumptopage.equals("")) actionConfig.setJumpToPage(jumptopage);
        
        String jumptopageflow = attributes.getValue("jumptopageflow");
        if (jumptopageflow != null && !jumptopageflow.equals("")) actionConfig.setJumpToPageflow(jumptopageflow);
        
        pageConfig.addProcessAction(actionname, actionConfig);

        this.getDigester().push(actionConfig);
    }
    
    /* (non-Javadoc)
     * @see org.apache.commons.digester.Rule#end(java.lang.String, java.lang.String)
     */
    
    @Override
    public void end(String namespace, String name) throws Exception {
        this.getDigester().pop();
    }

    protected Map<String, Boolean> wantsAttributes() {
        HashMap<String, Boolean> atts = new HashMap<String, Boolean>();
        atts.put("name", true);
        atts.put("pageflow", false);
        atts.put("jumptopage", false);
        atts.put("jumptopageflow", false);
        atts.put("forcestop", false);
        return atts;
    }
    
}
