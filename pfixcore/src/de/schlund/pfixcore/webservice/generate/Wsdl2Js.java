/*
 * de.schlund.pfixcore.webservice.generate.Wsdl2Js
 */
package de.schlund.pfixcore.webservice.generate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.schlund.pfixcore.webservice.Constants;
import de.schlund.pfixcore.webservice.generate.js.JsBlock;
import de.schlund.pfixcore.webservice.generate.js.JsClass;
import de.schlund.pfixcore.webservice.generate.js.JsMethod;
import de.schlund.pfixcore.webservice.generate.js.JsParam;
import de.schlund.pfixcore.webservice.generate.js.JsStatement;

/**
 * Wsdl2Js.java 
 * 
 * Created: 30.07.2004
 * 
 * @author mleidig
 */
public class Wsdl2Js {

    private File outputFile;
    private File inputFile;
    
    private HashMap schemaTypes;
    private HashMap typeInfoMap;
    private ArrayList typeInfoList;
    private ArrayList popInfoList;
    
    private static Pattern typePattern=Pattern.compile("(\\w+):(\\w+)");
    private static Pattern arrayTypePattern=Pattern.compile("(\\w+):(\\w+)((\\[\\])+)");
    
    private final static String WSDL4J_15_SCHEMA="javax.wsdl.extensions.schema.Schema";
    private final static String WSDL4J_14_SCHEMA="javax.wsdl.extensions.UnknownExtensibilityElement";
    private static boolean wsdl4jWithSchema;
    
    static {
    	try {
    		Class.forName(WSDL4J_15_SCHEMA);
            wsdl4jWithSchema=true;
        } catch(ClassNotFoundException x) {}
    }
    
    
    private SOAPAddress getSOAPAddress(Port port) {
        Iterator it=port.getExtensibilityElements().iterator();
        while(it.hasNext()) {
            ExtensibilityElement elem=(ExtensibilityElement)it.next();
            if(elem instanceof SOAPAddress) {
                SOAPAddress adr=(SOAPAddress)elem;
                return adr;
            }
        }
        return null;
    }
    
    private SOAPBinding getSOAPBinding(Binding binding) {
        Iterator it=binding.getExtensibilityElements().iterator();
        while(it.hasNext()) {
            ExtensibilityElement elem=(ExtensibilityElement)it.next();
            if(elem instanceof SOAPBinding) {
                SOAPBinding bind=(SOAPBinding)elem;
                return bind;
            }
        }
        return null;
    }
    
    private SOAPBody getSOAPBody(BindingInput binding) {
        Iterator it=binding.getExtensibilityElements().iterator();
        while(it.hasNext()) {
            ExtensibilityElement elem=(ExtensibilityElement)it.next();
            if(elem instanceof SOAPBody) {
                SOAPBody body=(SOAPBody)elem;
                return body;
            }
        }
        return null;
    }
    
    /**
    private SOAPBody getSOAPBody(BindingOutput binding) {
        Iterator it=binding.getExtensibilityElements().iterator();
        while(it.hasNext()) {
            ExtensibilityElement elem=(ExtensibilityElement)it.next();
            if(elem instanceof SOAPBody) {
                SOAPBody body=(SOAPBody)elem;
                return body;
            }
        }
        return null;
    }
    */
    
