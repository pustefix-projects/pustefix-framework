package de.schlund.pfixcore.workflow;

import de.schlund.pfixxml.XMLException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Describe class ContextInterceptorFactory here.
 *
 *
 * Created: Wed Apr 20 12:15:15 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class ContextInterceptorFactory {
    private HashMap                          icmap    = new HashMap();
    private static ContextInterceptorFactory instance = new ContextInterceptorFactory();
    
    /**
     * Creates a new <code>ContextInterceptorFactory</code> instance.
     *
     */
    private ContextInterceptorFactory() {
    }


    public static ContextInterceptorFactory getInstance() {
        return instance;
    }

    public ContextInterceptor getInterceptor(String classname) throws Exception {
        synchronized (icmap) {
            ContextInterceptor ic = (ContextInterceptor) icmap.get(classname);
            if (ic == null) {
                ic = (ContextInterceptor) icmap.get(classname);
                try {
                    Class stateclass = Class.forName(classname);
                    ic = (ContextInterceptor) stateclass.newInstance();
                } catch (InstantiationException e) {
                    throw new XMLException("unable to instantiate class [" + classname + "] :" + e.getMessage());
                } catch (IllegalAccessException e) {
                    throw new XMLException("unable access class [" + classname + "] :" + e.getMessage());
                } catch (ClassNotFoundException e) {
                    throw new XMLException("unable to find class [" + classname + "] :" + e.getMessage());
                } catch (ClassCastException e) {
                    throw new XMLException("class [" + classname + "] does not implement the interface ContextInterceptor :" + e.getMessage());
                }
                icmap.put(classname, ic);
            }
            return ic;
        }
    }

}
