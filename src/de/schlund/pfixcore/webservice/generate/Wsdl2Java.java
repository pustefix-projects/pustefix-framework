package de.schlund.pfixcore.webservice.generate;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.axis.tools.ant.wsdl.TypeMappingVersionEnum;
import org.apache.axis.wsdl.toJava.Emitter;
import org.apache.tools.ant.BuildException;

import de.schlund.pfixcore.webservice.Constants;


public class Wsdl2Java {
    
    private boolean verbose = false;
    private boolean debug = false;
    private boolean server = false;
    private boolean skeletonDeploy = false;
    private boolean testCase = false;
    private boolean noImports = false;
    private boolean all = false;
    private boolean helperGen = false;
    private String factory = null;
    private HashMap namespaceMap = new HashMap();
    private String output = "." ;
    private String deployScope = "";
    private String url = "";
    private String typeMappingVersion = TypeMappingVersionEnum.DEFAULT_VERSION;
    private long timeout = 45000;
    private File  namespaceMappingFile=null;
    
    //private String pkgName=null;
    
    private final static String SCOPE_11_CLASS="org.apache.axis.enum.Scope";
    private final static String SCOPE_12_CLASS="org.apache.axis.constants.Scope";
    
    public Wsdl2Java() {
    }

    public void generate() throws Exception {
        try {
            
            if(url==null || url.length()==0) {
                throw new Exception("No url specified");
            }
            if(timeout<-1) {
                throw new Exception("negative timeout supplied");
            }
            File outdir=new File(output);
            if(!outdir.isDirectory() || !outdir.exists()) {
                throw new Exception("output directory is not valid");
            }
            
            // Instantiate the emitter
            Emitter emitter = new Emitter();

            //Set webservice scope
            if(deployScope==null) deployScope=Constants.SERVICE_SCOPE_REQUEST;
            Class scopeClass=null;
            try {
            	scopeClass=Class.forName(SCOPE_12_CLASS);
            } catch(ClassNotFoundException x) {
            	scopeClass=Class.forName(SCOPE_11_CLASS);
            }
            Method getMeth=scopeClass.getMethod("getScope", new Class[] {String.class});
            Object scopeObj=getMeth.invoke(null,new Object[] {deployScope});
            Method setMeth=emitter.getClass().getMethod("setScope",new Class[] {scopeClass});
            setMeth.invoke(emitter,new Object[] {scopeObj});
            
            if (!namespaceMap.isEmpty()) {
                emitter.setNamespaceMap(namespaceMap);
            }
            emitter.setTestCaseWanted(testCase);
            emitter.setHelperWanted(helperGen);
            if (factory != null) {
                emitter.setFactory(factory);
            }
            emitter.setImports(!noImports);
            emitter.setAllWanted(all);
            emitter.setOutputDir(output);
            emitter.setServerSide(server);
            emitter.setSkeletonWanted(skeletonDeploy);
            emitter.setVerbose(verbose);
            emitter.setDebug(debug);
            
           
            
            emitter.setTypeMappingVersion(typeMappingVersion);
	        if (namespaceMappingFile != null) {
	            emitter.setNStoPkg(namespaceMappingFile.toString());
	        }    
            emitter.setTimeout(timeout);
            
            //if(pkgName!=null) emitter.setPackageName(pkgName);
            
            emitter.run(url);
            
        } catch(Exception x) {
            x.printStackTrace();
        }
            
    }

    /**
     *  flag for verbose output; default=false
     *
     *@param  verbose  The new verbose value
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     *  flag for debug output; default=false
     *
     *@param  debug  The new debug value
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     *  emit server-side bindings for web service; default=false
     */
    public void setServerSide(boolean parameter) {
        this.server = parameter;
    }

    /**
     * deploy skeleton (true) or implementation (false) in deploy.wsdd.
     * Default is false.  Assumes server-side="true".
     */
    public void setSkeletonDeploy(boolean parameter) {
        this.skeletonDeploy = parameter;
    }

    /**
     * flag for automatic Junit testcase generation
     * default is false
     */
    public void setTestCase(boolean parameter) {
        this.testCase = parameter;
    }

    /**
     * Turn on/off Helper class generation;
     * default is false
     */
    public void setHelperGen(boolean parameter) {
        this.helperGen = parameter;
    }

    /**
     * name of the Java2WSDLFactory class for
     * extending WSDL generation functions
     */
    public void setFactory(String parameter) {
        this.factory = parameter;
    }

    /**
     * only generate code for the immediate WSDL document,
     * and not imports; default=false;
     */
    public void setNoImports(boolean parameter) {
        this.noImports = parameter;
    }

    /**
     * output directory for emitted files
     */
    public void setOutput(File parameter) throws BuildException {
        try {
            this.output = parameter.getCanonicalPath();
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        }
    }

    /**
     * add scope to deploy.xml: "Application", "Request", "Session"
     * optional;
     */
    public void setDeployScope(String scope) {
        this.deployScope = scope;
    }
/*
    //unused till we can somehow get ant to be case insensitive when handling enums
    public void setDeployScope(DeployScopeEnum scope) {
        this.deployScope = scope.getValue();
    }
*/
    /**
     * URL to fetch and generate WSDL for.
     * Can be remote or a local file.
     */
    public void setURL(String parameter) {
        this.url = parameter;
    }

    /**
     * flag to generate code for all elements, even unreferenced ones
     * default=false;
     */
    public void setAll(boolean parameter) {
        this.all = parameter;
    }

    /**
     *  the default type mapping registry to use. Either 1.1 or 1.2.
     * Default is 1.1
     * @param parameter new version
     */
    public void setTypeMappingVersion(TypeMappingVersionEnum parameter) {
        this.typeMappingVersion = parameter.getValue();
    }

   
 
    /**
     * set the mapping file. This is a properties file of
     * package=namespace order. Optional, default is to look for
     * a file called NStoPkg.properties in the project directory.
     * @param namespaceMappingFile
     */
    public void setNamespaceMappingFile(File namespaceMappingFile) {
        this.namespaceMappingFile = namespaceMappingFile;
    }

    /**
    public void setPackageName(String pkgName) {
        this.pkgName=pkgName;
    }
    */
    
}