    public void generate() throws Exception {
        
            WSDLFactory wf=WSDLFactory.newInstance();
            WSDLReader wr=wf.newWSDLReader();
            wr.setFeature("javax.wsdl.verbose",true);
            wr.setFeature("javax.wsdl.importDocuments",true);
            
            if(inputFile==null) throw new Exception("No WSDL input file specified");
            if(outputFile==null) throw new Exception("No JS output file specified");
            if(!inputFile.exists()) throw new Exception("WSDL input file doesn't exist");
            InputSource inSrc=new InputSource(new FileInputStream(inputFile));
            Definition def=wr.readWSDL(null,inSrc);
            NamespaceMap namespaces=new NamespaceMap(def.getNamespaces());
            
            typeInfoMap=new HashMap();
            popInfoList=new ArrayList();
            typeInfoList=new ArrayList();
            
            schemaTypes=getComplexSchemaTypes(def.getTypes());
            
            Iterator srvIt=def.getServices().values().iterator();
            while(srvIt.hasNext()) {
                Service service=(Service)srvIt.next();
                Iterator prtIt=service.getPorts().values().iterator();
                while(prtIt.hasNext()) {
                    Port port=(Port)prtIt.next();
                    String portName=port.getName();
                    
                    SOAPAddress soapAdr=getSOAPAddress(port);
                    if(soapAdr==null) throw new Exception("No soap address binding found for port "+portName);
                    
                    
                    String stubClass=Constants.STUBGEN_DEFAULT_JSNAMESPACE+portName;
                    
                    JsParam[] constParams=new JsParam[] {new JsParam("cbObj")};
                    JsClass jsClass=new JsClass(stubClass,"SOAP_Stub",constParams);
                    JsBlock block=jsClass.getConstructorBody();
                    block.addStatement(new JsStatement("this._cbObj=cbObj"));
                    block.addStatement(new JsStatement("this._setURL(\""+soapAdr.getLocationURI()+"\")"));
                    
                    Binding binding=port.getBinding();
                    SOAPBinding soapBinding=getSOAPBinding(binding);
                    if(soapBinding==null) throw new Exception("No soap binding found for binding "+binding.getQName());
                    String style=soapBinding.getStyle();
                    
                    //PortType portType=binding.getPortType();
                    Iterator bopIt=binding.getBindingOperations().iterator();
                    while(bopIt.hasNext()) {
                        BindingOperation bop=(BindingOperation)bopIt.next();
                        Operation op=bop.getOperation();
                       
                        SOAPBody soapBody=getSOAPBody(bop.getBindingInput());
                        if(soapBody==null) throw new Exception("No soap binding found for operation "+bop.getName());
                        String use=soapBody.getUse();
                        
                        JsMethod jsMethod=new JsMethod(jsClass,op.getName());
                        jsClass.addMethod(jsMethod);
                       
                        //get input params
                        Input input=op.getInput();
                        Message inputMsg=input.getMessage();
                        Iterator partIt=inputMsg.getOrderedParts(op.getParameterOrdering()).iterator();
                        while(partIt.hasNext()) {
                            Part part=(Part)partIt.next();
                            JsParam jsParam=new JsParam(part.getName());
                            jsMethod.addParam(jsParam);     
                        }   
                        
                       
                        
                        JsBlock jsBlock=jsMethod.getBody();
                        jsBlock.addStatement(new JsStatement("var call=this._createCall()"));
                        jsBlock.addStatement(new JsStatement("this._extractCallback(call,arguments,"+jsMethod.getParams().length+")"));
                        jsBlock.addStatement(new JsStatement("call.setEncoding(\""+style+"\",\""+use+"\")"));
                        jsBlock.addStatement(new JsStatement("call.setOperationName(\""+jsMethod.getName()+"\")"));
                        JsParam[] jsParams=jsMethod.getParams();
                        for(int i=0;i<jsParams.length;i++) {
                            Part part=inputMsg.getPart(jsParams[i].getName());
                            QName type=part.getTypeName();
                            String info=createJsTypeInfo(type,namespaces,use);
                            jsBlock.addStatement(new JsStatement("call.addParameter(\""+jsParams[i].getName()+"\","+info+")"));
                        }
                        
                        //get return param (presume only one return param)
                        Output output=op.getOutput();
                        Message outputMsg=output.getMessage();
                        partIt=outputMsg.getParts().values().iterator();
                        while(partIt.hasNext()) {
                            Part part=(Part)partIt.next();
                            QName type=part.getTypeName();
                            String info=createJsTypeInfo(type,namespaces,use);
                            jsBlock.addStatement(new JsStatement("call.setReturnType("+info+")"));
                        }
                        
                        
                        jsBlock.addStatement(new JsStatement("return call.invoke("+jsMethod.getParamList()+")"));
                        
                        
                    }
                    
                    
                    for(int i=0;i<typeInfoList.size();i++) {
                        block.addStatement(new JsStatement("this._typeInfos["+i+"]="+(String)typeInfoList.get(i)));
                    }
                    for(int i=0;i<popInfoList.size();i++) {
                        block.addStatement(new JsStatement((String)popInfoList.get(i)));
                    }
                    
                    jsClass.printCode(new FileOutputStream(outputFile));
                    
                }
                
            }
        
    }
    
    
    private String createJsTypeInfo(QName type,NamespaceMap namespaces,String use) throws Exception {
        String ret=(String)typeInfoMap.get(type);
        if(ret==null) {
            int ind=typeInfoList.size();
            ret="this._typeInfos["+ind+"]";
            typeInfoMap.put(type,ret);
            String info="\"\"";
            typeInfoList.add(info);
            if(type.getNamespaceURI().equals(Constants.XMLNS_XSD)||type.getNamespaceURI().equals(Constants.XMLNS_SOAPENC)||
                    type.getNamespaceURI().equals(Constants.XMLNS_APACHESOAP)) {
                info="new SOAP_TypeInfo("+createJsQName(type)+")";
            } else {
                Element ctypeElem=(Element)schemaTypes.get(type);
                if(ctypeElem!=null) {
                	if(isComplexTypeArray(ctypeElem,namespaces,use)) {
                		String qn=createJsQName(type);
                        ArrayInfo arrayInfo=getArrayInfo(ctypeElem,namespaces,use);
                		String ainf=createJsTypeInfo(arrayInfo.getArrayType(),namespaces,use);
                		String pop=ret+".populate("+ainf+","+arrayInfo.getArrayDim()+")";
                		popInfoList.add(pop);
                		info="new SOAP_ArrayInfo("+qn+")";
                	} else {
                		info="new SOAP_BeanInfo("+createJsQName(type)+")";
                		String pop=ret+".populate(new Array(";
                        MemberInfo[] memberInfos=getMemberInfos(ctypeElem,namespaces,use);
                		for(int i=0;i<memberInfos.length;i++) {
                			QName propType=memberInfos[i].getMemberType();
                            String propInfo=createJsTypeInfo(propType,namespaces,use);
                            pop+="\""+memberInfos[i].getMemberName()+"\","+propInfo;
                            if(i<memberInfos.length-1) pop+=",";
                		}
                		pop+="))";
                		popInfoList.add(pop);
                    }
                } else throw new Exception("Type '"+type+"' not found within WSDL types section.");
            }
            typeInfoList.set(ind,info);
        }
        return ret; 
    }
   
