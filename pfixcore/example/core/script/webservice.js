//### CONSTANTS ###
var XMLNS_XSD="http://www.w3.org/1999/XMLSchema";
var XMLNS_XSI="http://www.w3.org/2001/XMLSchema-instance";
var XMLNS_PREFIX_MAP=new Array();
XMLNS_PREFIX_MAP[XMLNS_XSD]="xsd";
XMLNS_PREFIX_MAP[XMLNS_XSI]="xsi";


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

//*********************************
//XMLWriter()
//*********************************
function XMLWriter() {
	this.xml=null;
	this.init();
	this.inStart=false;
	this.prefixMap=new Array();
	this.prefixBase="ns";
	this.prefixCnt=0;
}

XMLWriter.prototype.init=function() {
	this.xml="";
}

XMLWriter.prototype.startElement=function(name) {
	this.xml+="<"+name;
	this.inStart=true;
}

XMLWriter.prototype.endElement=function(name) {
	if(this.inStart) {
		this.xml+="/>";
		this.inStart=false;
	} else {
		this.xml+="</"+name+">";
	}
}

//writeAttribute(QName name,String value)
XMLWriter.prototype.writeAttribute=function(name,value) {
	this.xml+=" "+this.getPrefixedName(name)+"=\""+value+"\"";
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
XMLWriter.prototype.writeNamespaceDeclaration=function(nsuri) {
	alert("grr");
}

//getPrefix(String nsuri)
XMLWriter.prototype.getPrefix=function(nsuri) {
	var prefix=this.prefixMap[nsuri];
	if(prefix==null) {
		prefix=XMLNS_PREFIX_MAP[nsuri];
		if(prefix==null) {
			prefix=this.prefixBase+this.prefixCnt;
			this.prefixCnt++;
		}
		this.prefixMap[nsuri]=prefix;
	}
	return prefix;
}

//getPrefixedName(QName name)
XMLWriter.prototype.getPrefixedName=function(name) {
	if(name.namespaceUri==null) { 
		return name.localpart;
	} else {
		var prefix=this.getPrefix(name.namespaceUri);
		if(prefix==null) {
			this.writeNamespaceDeclaration(namespaceUri);
			prefix=this.getPrefix(namespaceUri);
		}
		return prefix+":"+name.localpart;
	}
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
	writer.writeAttribute(QNAME_XSI_TYPE,writer.getPrefixedName(this.type));
	writer.writeChars(value);
	writer.endElement(name);
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

//invoke(param*)
//inovke(operationName,param*])
Call.prototype.invoke=function() {
	var writer=new XMLWriter();
	var ser=new SimpleTypeSerializer(xmltype.XSD_INT);
	var ind=0;
	if(arguments.length>=0) {
		if(arguments[0] instanceof QName) {
			this.setOperationName(arguments[0]);
			ind++;
		}
		if(arguments.length-ind!=this.params.length) throw "Wrong number of arguments"
		for(var i=0;i<this.params.length;i++) {
			//alert(this.params[i].name+" "+arguments[i+ind]);
			ser.serialize(arguments[i+ind],this.params[i].name,writer);
		}
	}
	alert(writer.xml);
}


function test() {
	
	var call=new Call();
	call.setTargetEndpointAddress("http://webservice.zap.ue.schlund.de/xml/webservice/Calculator");
	call.setOperationName(new QName("add"));
	call.addParameter("value1",xmltype.XSD_INT,"IN");
	call.addParameter("value2",xmltype.XSD_INT,"IN");
	call.setReturnType(xmltype.XSD_INT);
	try {
		call.invoke(3,4);
	} catch(exception) {
		alert(exception);
	}
	alert("test");
	
}