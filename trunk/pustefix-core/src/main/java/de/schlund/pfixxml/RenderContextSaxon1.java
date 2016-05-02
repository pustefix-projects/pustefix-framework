package de.schlund.pfixxml;

import com.icl.saxon.output.Outputter;

public class RenderContextSaxon1 extends RenderContext {

    private Outputter outputter;
    
    public void setOutputter(Outputter outputter) {
        this.outputter = outputter;
    }
    
    public Outputter getOutputter() {
        return outputter;
    }
    
}
