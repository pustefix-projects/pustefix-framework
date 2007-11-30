package de.schlund.pfixcore.example;

import java.util.HashMap;

import org.w3c.dom.Element;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

/**
 * Describe class ContextSimpleDataImpl here.
 *
 *
 * Created: Tue Jun 13 12:13:57 2006
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class ContextSimpleDataImpl implements ContextSimpleData {

    private HashMap<String, String> data;
    
    // Implementation of de.schlund.pfixcore.workflow.ContextResource

    /**
     * Describe <code>insertStatus</code> method here.
     *
     * @param resultDocument a <code>ResultDocument</code> value
     * @param element an <code>Element</code> value
     * @exception Exception if an error occurs
     */
    public final void insertStatus(final ResultDocument resdoc, final Element element) throws Exception {
        for (String key: data.keySet()) {
            String  value = data.get(key);
            Element sub   = resdoc.createNode(key);
            sub.setAttribute("value" , value);
            element.appendChild(sub);
        }

    }

    /**
     * Describe <code>init</code> method here.
     *
     * @param context a <code>Context</code> value
     * @exception Exception if an error occurs
     */
    public final void init(final Context context) throws Exception {
        data = new HashMap<String, String>();
    }

    public final void reset() {
        data.clear();
    }

    // Implementation of de.schlund.pfixcore.example.ContextSimpleData

    /**
     * Describe <code>getValue</code> method here.
     *
     * @param string a <code>String</code> value
     * @return a <code>String</code> value
     */
    public final String getValue(final String string) {
        return data.get(string);
    }

    /**
     * Describe <code>setValue</code> method here.
     *
     * @param string a <code>String</code> value
     * @param string1 a <code>String</code> value
     */
    public final void setValue(final String key, final String value) {
        data.put(key, value);
    }

}
