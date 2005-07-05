package de.schlund.lucefix.core;

import java.util.Properties;

import com.sun.corba.se.ActivationIDL._InitialNameServiceImplBase;

import de.schlund.pfixcore.editor.EditorProduct;
import de.schlund.pfixcore.editor.EditorProductFactory;
import de.schlund.pfixxml.XMLException;

/**
 * @author schuppi
 * @date Jun 24, 2005
 */
public class PfixEngine {
    private static PfixEngine _instance = new PfixEngine();
    
    private PfixEngine(){}
    
    public void init(Properties p) throws XMLException{
        PfixQueueManager pq = PfixQueueManager.getInstance(p);
        Thread queueT = new Thread(pq);
        PfixReadjustment pr = PfixReadjustment.getInstance(p);
        Thread readjustT = new Thread(pr);
        
        EditorProductFactory fac = EditorProductFactory.getInstance();

        
        queueT.start();
        readjustT.start();
    }
    
    public static PfixEngine getInstance(){
        return _instance;
    }

}
