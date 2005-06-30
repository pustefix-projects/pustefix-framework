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
        System.out.println("Lucefixengine INIT WIRD HIER GEMACHT!");
        PfixReadjustment pr = PfixReadjustment.getInstance(p);
        PfixQueueManager pq = PfixQueueManager.getInstance(p);
        Thread readjustT = new Thread(pr);
        Thread queueT = new Thread(pq);
        
        EditorProductFactory fac = EditorProductFactory.getInstance();
        System.out.println("b" + fac.hashCode());
        try {
            EditorProduct[] asdf = fac.getAllEditorProducts();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        queueT.start();
        readjustT.start();
    }
    
    public static PfixEngine getInstance(){
        return _instance;
    }

}
