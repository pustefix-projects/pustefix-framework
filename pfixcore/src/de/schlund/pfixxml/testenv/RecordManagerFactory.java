package de.schlund.pfixxml.testenv;


import de.schlund.pfixxml.util.Path;
import java.io.IOException;
import java.util.Hashtable;
import org.xml.sax.SAXException;


/**
 * This factory class is responsible for creating objects
 * of type {@link RecordManager}.
 * <br/>
 * Patterns: Singleton, Factory 
 * 
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class RecordManagerFactory {

    //~ Instance/static variables ..................................................................

    private static RecordManagerFactory instance = new RecordManagerFactory();

    /** Store the created RecordManager objects here */
    private Hashtable hash = new Hashtable();

    //~ Constructors ...............................................................................

    private RecordManagerFactory() {
    }

    //~ Methods ....................................................................................

    /**
     * Get the one and only instance of this singleton
     * @return the instance
     */
    public static RecordManagerFactory getInstance() {
        return instance;
    }

    /**
     * Create a RecordManager.
     * @param the path to the dependency configuration file. For one of 
     * these files, one RecordManager will be created.
     */
    public RecordManager createRecordManager(Path depxml) throws SAXException, IOException  {
        RecordManager ret = null;
        String key = depxml.resolve().getPath();
        if (hash.containsKey(key)) {
            ret = (RecordManager) hash.get(key);
        } else {
            ret = new RecordManager(depxml);
            hash.put(key, ret);
        }
        return ret;
    }
}