    private String createJsQName(QName name) {
        String nsuri="";
        if(name.getNamespaceURI().equals(Constants.XMLNS_XSD)) {
            nsuri="XML_NS_XSD";
        } else if(name.getNamespaceURI().equals(Constants.XMLNS_SOAPENC)) {
            nsuri="XML_NS_SOAPENC";
        } else nsuri="\""+name.getNamespaceURI()+"\"";
        return "new XML_QName("+nsuri+",\""+name.getLocalPart()+"\")";
    }
    
    public void setInputFile(File inputFile) {
        this.inputFile=inputFile;
    }
    
    public void setOutputFile(File outputFile) {
        this.outputFile=outputFile;
    }
 
    /**
     * Get all complexType elements from WSDL types section
     */
    public HashMap getComplexSchemaTypes(Types wsdlTypes) throws Exception {
        HashMap schemaTypes=new HashMap();
        if(wsdlTypes!=null) {
        	List schemaList=wsdlTypes.getExtensibilityElements();
        	Iterator schemaIt=schemaList.iterator();
        	while(schemaIt.hasNext()) {
        		ExtensibilityElement extElem=(ExtensibilityElement)schemaIt.next();
                String className=extElem.getClass().getName();
                boolean isSchema=wsdl4jWithSchema?className.indexOf("Schema")>-1:className.equals(WSDL4J_14_SCHEMA);
        		if(isSchema) {
        			Method getMeth=extElem.getClass().getMethod("getElement",new Class[0]);
                    Element schemaElem=(Element)getMeth.invoke(extElem,new Object[0]);
        			String targetNS=schemaElem.getAttribute("targetNamespace");
        			NodeList nl=schemaElem.getChildNodes();
        			for(int i=0;i<nl.getLength();i++) {
        				Node n=nl.item(i);
        				if(n.getNodeType()==Node.ELEMENT_NODE
        						&&n.getNamespaceURI().equals(Constants.XMLNS_XSD)&&n.getLocalName().equals("complexType")) {
        					Element ctElem=(Element)n;
        					String name=ctElem.getAttribute("name");
        					QName qname=new QName(targetNS,name);
                            schemaTypes.put(qname,ctElem);
        				}
        			}
                }
            }
        }
        return schemaTypes;
    }
    
