package org.pustefixframework.pfxinternals;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.pustefixframework.xml.tools.XSLTracing;
import org.pustefixframework.xml.tools.XSLTrace;
import org.w3c.dom.Element;

/**
 * Controller and model part of the XSL tracing UI.
 */
public class XSLTraceCategory implements Category {

    @Override
    public void model(Element parent, HttpServletRequest req, PageContext context) {
       
        XSLTracing tracing = XSLTracing.getInstance();
        
        if(req.getMethod().equals("POST")) {
            String enabledParam = req.getParameter("enabled");
            boolean enabled = false;
            if(enabledParam != null) {
                enabled = Boolean.valueOf(enabledParam);
                tracing.setEnabled(enabled);
            }
            if(enabled) {
                String sizeParam = req.getParameter("traceCacheSize");
                if(sizeParam != null) {
                    int size = Integer.valueOf(sizeParam);
                    tracing.setTraceCacheSize(size);
                }
                String granularParam = req.getParameter("instructionGranularity");
                if(granularParam != null) {
                    float granular = Float.valueOf(granularParam);
                    tracing.setInstructionGranularity((int)(granular * 1000000));
                }
                boolean record = false;
                String recordParam = req.getParameter("recordAttributes");
                if(recordParam != null) {
                    record = true;
                }
                tracing.setRecordAttributes(record);
            }
            return; //"PRG pattern"
        }
        
        Element tracesElem = parent.getOwnerDocument().createElement("traces");
        parent.appendChild(tracesElem);
        
        tracesElem.setAttribute("enabled", String.valueOf(tracing.isEnabled()));
        float granular = (float)tracing.getInstructionGranularity() / 1000000;
        tracesElem.setAttribute("instructionGranularity", String.valueOf(granular));
        tracesElem.setAttribute("traceCacheSize", String.valueOf(tracing.getTraceCacheSize()));
        tracesElem.setAttribute("recordAttributes", String.valueOf(tracing.recordAttributes()));
        
        Set<String> traces = tracing.getTraceIds();
        Element traceListElem = parent.getOwnerDocument().createElement("tracelist");
        tracesElem.appendChild(traceListElem);
        for(String traceId: traces) {
            Element traceElem = parent.getOwnerDocument().createElement("trace");
            traceElem.setAttribute("id", traceId);
            traceListElem.appendChild(traceElem);
        }
        
        String traceId = req.getParameter("trace");
        if(traceId != null) {
            
            XSLTrace trace = XSLTracing.getInstance().getTrace(traceId);
            if(trace != null) {
                    
                XSLTrace.SortBy sortBy = null;
                String sortByParam = req.getParameter("sortby");
                if(sortByParam != null) {
                    try {
                        sortBy = XSLTrace.SortBy.valueOf(sortByParam.toUpperCase());
                    } catch(IllegalArgumentException x) {
                        //ignore illegal values
                    }
                }
                String json = trace.getJSON(sortBy);
                Element statsElem = parent.getOwnerDocument().createElement("tracestatistics");
                statsElem.setAttribute("id", trace.getId());
                statsElem.setAttribute("time", String.valueOf(trace.getStep().getTime()));
                    
                if(sortByParam != null) {
                    statsElem.setAttribute("sortby", sortByParam);
                }
                parent.appendChild(statsElem);
                statsElem.setTextContent(json);
                        
            }
        }
    }

}
