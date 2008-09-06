package de.schlund.pfixcore.example;

import java.util.HashMap;
import java.util.Iterator;

import org.pustefixframework.container.annotations.Inject;

import de.schlund.pfixcore.example.iwrapper.IndexedTest;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

/**
 * Describe class IndexedTestHandler here.
 *
 *
 * Created: Mon Jul 11 14:01:39 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class IndexedTestHandler implements IHandler {
    // Implementation of de.schlund.pfixcore.generator.IHandler

    private ContextAdultInfo cai;
    
    public final void handleSubmittedData(final Context context, final IWrapper wrapper) throws Exception {
        IndexedTest      itest = (IndexedTest) wrapper;
        String[]         keys  = itest.getKeysValue();

        HashMap<String, String> inmap = new HashMap<String, String>();
        for (int i = 0; i < keys.length ; i++) {
            inmap.put(keys[i], itest.getValue(keys[i]));
        }
        cai.setIndexedTest(inmap);
    }

    public final void retrieveCurrentStatus(final Context context, final IWrapper wrapper) throws Exception {
        IndexedTest             itest  = (IndexedTest) wrapper;
        HashMap<String, String> outmap = cai.getIndexedTest();
        
        for (Iterator<String> i = outmap.keySet().iterator(); i.hasNext(); ) {
            String key = i.next();
            if (outmap.get(key) != null) {
                itest.setValue(outmap.get(key), key);
            } else {
                itest.setValue("", key);
            }

        }
    }

    public final boolean prerequisitesMet(final Context context) throws Exception {
        return true;
    }

    public final boolean isActive(final Context context) throws Exception {
        return true;
    }

    public final boolean needsData(final Context context) throws Exception {
        return false;
    }

    @Inject
    public void setContextAdultInfo(ContextAdultInfo cai) {
        this.cai = cai;
    }

}
