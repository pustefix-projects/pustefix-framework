//### CONSTANTS ###
var XMLNS_XSD="http://www.w3.org/1999/XMLSchema";
var XMLNS_XSI="http://www.w3.org/2001/XMLSchema-instance";
var XMLNS_SOAPENV="http://schemas.xmlsoap.org/soap/envelope/";
var XMLNS_PREFIX_MAP=new Array();
XMLNS_PREFIX_MAP[XMLNS_XSD]="xsd";
XMLNS_PREFIX_MAP[XMLNS_XSI]="xsi";
XMLNS_PREFIX_MAP[XMLNS_SOAPENV]="soapenv";


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


//### CONSTANTS ###
var QNAME_XSI_TYPE=new QName(XMLNS_XSI,"type");


//*********************************
//XMLType
//*********************************
function XMLType() {
	this.XSD_INT=new QName(XMLNS_XSD,"int");
	this.XSD_STRING=new QName(XMLNS_XSD,"string");
}

var xmltype=new XMLType();

//*********************************
//Parameter(name,xmlType,parameterMode)
//Parameter(name,xmlType,jsType,parameterMode)
//*********************************
function Parameter() {
	this.name=null;
	this.xmlType=null;
	this.jsType=null;
	this.parameterMode=null;
	this.init(arguments);
	this.value=null;
}

Parameter.prototype.init=function(args) {
	if(args.length==3) {
		this.name=args[0];
		this.xmlType=args[1];
		this.parameterMode=args[2];
	} else if(args.length==4) {
		this.name=args[0];
		this.xmlType=args[1];
		this.jsType=args[2];
		this.parameterMode=args[3];
	}
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
//SimpleTypeSerializer(type)
//*********************************
function SimpleTypeSerializer(type) {
	this.type=null;
	this.init(arguments);
}

SimpleTypeSerializer.prototype.init=function(args) {
	if(args.length==1) {
		this.type=args[0];
	} else throw "Wrong argument number";
}

//serialize(value,name,writer)
SimpleTypeSerializer.prototype.serialize=function(value,name,writer) {
	writer.startElement(name);
	var prefix=writer.getPrefix(this.type.namespaceUri);
	writer.writeAttribute(QNAME_XSI_TYPE,prefix+":"+this.type.localpart);
	writer.writeChars(value);
	writer.endElement(name);
}


//*********************************
// RPCSerializer(QName opName,ArrayOfParameter params,values,...)
//*********************************
function RPCSerializer(opName,params) {
	this.opName=opName;
	this.params=params;
}

RPCSerializer.prototype.serialize=function(writer) {
	var ser=new SimpleTypeSerializer(xmltype.XSD_INT);
	for(var i=0;i<this.params.length;i++) {
		ser.serialize(this.params[i].value,this.params[i].name,writer);
	}
}


//*********************************
// Call()
//*********************************
function Call() {
	this.endpoint=null;
	this.opName=null;
	this.params=new Array();
	this.retXmlType=null;
	this.retJsType=null;
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

//addParameter(paramName,xmlType,parameterMode)
//addParameter(paramName,xmlType,jsType,parameterMode)
Call.prototype.addParameter=function() {
	var param;
	if(arguments.length==3) {
		param=new Parameter(arguments[0],arguments[1],arguments[2]);
	} else if(arguments.length==4) {
		param=new Parameter(arguments[0],arguments[1],arguments[2],arguments[3]);
	}
	if(param!=null) this.params.push(param);
}

//setReturnType(xmlType)
//setReturnType(xmlType,jsType)
Call.prototype.setReturnType=function() {
	if(arguments.length==1) {
		this.retXmlType=arguments[0];
	} else if(arguments.length==2) {
		this.retXmlType=arguments[0];
		this.retJsType=arguments[1];
	}
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
	if(this.params.length!=arguments.length-ind) throw "Wrong number of arguments";
	for(var i=0;i<this.params.length;i++) {
		this.params[i].setValue(arguments[i+ind]);
	}
	var rpc=new RPCSerializer(this.opName,this.params);
	
	var bodyElem=new SOAPBodyElement(rpc);
	soapMsg.getSOAPPart().getEnvelope().getBody().addBodyElement(bodyElem);
	soapMsg.write(writer);
	document.getElementById('request').value=writer.xml;
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


function test() {
	
	var call=new Call();
	call.setTargetEndpointAddress("http://webservice.zap.ue.schlund.de/xml/webservice/Calculator");
	call.setOperationName(new QName("add"));
	call.addParameter("value1",xmltype.XSD_INT,"IN");
	call.addParameter("value2",xmltype.XSD_INT,"IN");
	call.setReturnType(xmltype.XSD_INT);
	//try {
		call.invoke(3,4);
	//} catch(exception) {
	//	alert(exception);
	//}
	
}