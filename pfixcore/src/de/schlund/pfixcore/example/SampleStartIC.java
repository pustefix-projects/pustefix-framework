package de.schlund.pfixcore.example;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextInterceptor;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.Variant;
import org.apache.log4j.Category;

/**
 * Describe class SampleStartIC here.
 *
 *
 * Created: Fri Apr  8 12:53:30 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class SampleStartIC implements ContextInterceptor {
    Category CAT = Category.getInstance(this.getClass().getName());

    // Implementation of de.schlund.pfixcore.workflow.ContextInterceptor
    /**
     * Describe <code>process</code> method here.
     *
     * @param context a <code>Context</code> value
     * @param pfixServletRequest a <code>PfixServletRequest</code> value
     */
    public final void process(final Context context, final PfixServletRequest pfixServletRequest) {
        RequestParam param = pfixServletRequest.getRequestParam("SETVAR");
        if (param != null && param.getValue() != null && !param.getValue().equals("")) {
            context.setVariant(new Variant(param.getValue()));
        }
    }
}
