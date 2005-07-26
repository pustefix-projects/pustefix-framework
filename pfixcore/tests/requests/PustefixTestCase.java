/*
 * Created on Jul 15, 2005
 *
 */
package ro.schlund.test.pustefix;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import ro.schlund.test.pustefix.mock.MockRequestDataImpl;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.PathFactory;
import de.schlund.util.statuscodes.PartIndex;
import junit.framework.TestCase;
/**
 * @author Dan Dumitrescu
 *
 * Base class for all test cases that require a pustefix environment
 */
public abstract class PustefixTestCase extends TestCase
{
    protected Context context;
    protected Properties contextProperties;
    private String configRelPath;
    protected IWrapper wrapper;
    protected IHandler handler;
    private Map<String, Object[]> wrapperParams;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
	
	//path to the "projects" directory
        PathFactory.getInstance().init(System.getProperty("abs.proj.dir"));
	
	//path to the "config.prop" file from the project that we want to test
        configRelPath = System.getProperty("config.prop.file");
        PartIndex.getInstance().init(new Properties());        
        contextProperties = new Properties();
        contextProperties.load(new FileInputStream(PathFactory.getInstance().createPath(configRelPath).resolve()));
        context = new Context();
        context.init(contextProperties, "context");
        wrapperParams = new HashMap<String, Object[]>();
    }
    
    /**
     * Init the wrapper with the desired prefix
     * 
     * @param prefix
     * @throws Exception
     */
    protected void initWrapper(String prefix) throws Exception {
        wrapper.init(prefix);
    }
    
    /**
     * Add a new parameter value into the wrapper parameter list
     * 
     * @param name  the name of the parameter
     * @param param the value of the parameter
     * @throws Exception
     */
    protected void addWrapperParam(String name, String param) throws Exception
    {
        wrapperParams.put(name, new Object[] {param});
    }
    
    /**
     * Load all parameters into the wrapper
     * 
     * @throws Exception
     */
    protected void reloadWrapperParams() throws Exception
    {
        MockRequestDataImpl request = new MockRequestDataImpl(context);
        String prefix = wrapper.gimmePrefix();
        for (Entry e : wrapperParams.entrySet()) {
            request.registerParam(prefix + "." + e.getKey(), (Object[]) e.getValue());
        }
        wrapper.load(request);        
    }
    
    /**
     * Add a new parameter value into the wrapper parameter list
     * 
     * @param name  the name of the parameter
     * @param params    the values of the parameter
     * @throws Exception
     */
    protected void addWrapperParam(String name, Object[] params) throws Exception
    {
        wrapperParams.put(name, params);
    }
}