    /**
     * Check if complexType represents an array
     */
    private boolean isComplexTypeArray(Element ctElem,NamespaceMap namespaces,String use) throws Exception {
        if(use.equals(Constants.ENCODING_USE_ENCODED)) {
            Element modelElem=getFirstSchemaChild(ctElem);
            if(modelElem!=null&&modelElem.getLocalName().equals("complexContent")) {
                Element restElem=getFirstSchemaChild(modelElem);
                if(restElem!=null&&restElem.getLocalName().equals("restriction")) {
                	String base=restElem.getAttribute("base");
                    String soapencPrefix=namespaces.getPrefix(Constants.XMLNS_SOAPENC);
                    String arrayBase=soapencPrefix+":"+"Array";
                    if(base!=null&&base.equals(arrayBase)) return true;
                } 
            }
        } else if(use.equals(Constants.ENCODING_USE_LITERAL)) {
        	Element modelElem=getFirstSchemaChild(ctElem);
            if(modelElem!=null&&modelElem.getLocalName().equals("sequence")) {
            	Element elem=getFirstSchemaChild(modelElem);
                if(elem!=null&&elem.getLocalName().equals("element")) {
                	String min=elem.getAttribute("minOccurs");
                    String max=elem.getAttribute("maxOccurs");
                    if(min!=null&&max!=null&&min.equals("0")&&max.equals("unbounded")) return true;
                }
            }
        } else throw new Exception("Encoding use '"+use+"' is not supported.");
    	return false;
    }
    
    /**
     * Extract array information from complexType
     */
    private ArrayInfo getArrayInfo(Element ctElem,NamespaceMap namespaces,String use) throws Exception {
    	if(use.equals(Constants.ENCODING_USE_ENCODED)) {
            Element modelElem=getFirstSchemaChild(ctElem);
            if(modelElem!=null&&modelElem.getLocalName().equals("complexContent")) {
                Element restElem=getFirstSchemaChild(modelElem);
                if(restElem!=null&&restElem.getLocalName().equals("restriction")) {
                    String base=restElem.getAttribute("base");
                    String soapencPrefix=namespaces.getPrefix(Constants.XMLNS_SOAPENC);
                    String arrayBase=soapencPrefix+":"+"Array";
                    if(base!=null&&base.equals(arrayBase)) {
                    	Element attrElem=getFirstSchemaChild(restElem);
                        if(attrElem!=null&&attrElem.getLocalName().equals("attribute")) {
                        	String ref=attrElem.getAttribute("ref");
                            String arrayRef=soapencPrefix+":"+"arrayType";
                            if(ref!=null&&ref.equals(arrayRef)) {
                            	String arrayType=attrElem.getAttributeNS(Constants.XMLNS_WSDL,"arrayType");
                                if(arrayType!=null) {
                                    Matcher mat=arrayTypePattern.matcher(arrayType);
                                    if(mat.matches()) {
                                    	String prefix=mat.group(1);
                                        String localName=mat.group(2);
                                        String nsuri=namespaces.getNamespaceURI(prefix);
                                        QName qname=new QName(nsuri,localName);
                                        String dimStr=mat.group(3);
                                        int dim=dimStr.length()/2;
                                        ArrayInfo info=new ArrayInfo(qname,dim);
                                        return info;
                                    } else throw new Exception("Illegal 'arrayType': "+arrayType);
                                } else throw new Exception("Illegal 'arrayType': "+ctElem.getAttribute("name"));
                            } else throw new Exception("Illegal 'ref': "+ctElem.getAttribute("name"));
                        } else throw new Exception("Illegal 'attribute': "+ctElem.getAttribute("name"));
                    } else throw new Exception("Illegal 'base': "+ctElem.getAttribute("name"));
                } else throw new Exception("Illegal 'restriction': "+ctElem.getAttribute("name"));
            } else throw new Exception("Illegal 'complexContent': "+ctElem.getAttribute("name"));
        } else if(use.equals(Constants.ENCODING_USE_LITERAL)) {
            Element modelElem=getFirstSchemaChild(ctElem);
            if(modelElem!=null&&modelElem.getLocalName().equals("sequence")) {
                Element elem=getFirstSchemaChild(modelElem);
                if(elem!=null&&elem.getLocalName().equals("element")) {
                    String arrayType=elem.getAttribute("type");
                    Matcher mat=typePattern.matcher(arrayType);
                    if(mat.matches()) {
                    	String prefix=mat.group(1);
                    	String localName=mat.group(2);
                    	String nsuri=namespaces.getNamespaceURI(prefix);
                        QName qname=new QName(nsuri,localName);
                        Element subCtElem=(Element)schemaTypes.get(qname);
                        if(subCtElem!=null) {
                            if(isComplexTypeArray(subCtElem,namespaces,use)) {
                                int dim=1;
                                QName subArrayType=null;
                                do {
                                    ArrayInfo subInfo=getArrayInfo(subCtElem,namespaces,use);
                                    subArrayType=subInfo.getArrayType();
                                    dim+=subInfo.getArrayDim();
                                    subCtElem=(Element)schemaTypes.get(subArrayType);
                                } while(subCtElem!=null&&isComplexTypeArray(subCtElem,namespaces,use));
                                ArrayInfo info=new ArrayInfo(subArrayType,dim);
                                return info;
                            } else return new ArrayInfo(qname,1);
                        } else return new ArrayInfo(qname,1);
                    } else throw new Exception("Illegal array type: "+arrayType);
                } else throw new Exception("Illegal sequence: "+ctElem.getAttribute("name"));
            } else throw new Exception("Illegal content model: "+ctElem.getAttribute("name"));
        } else throw new Exception("Encoding use '"+use+"' is not supported.");
    }
    
