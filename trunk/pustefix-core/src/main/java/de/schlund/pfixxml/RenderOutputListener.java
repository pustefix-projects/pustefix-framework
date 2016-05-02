package de.schlund.pfixxml;

import java.io.ByteArrayOutputStream;

import de.schlund.pfixcore.workflow.Context;

public interface RenderOutputListener {

    public void output(PfixServletRequest request, Context context, ByteArrayOutputStream output);
    
}
