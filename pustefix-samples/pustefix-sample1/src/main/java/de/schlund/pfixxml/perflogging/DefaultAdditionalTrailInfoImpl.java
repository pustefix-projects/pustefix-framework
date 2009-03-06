package de.schlund.pfixxml.perflogging;

import java.util.LinkedHashMap;

import org.pustefixframework.http.AbstractPustefixXMLRequestHandler;

import de.schlund.pfixxml.PfixServletRequest;

public class DefaultAdditionalTrailInfoImpl implements AdditionalTrailInfo {

    public LinkedHashMap<String,Object> getData(PfixServletRequest preq) {
        Object pptime    = preq.getRequest().getAttribute(AbstractPustefixXMLRequestHandler.PREPROCTIME);
        Object doctime   = preq.getRequest().getAttribute(AbstractPustefixXMLRequestHandler.GETDOMTIME);
        Object trafotime = preq.getRequest().getAttribute(AbstractPustefixXMLRequestHandler.TRAFOTIME);
        
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
