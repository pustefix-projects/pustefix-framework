//### CONSTANTS ###
var XMLNS_XSD="http://www.w3.org/2001/XMLSchema";
var XMLNS_XSI="http://www.w3.org/2001/XMLSchema-instance";
var XMLNS_SOAPENC="http://schemas.xmlsoap.org/soap/encoding/"
var XMLNS_SOAPENV="http://schemas.xmlsoap.org/soap/envelope/";
var XMLNS_PREFIX_MAP=new Array();
XMLNS_PREFIX_MAP[XMLNS_XSD]="xsd";
XMLNS_PREFIX_MAP[XMLNS_XSI]="xsi";
XMLNS_PREFIX_MAP[XMLNS_SOAPENC]="soapenc";
XMLNS_PREFIX_MAP[XMLNS_SOAPENV]="soapenv";

var ERR_WRONGARGS="Wrong number of arguments";


//*********************************
//QName(localpart)
//QName(namespaceUri,localpart)
//QName(namespaceUri,localpart,prefix)
//*********************************
function QName() {
	this.namespaceUri=null;
	this.localpart=null;
	this.prefix=null;
	this.init(arguments);
}

QName.prototype.init=function(args){
	if(args.length==1) {
		this.localpart=args[0];
	}else if(args.length==2) {
		this.namespaceUri=args[0];
		this.localpart=args[1];	
	}else if(args.length==3) {
		this.namespaceUri=args[0];
		this.localpart=args[1];
		this.prefix=args[2];
	}
}

QName.prototype.hashKey=function() {
	return this.namespaceUri+"#"+this.localpart;
}

QName.prototype.toString=function() {
	var name=this.localpart;
	if(this.prefix!=null) name=this.prefix+":"+name;
	if(this.namespaceUri!=null) {
		name+=" xmlns";
		if(this.prefix!=null) name=name+":"+this.prefix;
		name=name+"=\""+this.namespaceUri+"\"";
	}
	return name; 
}

//### CONSTANTS ###
var QNAME_XSI_TYPE=new QName(XMLNS_XSI,"type");
var QNAME_ARRAY=new QName(XMLNS_SOAPENC,"Array");
var QNAME_ARRAY_TYPE=new QName(XMLNS_SOAPENC,"arrayType");

//*********************************
//XMLTypes
//*********************************
function XMLTypes() {

	this.XSD_BASE64=new QName(XMLNS_XSD,"base64Binary");
	this.XSD_BOOLEAN=new QName(XMLNS_XSD,"boolean");
	this.XSD_BYTE=new QName(XMLNS_XSD,"byte");
	this.XSD_DATETIME=new QName(XMLNS_XSD,"dateTime");
	this.XSD_DECIMAL=new QName(XMLNS_XSD,"decimal");
	this.XSD_DOUBLE=new QName(XMLNS_XSD,"double");
	this.XSD_FLOAT=new QName(XMLNS_XSD,"float");
	this.XSD_HEXBINARY=new QName(XMLNS_XSD,"hexBinary");
	this.XSD_INT=new QName(XMLNS_XSD,"int");
	this.XSD_INTEGER=new QName(XMLNS_XSD,"integer");
	this.XSD_LONG=new QName(XMLNS_XSD,"long");
	this.XSD_QNAME=new QName(XMLNS_XSD,"QName");
	this.XSD_SHORT=new QName(XMLNS_XSD,"short");
	this.XSD_STRING=new QName(XMLNS_XSD,"string");
	
	this.SOAP_ARRAY=new QName(XMLNS_SOAPENC,"Array");
	this.SOAP_BASE64=new QName(XMLNS_SOAPENC,"base64");
	this.SOAP_BOOLEAN=new QName(XMLNS_SOAPENC,"boolean");
	this.SOAP_BYTE=new QName(XMLNS_SOAPENC,"byte");
	this.SOAP_DOUBLE=new QName(XMLNS_SOAPENC,"double");
	this.SOAP_FLOAT=new QName(XMLNS_SOAPENC,"float");
	this.SOAP_INT=new QName(XMLNS_SOAPENC,"int");
	this.SOAP_LONG=new QName(XMLNS_SOAPENC,"long");
	this.SOAP_SHORT=new QName(XMLNS_SOAPENC,"short");
	this.SOAP_STRING=new QName(XMLNS_SOAPENC,"string");
	
}

