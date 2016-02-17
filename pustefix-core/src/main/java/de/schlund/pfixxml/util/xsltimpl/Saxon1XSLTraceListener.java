package de.schlund.pfixxml.util.xsltimpl;

import java.util.ArrayList;
import java.util.HashMap;

import org.pustefixframework.xml.tools.XSLTrace;
import org.pustefixframework.xml.tools.XSLTraceStep;
import org.pustefixframework.xml.tools.XSLTracing;

import com.icl.saxon.Context;
import com.icl.saxon.NodeHandler;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.trace.TraceListener;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.tree.ElementWithAttributes;

/**
 * Measures execution time of XSL instructions.
 */
public class Saxon1XSLTraceListener implements TraceListener {

    private String systemId;
    
    private XSLTraceStep currentStep;
    
    private long granularity;
    private boolean recordAttributes;
    
    private long instructionCount;
    
    public Saxon1XSLTraceListener(String systemId) {
        this.systemId = systemId;
        this.granularity = XSLTracing.getInstance().getInstructionGranularity();
        this.recordAttributes = XSLTracing.getInstance().recordAttributes();
     }
    
    @Override
    public void open() {
        currentStep = new XSLTraceStep();
        currentStep.systemId = systemId;
        currentStep.displayName = "/";
        currentStep.start = System.nanoTime();
        currentStep.startInsNo = 0;
    }
    
    @Override
    public void close() {
        currentStep.end = System.nanoTime();
        currentStep.endInsNo = instructionCount;
        XSLTrace trace = new XSLTrace(systemId, currentStep);
        XSLTracing.getInstance().addTrace(trace);
    }

    @Override
    public void enterSource(NodeHandler arg0, Context arg1) {}
    
    @Override
    public void leaveSource(NodeHandler arg0, Context arg1) {}

    @Override
    public void toplevel(NodeInfo arg0) {}

    @Override
    public void enter(NodeInfo node, Context arg1) {
        
        XSLTraceStep step = new XSLTraceStep();
        step.start = System.nanoTime();
        step.startInsNo = instructionCount;
        step.systemId = node.getSystemId();
        step.lineNumber = node.getLineNumber();
        step.displayName = node.getDisplayName();
        
        if(currentStep != null) {
            step.parent = currentStep;
            if(currentStep.children == null) {
                currentStep.children = new ArrayList<>();
            }
            currentStep.children.add(step);
        }
        currentStep = step;
        instructionCount++;
    }
    
    @Override
    public void leave(NodeInfo node, Context arg1) {
        
        currentStep.end = System.nanoTime();
        currentStep.endInsNo = instructionCount;
        if(currentStep.getTime() < granularity) {
            currentStep.parent.children.remove(currentStep);
        } else {
            if(recordAttributes) {
                if(node instanceof ElementWithAttributes) {
                    currentStep.attributes = new HashMap<String, String>();
                    ElementWithAttributes elem = (ElementWithAttributes)node;
                    AttributeCollection attrs = elem.getAttributeList();
                    for(int i=0; i<attrs.getLength(); i++) {
                        currentStep.attributes.put(attrs.getQName(i), attrs.getValue(i));
                    }
                }
            }
        }
        currentStep = currentStep.parent;
    }

}