    private MemberInfo[] getMemberInfos(Element ctElem,NamespaceMap namespaces,String use) throws Exception {
        if(use.equals(Constants.ENCODING_USE_ENCODED)||use.equals(Constants.ENCODING_USE_LITERAL)) {
            Element modelElem=getFirstSchemaChild(ctElem);
            if(modelElem!=null) {
            	if(modelElem.getLocalName().equals("sequence")) {
                    return getSequenceMemberInfos(modelElem,namespaces,use);
                } else if(modelElem.getLocalName().equals("complexContent")) {
                    ArrayList memberInfos=new ArrayList();
                    Element extElem=getFirstSchemaChild(modelElem);
                    if(extElem!=null&&extElem.getLocalName().equals("extension")) {
                    	String base=extElem.getAttribute("base");
                        Matcher mat=typePattern.matcher(base);
                        if(mat.matches()) {
                        	String prefix=mat.group(1);
                        	String localName=mat.group(2);
                        	String nsuri=namespaces.getNamespaceURI(prefix);
                        	QName qname=new QName(nsuri,localName);
                            Element baseElem=(Element)schemaTypes.get(qname);
                            MemberInfo[] baseInfos=getMemberInfos(baseElem,namespaces,use);
                            for(int i=0;i<baseInfos.length;i++) memberInfos.add(baseInfos[i]);
                            Element seqElem=getFirstSchemaChild(extElem);
                            if(seqElem!=null&&seqElem.getLocalName().equals("sequence")) {
                                MemberInfo[] extInfos=getSequenceMemberInfos(seqElem,namespaces,use);
                                for(int i=0;i<extInfos.length;i++) memberInfos.add(extInfos[i]);
                                MemberInfo[] infos=new MemberInfo[memberInfos.size()];
                                memberInfos.toArray(infos);
                                return infos;
                            } else throw new Exception("Illegal 'extension' content: "+ctElem.getAttribute("name"));
                        } else throw new Exception("Illegal 'base': "+base);
                    } else throw new Exception("Illegal 'complexContent': "+ctElem.getAttribute("name"));
                } else throw new Exception("Illegal content model: "+ctElem.getAttribute("name"));
            } else throw new Exception("XML Schema child expected below complex type "+ctElem.getAttribute("name"));
        } else if(use.equals(Constants.ENCODING_USE_LITERAL)) {
            
        } else throw new Exception("Encoding use '"+use+"' is not supported.");
        return null;
    }
    