var xmltypes=new XMLTypes();

//*********************************
//JSType
//*********************************
function JSType() {
	
	this.JS_BOOLEAN="boolean";
	this.JS_DATE="date";
	this.JS_FLOAT="float";
	this.JS_INTEGER="integer";
	this.JS_STRING="string";
	
}

var jstype=new JSType();


//*********************************
//Parameter(String name,TypeInfo typeInfo,parameterMode)
//*********************************
function Parameter() {
	this.name=null;
	this.typeInfo=null;
	this.parameterMode=null;
	this.init(arguments);
	this.value=null;
}

Parameter.prototype.init=function(args) {
	if(args.length==3) {
		this.name=args[0];
		this.typeInfo=args[1];
		this.parameterMode=args[2];
	} else throw ERR_WRONGARGS;
}

Parameter.prototype.setValue=function(value) {
	this.value=value;
}


//*********************************
//XMLContext(XMLContext parent)
//*********************************
function XMLContext(parent) {
	this.parent=parent
	this.prefixMap=new Array();
}

//String getPrefix(String nsuri)
XMLContext.prototype.getPrefix=function(nsuri) {
	var prefix=this.prefixMap[nsuri];
	if(prefix==null && this.parent!=null) {
		prefix=this.parent.getPrefix(nsuri);
	}
	return prefix;
}

//setPrefix(String nsuri,String prefix)
XMLContext.prototype.setPrefix=function(nsuri,prefix) {
	this.prefixMap[nsuri]=prefix;
}


//*********************************
//XMLWriter()
//*********************************
function XMLWriter() {
	this.xml=null;
	this.currentCtx=null;
	this.init();
	this.inStart=false;
	this.prefixMap=new Array();
	this.prefixBase="ns";
	this.prefixCnt=0;
}

XMLWriter.prototype.init=function() {
	this.xml="";
}

//startElement(String name)
//startElement(QName name)
XMLWriter.prototype.startElement=function(name) {
	this.currentCtx=new XMLContext(this.currentCtx);
	if(this.inStart) {
		this.xml+=">";
	}
	var tagName=null;
	var prefix=null;
	var nsuri=null;
	var newNS=false;
	if(name instanceof QName) {
		tagName=name.localpart;
		nsuri=name.namespaceUri;
		if(nsuri!=null) {
			prefix=this.currentCtx.getPrefix(nsuri);
			if(prefix==null) {
				prefix=this.getPrefix(nsuri);
				newNS=true;
			}
		}
	} else {
		tagName=name;
	}
	if(prefix!=null) tagName=prefix+":"+tagName;
	this.xml+="<"+tagName;
	if(newNS) this.writeNamespaceDeclaration(nsuri,prefix);
	this.inStart=true;
}

//endElement(String name)
//endElement(QName name)
XMLWriter.prototype.endElement=function(name) {
	if(this.inStart) {
		this.xml+="/>";
	} else {
		var tagName=null;
		var prefix=null;
		var nsuri=null;
		if(name instanceof QName) {
			tagName=name.localpart;
			nsuri=name.namespaceUri;
			if(nsuri!=null) {
				prefix=this.currentCtx.getPrefix(nsuri);
			}
		} else {
			tagName=name;
		}
		if(prefix!=null) tagName=prefix+":"+tagName;
		this.xml+="</"+tagName+">";
	}
	this.inStart=false;
	this.currentCtx=this.currentCtx.parent;
}

//writeAttribute(String name,String value)
//writeAttribute(QName name,String value)
XMLWriter.prototype.writeAttribute=function(name,value) {
	var attrName=null;
	var prefix=null;
	var nsuri=null;
	var newNS=false;
	if(name instanceof QName) {
		attrName=name.localpart;
		nsuri=name.namespaceUri;
		if(nsuri!=null) {
			prefix=this.currentCtx.getPrefix(nsuri);
			if(prefix==null) {
				prefix=this.getPrefix(nsuri);
				newNS=true;
			}
		}
	} else {
		attrName=name;
	}
	if(prefix!=null) attrName=prefix+":"+attrName;
	if(newNS) this.writeNamespaceDeclaration(nsuri,prefix);
	this.xml+=" "+attrName+"=\""+value+"\"";
}

