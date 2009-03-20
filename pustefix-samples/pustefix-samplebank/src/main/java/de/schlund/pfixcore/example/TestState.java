package de.schlund.pfixcore.example;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.StateImpl;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;

/**
 * Describe class TestState here.
 *
 *
 * Created: Tue Jun 13 12:27:01 2006
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class TestState extends StateImpl {

    @Override
    public final ResultDocument getDocument(final Context context, final PfixServletRequest preq) throws Exception {
        ContextSimpleData csd = (ContextSimpleData) context.getContextResourceManager().
            getResource("de.schlund.pfixcore.example.ContextSimpleData");

        csd.setValue("b_was_called", "true");
        return new ResultDocument();
    }
  
}
