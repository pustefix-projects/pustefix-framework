/**
 * 
 */
package de.schlund.pfixxml.config.impl;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixxml.config.ContextXMLServletConfig;

/**
 * @author jtl
 *
 */
public class ProcessActionWrapperRule extends CheckedRule {
    String type = null;
    
    public ProcessActionWrapperRule(ContextXMLServletConfig config, String type) {
        this.type = type;
    }

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        ProcessActionConfigImpl actionConfig = (ProcessActionConfigImpl) this.getDigester().peek();
        String prefix = attributes.getValue("ref");
        if (type.equals("submit")) {
            actionConfig.addSubmitPrefix(prefix);
        } else if (type.equals("retrieve")) {
            actionConfig.addRetrievePrefix(prefix);
        } else {
            throw new PustefixCoreException("Invalid type for action part: " + type);
        }
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.impl.CheckedRule#wantsAttributes()
     */
    @Override
    protected Map<String, Boolean> wantsAttributes() {
        HashMap<String, Boolean> atts = new HashMap<String, Boolean>();
        atts.put("ref", true);
        return atts;
    }

}
