package de.schlund.pfixcore.webservice.generate;

import org.apache.axis.encoding.DefaultSOAPEncodingTypeMappingImpl;
import org.apache.axis.wsdl.fromJava.Emitter;

import java.io.File;
import java.util.HashMap;
import org.apache.axis.tools.ant.wsdl.*;

public class Java2Wsdl {
    
    private String namespace = "";
    private HashMap namespaceMap = new HashMap();
    private String location = "";
    private String output = "." ;
    private String className = "." ;
    private String typeMappingVersion = TypeMappingVersionEnum.DEFAULT_VERSION;

    public void generate() throws Exception {
        try {
            if(className==null || className.length() ==0) throw new Exception("No classname was specified");
            if(location==null || location.length() == 0) throw new Exception("No location was specified");
            Emitter emitter = new Emitter();
            if (!namespaceMap.isEmpty()) {
                emitter.setNamespaceMap(namespaceMap);
            }
            emitter.setCls(className);
            emitter.setDefaultTypeMapping(DefaultSOAPEncodingTypeMappingImpl.create());
            emitter.setIntfNamespace(namespace);
            //emitter.setImplNamespace(namespaceImpl); 
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
    
    public void setNamespace(String parameter) {
        this.namespace = parameter;
    }
    
    public void addNamespaceMapping(String pkg,String ns) {
        namespaceMap.put(pkg,ns);
    }

}