//writeChars(String chars)
XMLWriter.prototype.writeChars=function(chars) {
	if(this.inStart) {
		this.xml+=">";
		this.inStart=false;
	}
	this.xml+=chars;
}

//writeNamespaceDeclaration(String nsuri)
//writeNamespaceDeclaration(String nsuri,String prefix)
XMLWriter.prototype.writeNamespaceDeclaration=function() {
	if(arguments.length==1) {
		var prefix=this.getPrefix(arguments[0]);
		this.writeNamespaceDeclaration(arguments[0],prefix);
	} else if(arguments.length==2) {
		this.xml+=" xmlns:"+arguments[1]+"=\""+arguments[0]+"\"";
		this.currentCtx.setPrefix(arguments[0],arguments[1]);
	}
}

//String getPrefix(String nsuri)
XMLWriter.prototype.getPrefix=function(nsuri) {
	var prefix=this.prefixMap[nsuri];
	if(prefix==null) {
		prefix=XMLNS_PREFIX_MAP[nsuri];
		if(prefix==null) {
			prefix=this.prefixBase+this.prefixCnt;
			this.prefixCnt++;
		}
	}
	this.prefixMap[nsuri]=prefix;
	return prefix;
}


//*********************************
//SimpleTypeSerializer(QName xmlType)
//*********************************
function SimpleTypeSerializer(xmlType) {
	if(arguments.length==1) {
		this.xmlType=xmlType;
	} else throw ERR_WRONGARGS;
}

//serialize(value,name,typeInfo,writer)
SimpleTypeSerializer.prototype.serialize=function(value,name,typeInfo,writer) {
	writer.startElement(name);
	var prefix=writer.getPrefix(typeInfo.xmlType.namespaceUri);
	writer.writeAttribute(QNAME_XSI_TYPE,prefix+":"+typeInfo.xmlType.localpart);
	writer.writeChars(value);
	writer.endElement(name);
}

//deserialize(typeInfo,element)
SimpleTypeSerializer.prototype.deserialize=function(typeInfo,element) {
	return element.firstChild.nodeValue;
}


//*********************************
//ArraySerializer(QName xmlType)
//*********************************
function ArraySerializer(xmlType) {
	if(arguments.length==1) {
		this.xmlType=xmlType;
	} else throw ERR_WRONGARGS;
}

ArraySerializer.prototype.serializeSub=function(value,name,typeInfo,dim,writer) {
	if(dim>0 && value instanceof Array) {
		writer.startElement(name);
		if(dim==typeInfo.dimension) {
			var prefix=writer.getPrefix(QNAME_ARRAY.namespaceUri);
			writer.writeAttribute(QNAME_XSI_TYPE,prefix+":"+QNAME_ARRAY.localpart);
		}
		var dimStr="";
		for(var j=0;j<dim;j++) dimStr+="[]";
		dimStr=dimStr.replace(/\[\]$/,"["+value.length+"]");
		var prefix=writer.getPrefix(typeInfo.arrayType.namespaceUri);
		writer.writeAttribute(QNAME_ARRAY_TYPE,prefix+":"+typeInfo.arrayType.localpart+dimStr);
		for(var i=0;i<value.length;i++) {
			this.serializeSub(value[i],"item",typeInfo,dim-1,writer);
		}
		writer.endElement(name);
	} else {
		writer.startElement(name);
		writer.writeChars(value);
		writer.endElement(name);
	}
}

ArraySerializer.prototype.serialize=function(value,name,typeInfo,writer) {
	this.serializeSub(value,name,typeInfo,typeInfo.dimension,writer);
}


//*********************************
//BeanSerializer(QName type)
//*********************************
function BeanSerializer(type) {
	this.type=type;
}


