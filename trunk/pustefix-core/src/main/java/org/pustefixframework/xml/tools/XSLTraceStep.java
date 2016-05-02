package org.pustefixframework.xml.tools;

import java.util.List;
import java.util.Map;

/**
 * Represents a single trace step, i.e. a XSL instruction.
 */
public class XSLTraceStep {
    
    public long start;
    public long end;
    
    public long startInsNo;
    public long endInsNo;
    
    public String systemId;
    public int lineNumber;
    public String displayName;
    public Map<String, String> attributes;
    
    public List<XSLTraceStep> children;
    public XSLTraceStep parent;

    public long getTime() {
        return end - start;
    }
    
    public long getInstructionCount() {
        return endInsNo - startInsNo;
    }
}
