package de.schlund.pfixxml.testenv;

import java.util.HashMap;

/**
 * @author jh
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class RecordManagerFactory {
    private static RecordManagerFactory instance = new RecordManagerFactory();
    private HashMap hash = new HashMap();    

    private RecordManagerFactory() {
    }
    
    public static RecordManagerFactory getInstance() {
        return instance;
    }     

    public RecordManager createRecordManager(String depxml) throws Exception {
        RecordManager ret = null;
        if(hash.containsKey(depxml)) {
            ret =  (RecordManager) hash.get(depxml);
        } else {
            ret = new RecordManager(depxml);
            hash.put(depxml, ret);
        }
        return ret;
    }

}