//*********************************
//TypeMapping()
//*********************************
function TypeMapping() {
	this.mappings=new Array();
	this.builtin=new Array();
	this.SER_SIMPLE=1;
	this.SER_ARRAY=2;
	this.init();
}

//init()
TypeMapping.prototype.init=function() {
	this.builtin[xmltypes.XSD_BOOLEAN.hashKey()]=this.SER_SIMPLE;
	this.builtin[xmltypes.XSD_DATETIME.hashKey()]=this.SER_SIMPLE;
	this.builtin[xmltypes.XSD_FLOAT.hashKey()]=this.SER_SIMPLE;
	this.builtin[xmltypes.XSD_INT.hashKey()]=this.SER_SIMPLE;
	this.builtin[xmltypes.XSD_STRING.hashKey()]=this.SER_SIMPLE;
	this.builtin[xmltypes.SOAP_ARRAY.hashKey()]=this.SER_ARRAY;
}

//register(QName xmlType,Serializer serializer)
TypeMapping.prototype.register=function(xmlType,serializer) {
	this.mappings[xmlType.hashKey()]=serializer;
}

//Serializer getSerializer(TypeInfo typeInfo)
TypeMapping.prototype.getSerializer=function(typeInfo) {
	if(arguments.length==1) {
		var serializer=this.mappings[typeInfo.xmlType.hashKey()];
		if(serializer==null) {
			serializer=this.getBuiltinSerializer(typeInfo);
			if(serializer==null) throw "Can't find serializer for type '"+typeInfo.xmlType.toString()+"'";
			this.register(typeInfo.xmlType,serializer);
		} 
		return serializer;
	} else throw ERR_WRONGARGS;
}

//Serializer getBuiltinSerializer(TypeInfo typeInfo)
TypeMapping.prototype.getBuiltinSerializer=function(typeInfo) {
	var serializer=null;
	var serType=this.builtin[typeInfo.xmlType.hashKey()];
	if(serType==this.SER_SIMPLE) {
		serializer=new SimpleTypeSerializer(typeInfo.xmlType);
	} else if(serType==this.SER_ARRAY) {
		serializer=new ArraySerializer(typeInfo.xmlType);
	} else {
		if(typeInfo instanceof ArrayInfo) serializer=new ArraySerializer(typeInfo.xmlType);
	}
	return serializer;
}

var typeMapping=new TypeMapping();


//*********************************
// RPCSerializer(QName opName,ArrayOfParameter params,values,...)
//*********************************
function RPCSerializer(opName,params,retTypeInfo) {
	this.opName=opName;
	this.params=params;
	this.retTypeInfo=retTypeInfo;
}

RPCSerializer.prototype.serialize=function(writer) {
	writer.startElement(this.opName);
	writer.writeAttribute(new QName(XMLNS_SOAPENV,"encodingStyle"),XMLNS_SOAPENC);
	for(var i=0;i<this.params.length;i++) {
		var serializer=typeMapping.getSerializer(this.params[i].typeInfo);
		serializer.serialize(this.params[i].value,this.params[i].name,this.params[i].typeInfo,writer);
	}
	writer.endElement(this.opName);
}

RPCSerializer.prototype.deserialize=function(element) {
	var serializer=typeMapping.getSerializer(this.retTypeInfo);
	var res=serializer.deserialize(this.retTypeInfo,element.getElementsByTagName(this.opName+"Return")[0]);
	return res;
}


//*********************************
// Call()
//*********************************
function Call() {
	this.endpoint=null;
	this.opName=null;
	this.params=new Array();
	this.retTypeInfo=null;
  this.callback=null;
}

//setTargetEndpointAddress(address)
Call.prototype.setTargetEndpointAddress=function() {
	if(arguments.length==1) {
		this.endpoint=arguments[0];
	}
}

//setOperationName(operationName)
Call.prototype.setOperationName=function() {
	if(arguments.length==1) {
		this.opName=arguments[0];
	}
}	

//addParameter(paramName,typeInfo,parameterMode)
Call.prototype.addParameter=function() {
	if(arguments.length==3) {
		var param=new Parameter(arguments[0],arguments[1],arguments[2]);
		if(param!=null) this.params.push(param);
	} else throw ERR_WRONGARGS;
}

