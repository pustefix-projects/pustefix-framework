package org.pustefixframework.xml.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.pustefixframework.util.javascript.JSUtils;

/**
 * Holds the tracing data of a XSL transformation.S
 */
public class XSLTrace {

    private String id;
    private XSLTraceStep step;
    
    public XSLTrace(String id, XSLTraceStep step) {
        this.id = id;
        this.step = step;
    }
    
    public String getId() {
        return id;
    }
    
    public XSLTraceStep getStep() {
        return step;
    }
    
    public String getJSON(SortBy sortBy) {
        StringBuilder jsonBuilder = new StringBuilder();
        XSLTraceStepComparator comp = null;
        if(sortBy != null) {
            comp = new XSLTraceStepComparator(sortBy);
        }
        getJSON(step, jsonBuilder, comp);
        return jsonBuilder.toString();
    }
    
    private void getJSON(XSLTraceStep step, StringBuilder sb, XSLTraceStepComparator comp) {
        String name = step.displayName;
        if(name == null) {
            name = "n/a";
        }
        sb.append("{\"displayName\":\"").append(JSUtils.escape(name)).append("\",");
        String systemId = step.systemId;
        if(systemId == null) {
            systemId = "n/a";
        }
        if(step.attributes != null) {
            sb.append("\"attributes\":{");
            Iterator<Entry<String, String>> it = step.attributes.entrySet().iterator();
            while(it.hasNext()) {
                Entry<String, String> entry = it.next();
                sb.append("\"").append(JSUtils.escape(entry.getKey())).append("\":");
                sb.append("\"").append(JSUtils.escape(entry.getValue())).append("\"");
                if(it.hasNext()) {
                    sb.append(",");
                }
            }
            sb.append("},");
        }
        sb.append("\"systemId\":\"").append(JSUtils.escape(systemId)).append("\",");
        sb.append("\"lineNumber\":").append(step.lineNumber).append(",");
        sb.append("\"time\":").append(step.end - step.start).append(",");
        sb.append("\"count\":").append(step.endInsNo - step.startInsNo).append(",");
        if(step.children != null) {
            sb.append("\"steps\":[");
            List<XSLTraceStep> list;
            if(comp == null) {
                list = step.children;
            } else {
                list = new ArrayList<XSLTraceStep>();
                list.addAll(step.children);
                Collections.sort(list, comp);
            }
            Iterator<XSLTraceStep> it = list.iterator();
            while(it.hasNext()) {
                XSLTraceStep child = it.next();
                getJSON(child, sb, comp);
                if(it.hasNext()) {
                    sb.append(",");
                }
            }
            sb.append("]");
        }
        sb.append("}");
    }
    
    public static enum SortBy { INSTRUCTION, LOCATION, COUNT, TIME};
    
    private static class XSLTraceStepComparator implements Comparator<XSLTraceStep> {
        
        SortBy sortBy;
        
        XSLTraceStepComparator(SortBy sortBy) {
            this.sortBy = sortBy;
        }
        
        public int compare(XSLTraceStep o1, XSLTraceStep o2) {
            if(sortBy == SortBy.INSTRUCTION) {
                return o1.displayName.compareTo(o2.displayName);
            } else if(sortBy == SortBy.LOCATION) {
                return o1.systemId.compareTo(o2.systemId);
            } else if(sortBy == SortBy.TIME) {
                long s1 = o1.getTime();
                long s2 = o2.getTime();
                if(s1 > s2) {
                    return -1;
                } else if(s1 < s2) {
                    return 1;
                }
                return 0;
            } else if(sortBy == SortBy.COUNT) {
                long s1 = o1.getInstructionCount();
                long s2 = o2.getInstructionCount();
                if(s1 > s2) {
                    return -1;
                } else if(s1 < s2) {
                    return 1;
                }
                return 0;
            }
            return 0;
        }
    }

}
