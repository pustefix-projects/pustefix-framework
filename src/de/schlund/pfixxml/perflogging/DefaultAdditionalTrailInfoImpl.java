package de.schlund.pfixxml.perflogging;

import de.schlund.pfixxml.AbstractXMLServlet;
import de.schlund.pfixxml.PfixServletRequest;
import java.util.LinkedHashMap;

public class DefaultAdditionalTrailInfoImpl implements AdditionalTrailInfo {

    public LinkedHashMap<String,Object> getData(PfixServletRequest preq) {
        Object pptime    = preq.getRequest().getAttribute(AbstractXMLServlet.PREPROCTIME);
        Object doctime   = preq.getRequest().getAttribute(AbstractXMLServlet.GETDOMTIME);
        Object trafotime = preq.getRequest().getAttribute(AbstractXMLServlet.TRAFOTIME);
        
        LinkedHashMap<String,Object> retval = new LinkedHashMap<String,Object>();
        retval.put("GET_DOM", ""+doctime);
        retval.put("HDL_DOC", ""+trafotime);
        retval.put("PRE_PROC", ""+pptime);
        return retval;
    }
    
    public void reset() {
        // Nothing to do in default impl
    }
}