//setReturnType(retTypeInfo)
Call.prototype.setReturnType=function(retTypeInfo) {
	this.retTypeInfo=retTypeInfo;
}

//invoke()
//invoke(values,...)
//invoke(QName operationName,values,...)
Call.prototype.invoke=function() {
	var writer=new XMLWriter();
	var soapMsg=new SOAPMessage();
	
	var ind=0;
	if(arguments.length>0) {
		if(arguments[0] instanceof QName) {
			this.setOperationName(arguments[0]);
			ind++;
		}
	}
	if(this.params.length!=arguments.length-ind) throw ERR_WRONGARGS;
	for(var i=0;i<this.params.length;i++) {
		this.params[i].setValue(arguments[i+ind]);
	}
	var rpc=new RPCSerializer(this.opName,this.params,this.retTypeInfo);
	
	var bodyElem=new SOAPBodyElement(rpc);
	soapMsg.getSOAPPart().getEnvelope().getBody().addBodyElement(bodyElem);
	soapMsg.write(writer);
  //  alert("writer.xml:\n" + writer.xml);
  	document.getElementById('request').value=writer.xml;
  	

	var resDoc=sendTest(writer.xml,this.endpoint);

	
	return rpc.deserialize(resDoc.getElementsByTagNameNS(XMLNS_SOAPENV,"Body")[0]);
	
  //return new xmlRequest( 'POST', this.endpoint, this.callback ).start( writer.xml );

}




//*********************************
// SOAPMessage()
//*********************************
function SOAPMessage() {
	this.soapPart=new SOAPPart();
}

//SOAPPart getSOAPPart()
SOAPMessage.prototype.getSOAPPart=function() {
	return this.soapPart;
}

//write(XMLWriter writer) {
SOAPMessage.prototype.write=function(writer) {
	this.soapPart.write(writer);
}

//*********************************
// SOAPPart()
//*********************************
function SOAPPart() {
	this.envelope=new SOAPEnvelope();
	
}

//SOAPEnvelope getEnvelope()
SOAPPart.prototype.getEnvelope=function() {
	return this.envelope;
}

//write(XMLWriter writer) {
SOAPPart.prototype.write=function(writer) {
	this.envelope.write(writer);
}


//*********************************
// SOAPEnvelope()
//*********************************
function SOAPEnvelope() {
	this.header=new SOAPHeader();
	this.body=new SOAPBody();
}

//write(XMLWriter writer) {
SOAPEnvelope.prototype.write=function(writer) {
	var envName=new QName(XMLNS_SOAPENV,"Envelope");
	writer.startElement(envName);
	writer.writeNamespaceDeclaration(XMLNS_XSI);
	writer.writeNamespaceDeclaration(XMLNS_XSD);
	this.body.write(writer);
	writer.endElement(envName);
}

//SOAPHeader getHeader()
SOAPEnvelope.prototype.getHeader=function() {
	return this.header;
}

//SOAPBody getBody()
SOAPEnvelope.prototype.getBody=function() {
	return this.body;
}

//*********************************
// SOAPBodyElement(serializer)
//*********************************
function SOAPBodyElement(serializer) {
	this.serializer=serializer;
}

SOAPBodyElement.prototype.write=function(writer) {
	this.serializer.serialize(writer);
}

//*********************************
// SOAPBody()
//*********************************
function SOAPBody() {
	this.fault=null;
	this.bodyElems=new Array();
}

//addBodyElement(SOAPBodyElement bodyElem)
SOAPBody.prototype.addBodyElement=function(bodyElem) {
	this.bodyElems.push(bodyElem);	
}

//setFault(SOAPFault fault) 
SOAPBody.prototype.setFault=function(fault) {
	this.fault=fault;
}

//write(XMLWriter writer) {
SOAPBody.prototype.write=function(writer) {
	var bodyName=new QName(XMLNS_SOAPENV,"Body");
	writer.startElement(bodyName);
	if(this.fault!=null) {
		return;
	}
	if(this.bodyElems!=null) {
		for(var i=0;i<this.bodyElems.length;i++) {
			this.bodyElems[i].write(writer);
		}
	}
	writer.endElement(bodyName);
}

