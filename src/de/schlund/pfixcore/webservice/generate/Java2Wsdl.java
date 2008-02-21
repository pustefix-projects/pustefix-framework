package de.schlund.pfixcore.webservice.generate;

import java.io.File;
import java.util.HashMap;

import org.apache.axis.wsdl.fromJava.Emitter;

public class Java2Wsdl {
    
    //private String namespace = "";
    private HashMap<String, String> namespaceMap = new HashMap<String, String>();
    private String location = "";
    private String output = "." ;
    private String className = "." ;
    //private String typeMappingVersion = TypeMappingVersionEnum.DEFAULT_VERSION;
    private String style="rpc";
    private String use="encoded";
    private String implClassName;
    
    public void generate() throws Exception {
        try {
            if(className==null || className.length() ==0) throw new Exception("No classname was specified");
            if(location==null || location.length() == 0) throw new Exception("No location was specified");
            Emitter emitter = new Emitter();
            if (!namespaceMap.isEmpty()) {
                emitter.setNamespaceMap(namespaceMap);
            }
            emitter.setCls(className);
            //emitter.setDefaultTypeMapping(DefaultSOAPEncodingTypeMappingImpl.create());
            //emitter.setIntfNamespace(namespace);
            emitter.setImplCls(implClassName);
            //emitter.setImplNamespace(namespaceImpl); 
            emitter.setStyle(style);
            emitter.setUse(use);
            emitter.setLocationUrl(location);
            emitter.emit(output,Emitter.MODE_ALL);
        } catch(Exception x) {
            throw new Exception("WSDL generation error",x);
        }
    }
  
    public void setOutput(File parameter) {
        this.output = parameter.getPath();
    }
   
    public void setLocation(String parameter) {
        this.location = parameter;
    }

    public void setClassName(String parameter) {
        this.className = parameter;
    }
    
    /**
    public void setNamespace(String parameter) {
        this.namespace = parameter;
    }
    */
    
    public void addNamespaceMapping(String pkg,String ns) {
        namespaceMap.put(pkg,ns);
    }

    public void setUse(String use) {
        this.use=use;
    }
    
    public void setStyle(String style) {
    	this.style=style;
    }
    
    public void setImplClassName(String implClassName) {
        this.implClassName=implClassName;
    }
    
    
}


