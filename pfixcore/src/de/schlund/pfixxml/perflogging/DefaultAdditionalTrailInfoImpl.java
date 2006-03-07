package de.schlund.pfixxml.perflogging;

import de.schlund.pfixxml.AbstractXMLServer;
import de.schlund.pfixxml.PfixServletRequest;
import java.util.LinkedHashMap;

public class DefaultAdditionalTrailInfoImpl implements AdditionalTrailInfo {

    public LinkedHashMap<String,Object> getData(PfixServletRequest preq) {
        Object pptime    = preq.getRequest().getAttribute(AbstractXMLServer.PREPROCTIME);
        Object doctime   = preq.getRequest().getAttribute(AbstractXMLServer.GETDOMTIME);
        Object trafotime = preq.getRequest().getAttribute(AbstractXMLServer.TRAFOTIME);
        
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