//*********************************
// SOAPHeaderElement(serializer)
//*********************************
function SOAPHeaderElement(serializer) {
	this.serializer=serializer;
}

SOAPHeaderElement.prototype.write=function(writer) {
	this.serializer.serialize(writer);
}

//*********************************
// SOAPHeader()
//*********************************
function SOAPHeader() {
	this.headerElems=new Array();
}

SOAPHeader.prototype.addHeaderElement=function(headerElem) {
	this.headerElems.push(headerElem);
}

//write(XMLWriter writer) {
SOAPHeader.prototype.write=function(writer) {
	var headerName=new QName(XMLNS_SOAPENV,"Header");
	writer.startElement(headerName);
	for(var i=0;i<this.headerElems.length;i++) {
		this.headerElems[i].write(writer);
	}
	writer.endElement(headerName);
}

//*********************************
// SOAPFault(String faultCode,String faultString)
//*********************************
function SOAPFault(faultCode,faultString) {
	this.faultCode=faultCode;
	this.faultString=faultString;
}

//*********************************
// TypeInfo(QName xmlType)
//*********************************
function TypeInfo(xmlType) {
	this.xmlType=xmlType;
}

//*********************************
// ArrayInfo(QName xmlType,QName arrayType,Number dimension)
//*********************************
function ArrayInfo(xmlType,arrayType,dimension) {
	this.xmlType=xmlType;
	this.arrayType=arrayType;
	this.dimension=dimension;
}

//*********************************
// BeanInfo(QName xmlType,Array propToInfo)
//*********************************
function BeanInfo(xmlType,arrayType,propToInfo) {
	this.xmlType=xmlType;
	this.arrayType=arrayType;
	this.propToInfo=propToInfo;
}



function test() {
	
	
	var call=new Call();
	call.setTargetEndpointAddress(window.location.protocol + "//" + window.location.host + "/xml/webservice/Calculator");
	//call.setTargetEndpointAddress(window.location.protocol + "//" + window.location.host + "/xml/webservice/TypeTest");
	
	
	call.setOperationName(new QName("add"));
	call.addParameter("val1",new TypeInfo(xmltypes.XSD_INT),"IN");
	call.addParameter("val2",new TypeInfo(xmltypes.XSD_INT),"IN");
	call.setReturnType(new TypeInfo(xmltypes.XSD_INT));
	var res=call.invoke(2,3);
	alert("Result: "+res);
	alert(res+9);
	
	/*
	call.setOperationName(new QName("echoString"));
	call.addParameter("val",new TypeInfo(xmltypes.XSD_STRING),"IN");
	call.setReturnType(new TypeInfo(xmltypes.XSD_STRING));
	var res=call.invoke("testtext");
	alert("Result: "+res);
	*/
	/*
	call.setOperationName(new QName("echoStringArray"));
	var info=new ArrayInfo(new QName("urn:webservices.example.pfixcore.schlund.de","ArrayOf_xsd_string"),xmltypes.XSD_STRING,1);
	call.addParameter("val",info,"IN");
	call.setReturnType(info);
	call.invoke(new Array("testtext","foooo","grrrrr"));
	*/
	
	/*
	call.setOperationName(new QName("echoStringMultiArray"));
	var info=new ArrayInfo(new QName("urn:webservices.example.pfixcore.schlund.de","ArrayOfArrayOf_xsd_string"),xmltypes.XSD_STRING,2);
	call.addParameter("val",info,"IN");
	call.setReturnType(info);
	call.invoke(new Array(new Array("a","b","c"),new Array("d","e")));
	*/
	
}

function sendTest(msg,url) {
	var req;
	if (window.XMLHttpRequest) {
		req=new XMLHttpRequest();
	} else if(window.ActiveXObject) {
      req = new ActiveXObject("Microsoft.XMLHTTP");
	} else {
		alert("XMLHttpRequest not supported");
	}
	req.open("POST", url, false);
	req.setRequestHeader("SOAPAction",'""');
	req.send(msg);
	return req.responseXML;
}
