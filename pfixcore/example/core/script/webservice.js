var xmlns_xsd="http://www.w3.org/1999/XMLSchema";

//*********************************
//QName(localpart)
//QName(namespaceUri,localpart)
//*********************************
function QName() {
	this.namespaceUri=null;
	this.localpart=null;
	this.init(arguments);
}

QName.prototype.init=function(args){
	if(args.length==1) {
		this.localpart=args[0];
	} else if(args.length==2) {
		this.namespaceUri=args[0];
		this.localpart=args[1];	
	}
}

//*********************************
//XMLType
//*********************************
function XMLType() {
	this.XSD_INT=new QName(xmlns_xsd,"int");
	this.XSD_STRING=new QName(xmlns_xsd,"string");
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
//IntSerializer
//*********************************
function IntSerializer() {

}

//serialize(value,name,writer)
IntSerializer.prototype.serialize=function() {

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
	var ind=0;
	if(arguments.length>=0) {
		if(arguments[0] instanceof QName) {
			this.setOperationName(arguments[0]);
			ind++;
		}
		if(arguments.length-ind!=this.params.length) throw "Wrong number of arguments"
		for(var i=0;i<this.params.length;i++) {
			alert(this.params[i].name);
		}
	}
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