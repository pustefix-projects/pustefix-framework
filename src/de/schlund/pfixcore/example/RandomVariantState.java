package de.schlund.pfixcore.example;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.app.DefaultIWrapperState;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.ResultDocument;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;

/**
 * Describe class RandomVariantState here.
 *
 *
 * Created: Wed Mar 23 10:35:30 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class RandomVariantState extends DefaultIWrapperState {
    
    public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {

        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("/home/jtl/out"), "utf8");
        
        RequestParam tmp = preq.getRequestParam("foo");
        if (tmp != null) {
            writer.write("=FOO==>" + tmp.getValue());
        }
        tmp = preq.getRequestParam("bar");
        if (tmp != null) {
            writer.write("=BAR==>" + tmp.getValue());
        }
        writer.flush();
        writer.close();
        
        long key = (System.currentTimeMillis() % 5);
        if (key == 0) {
            context.setVariant(null);
        } else if (key == 1) {
            context.setVariant("foo");
        } else if (key == 2) {
            context.setVariant("foo:bar");
        } else if (key == 3) {
            context.setVariant("baz");
        } else {
            context.setVariant("foo:baz");
        }

        return super.getDocument(context, preq);
    }

}
