package de.schlund.pfixxml.xpath;

import java.util.Properties;

import org.apache.log4j.Category;

import org.w3c.dom.NodeList;

import com.icl.saxon.expr.NodeSetExtent;

import de.schlund.pfixxml.XMLException;
import de.schlund.util.FactoryInit;

/** 
 * <b>PFXPathEvaluatorFactory</b> realises the singleton and the factory pattern. Its purpose is
 * to encapsulate all XPath-expression used by the PUSTEFIX system. It can
 * be configured like all factories implementing the {@link FactoryInit} interface.
 * This factory returns the desired implementation for evaluating XPath expressions. 
 * @author <a href="mailto:haecker@schlund.de">Joerg Haecker</a>
 */
public class PFXPathEvaluatorFactory implements FactoryInit {
    private static Category CAT = Category.getInstance(PFXPathEvaluatorFactory.class.getName());
    private boolean use_saxon = true;
    private boolean use_xalan = false;

    private static PFXPathEvaluatorFactory instance = new PFXPathEvaluatorFactory();
    private static String KEY = "xpathexpression.processor";
    private static String VALUE_SAXON = "saxon";
    private static String VALUE_XALAN = "xalan";


    public static String XALAN = VALUE_XALAN;
    public static String SAXON = VALUE_SAXON;    
    
    /**
     * @see de.schlund.util.FactoryInit#init(Properties)
     */
    public void init(Properties props) throws Exception {
        String value= (String) props.get(KEY);
        if(value !=null) {
            if (value.equals(VALUE_SAXON)) {
                use_saxon = true;
                if(CAT.isDebugEnabled()) 
                    CAT.debug("using "+value); 
            }
            else if (value.equals(VALUE_XALAN)) {
                use_xalan = true;
                if(CAT.isDebugEnabled()) 
                    CAT.debug(" using "+value);
            }
            else {
                StringBuffer buf = new StringBuffer();
                buf.append("Unkown value ").append(value).append(" for key ").append(KEY);
                CAT.error(buf.toString());
                throw new XMLException(buf.toString());
            }
        } else {
            StringBuffer buf = new StringBuffer();
            buf.append("Need property ").append(KEY);
            CAT.error(buf.toString());
            throw new XMLException(buf.toString());
        }
    }
    
    public static PFXPathEvaluatorFactory getInstance() {
        return instance;
    }

    public PFXPathEvaluator getXPathEvaluator() {
        if(use_saxon)
            return new PFXPathEvaluatorImplSaxon();        
        else if(use_xalan)
            return new PFXPathEvaluatorImplXalan();
        else
            return null;
    }
    
    public PFXPathEvaluator getXPathEvaluator(String key) {
        if(key.equals(SAXON)) {
            use_saxon = true;
            use_xalan = false;
            return new PFXPathEvaluatorImplSaxon();
        }
        else if(key.equals(XALAN)) {
            use_xalan = true;
            use_saxon = false;
            return new PFXPathEvaluatorImplXalan();
        }
        else
            return null;
    }
    
    
    public NodeList getEmptyNodeList() {
        if(use_saxon) {
            NodeList list = new NodeSetExtent(null);
            return list;
        } else if(use_xalan) {
           // NodeList list = new NodeSet();
            //            return list;
            throw new RuntimeException("Not implemented");
        }
        else
            return null;        
    }
    

}