    private MemberInfo[] getSequenceMemberInfos(Element seqElem,NamespaceMap namespaces,String use) throws Exception {
        if(use.equals(Constants.ENCODING_USE_ENCODED)||use.equals(Constants.ENCODING_USE_LITERAL)) {
            ArrayList memberInfos=new ArrayList();
        	Element[] elems=getSchemaChildren(seqElem);
        	for(int i=0;i<elems.length;i++) {
        		Element elem=elems[i];
        		if(elem.getLocalName().equals("element")) {
        			String name=elem.getAttribute("name");
        			String type=elem.getAttribute("type");
        			Matcher mat=typePattern.matcher(type);
        			if(mat.matches()) {
        				String prefix=mat.group(1);
        				String localName=mat.group(2);
        				String nsuri=namespaces.getNamespaceURI(prefix);
        				QName qname=new QName(nsuri,localName);
        				memberInfos.add(new MemberInfo(qname,name));
        			}
        		} else throw new Exception("Illegal sequence child: "+elem.getAttribute("name"));
        	}
        	MemberInfo[] infos=new MemberInfo[memberInfos.size()];
        	memberInfos.toArray(infos);
        	return infos;
        } else throw new Exception("Encoding use '"+use+"' is not supported.");
    }
    
    private Element getFirstSchemaChild(Element parent) {
        NodeList nl=parent.getChildNodes();
        for(int i=0;i<nl.getLength();i++) {
        	Node n=nl.item(i);
            if(n.getNodeType()==Node.ELEMENT_NODE&&n.getNamespaceURI().equals(Constants.XMLNS_XSD)) {
            	return (Element)n;
            }
        }
        return null;
    }
    
    private Element[] getSchemaChildren(Element parent) {
        ArrayList al=new ArrayList();
        NodeList nl=parent.getChildNodes();
        for(int i=0;i<nl.getLength();i++) {
            Node n=nl.item(i);
            if(n.getNodeType()==Node.ELEMENT_NODE&&n.getNamespaceURI().equals(Constants.XMLNS_XSD)) {
            	al.add(n);
            }
        }
        Element[] elems=new Element[al.size()];
        al.toArray(elems);
        return elems;
    }
    
    
    class MemberInfo {
     
        QName memberType;
        String memberName;
        
        MemberInfo(QName memberType,String memberName) {
        	this.memberType=memberType;
            this.memberName=memberName;
        }
        
        QName getMemberType() {
        	return memberType;
        }
        
        String getMemberName() {
        	return memberName;
        }
        
    }
    
    class ArrayInfo {
     
        QName arrayType;
        int arrayDim;
        
        ArrayInfo(QName arrayType,int arrayDim) {
        	this.arrayType=arrayType;
            this.arrayDim=arrayDim;
        }
        
        QName getArrayType() {
        	return arrayType;
        }
        
        int getArrayDim() {
        	return arrayDim;
        }
        
    }
    
    class NamespaceMap {
     
        Map namespaces;
        Map prefixes;
        
        NamespaceMap(Map namespaces) {
            this.namespaces=namespaces;
            prefixes=new HashMap();
            Iterator it=namespaces.keySet().iterator();
            while(it.hasNext())  {
                String prefix=(String)it.next();
                String nsuri=(String)namespaces.get(prefix);
                prefixes.put(nsuri,prefix);
            }
        }
        
        /**
         * Returns the namespace uri for a prefix
         */
        String getNamespaceURI(String prefix) {
        	return (String)namespaces.get(prefix);
        }
        
        /**
         * Returns a prefix for the namespace uri
         * (for multiple prefixes with the same namespace uri it's undefined which one you get)
         */
        String getPrefix(String nsuri) {
        	return (String)prefixes.get(nsuri);
        }
        
    }
    
}
