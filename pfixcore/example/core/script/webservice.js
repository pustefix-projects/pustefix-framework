//Add inheritance support
Function.prototype.extend=function(base) {
	var derived=this.prototype=new base;
	this.prototype.superclass=base.prototype;
	return derived;
};



//==================================================================
//core (Pustefix core classes)
//==================================================================


//*********************************
//coreException(string msg,string src)
//*********************************
function coreException(msg,src) {
   this.msg=msg;
   this.src=src;
   this.name="soapException";
   this.desc="General error";
}
//string toString()
coreException.prototype.toString=function() {
	return this.name+":"+this.desc+"["+this.src+"] "+this.msg;
}

//*********************************
//coreIllegalArgsEx
//*********************************
function coreIllegalArgsEx(msg,src) {
	coreException.call(this,msg,src);
	this.name="IllegalArgumentException";
	this.desc="Illegal arguments";
}
coreIllegalArgsEx.extend(coreException);

//*********************************
//coreWrongArgNoEx
//*********************************
function coreWrongArgNoEx(msg,src) {
	coreIllegalArgsEx.call(this,msg,src);
	this.desc="Wrong number of arguments";
}
coreWrongArgNoEx.extend(coreIllegalArgsEx);



//==================================================================
//xml (Pustefix xml classes)
//==================================================================


//*********************************
//xmlException(string msg,string src)
//*********************************
function xmlException(msg,src) {
	coreException.call(this,msg,src);
   this.name="xmlException";
   this.desc="XML error";
}
xmlException.extend(coreException);

//*********************************
//xmlUtilities
//*********************************
function xmlUtilities() {
	this.scopeSupport=false;
	try {
		document.scopeName;
		this.scopeSupport=true;
	} catch(ex) {}
}

xmlUtilities.prototype.getChildrenByName=function(node,name) {
	if(arguments.length!=2) throw new coreWrongArgNoEx("","xmlUtilities.getChildrenByName");
	//NOTE: getting child elements via childNodes property and name comparison is much slower
	var nl=node.getElementsByTagName(name);
	var nodes=new Array();
	for(var i=0;i<nl.length;i++) {
		if(nl[i].parentNode==node) nodes.push(nl[i]);
	}
	return nodes;
}

xmlUtilities.prototype.getChildrenByNameNS=function(node,name) {
	if(arguments.length!=2) throw new coreWrongArgNoEx("","xmlUtilities.getChildrenByNameNS");
	if(node.childNodes==null || node.childNodes.length==0) return null;
	var nodes=new Array();
	for(var i=0;i<node.childNodes.length;i++) {
    //    alert( xml.firstChild.scopeName + "::" + xml.firstChild.nodeName );
    if(this.scopeSupport) {
    	if(node.childNodes[i].scopeName+":"+node.childNodes[i].nodeName==name) nodes.push(node.childNodes[i]);
	 } else {
	 	if(node.childNodes[i].nodeName==name) nodes.push(node.childNodes[i]);
	 }
	}
	return nodes;
}

xmlUtilities.prototype.getText=function(node) {
	if(arguments.length!=1) throw new coreWrongArgNoEx("","xmlUtilities.getText");
//	if(!(node instanceof Node)) throw new coreIllegalArgsEx("Illegal argument type: "+(typeof node),"xmlUtilities.getText");
	if(node.childNodes==null) return null;
	var text="";
	for(var i=0;i<node.childNodes.length;i++) {
		var n=node.childNodes[i];
		if(n.nodeType==3) text+=n.nodeValue;
		else if(n.nodeType==1 || n.nodeType==10) text+=this.getText(n);
	}
	return text;
}

var xmlUtils=new xmlUtilities();



//### CONSTANTS ###
var XMLNS_XSD="http://www.w3.org/2001/XMLSchema";
var XMLNS_XSI="http://www.w3.org/2001/XMLSchema-instance";
var XMLNS_SOAPENC="http://schemas.xmlsoap.org/soap/encoding/"
var XMLNS_SOAPENV="http://schemas.xmlsoap.org/soap/envelope/";
var XMLNS_APACHESOAP="http://xml.apache.org/xml-soap";
var XMLNS_PREFIX_MAP=new Array();
XMLNS_PREFIX_MAP[XMLNS_XSD]="xsd";
XMLNS_PREFIX_MAP[XMLNS_XSI]="xsi";
XMLNS_PREFIX_MAP[XMLNS_SOAPENC]="soapenc";
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
	
	this.APACHESOAP_ELEMENT=new QName(XMLNS_APACHESOAP,"Element");
	
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
	this.JS_NUMBER="number";
	this.JS_OBJECT="object";
	this.JS_STRING="string";
	
}

var jstype=new JSType();


//*********************************
//Parameter(String name,soapTypeInfo typeInfo)
//Parameter(String name,soapTypeInfo typeInfo,parameterMode)
//*********************************
function Parameter() {
	this.name=null;
	this.typeInfo=null;
	this.parameterMode=null;
	this.init(arguments);
	this.value=null;
	this.MODE_IN=0;
	this.MODE_INOUT=1;
	this.MODE_OUT=2;
}

Parameter.prototype.init=function(args) {
	if(args.length==2) {
		this.name=args[0];
		this.typeInfo=args[1];
		this.parameterMode=this.MODE_IN;
	} else if(args.length==3) {
		this.name=args[0];
		this.typeInfo=args[1];
		this.parameterMode=args[2];
	} else throw new coreIllegalArgsEx("Wrong number of arguments","Parameter.init");
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



//==================================================================
//soap (Pustefix SOAP classes)
//==================================================================


//*********************************
//soapException
//*********************************
function soapException(msg,src) {
	coreException.call(this,msg,src);
   this.msg=msg;
   this.src=src;
   this.name="soapException";
   this.desc="General error";
}
soapException.extend(coreException);


//*********************************
//soapSerializeEx
//*********************************
function soapSerializeEx(msg,src) {
	soapException.call(this,msg,src);
	this.name="SerializationException";
	this.desc="Serialization failed";
}
soapSerializeEx.extend(soapException);


//*********************************
//soapSimpleSerializer
//*********************************
function soapSimpleSerializer() {
}
//serialize(value,name,typeInfo,writer)
soapSimpleSerializer.prototype.serialize=function(value,name,typeInfo,writer) {
	writer.startElement(name);
	var nsuri=typeInfo.xmlType.namespaceUri;
	var prefix=writer.currentCtx.getPrefix(nsuri);
	if(prefix==null) {
		prefix=writer.getPrefix(nsuri);
		writer.writeNamespaceDeclaration(nsuri);
	}
	writer.writeAttribute(QNAME_XSI_TYPE,prefix+":"+typeInfo.xmlType.localpart);
	writer.writeChars(value);
	writer.endElement(name);
}
//deserialize(typeInfo,element)
soapSimpleSerializer.prototype.deserialize=function(typeInfo,element) {
	return element.firstChild.nodeValue;
}


//*********************************
//soapIntSerializer(QName xmlType)
//*********************************
function soapIntSerializer(xmlType) {
	soapSimpleSerializer.call(this,xmlType);
}
soapIntSerializer.extend(soapSimpleSerializer);
soapIntSerializer.prototype.serialize=function(value,name,typeInfo,writer) {
	if(typeof value!="number") throw new soapSerializeEx("Illegal type: "+(typeof value),"soapIntSerializer.serialize");
	if(isNaN(value)) throw new soapSerializeEx("Illegal value: "+value,"soapIntSerializer.serialize");
	this.superclass.serialize(value,name,typeInfo,writer);
}
soapIntSerializer.prototype.deserialize=function(typeInfo,element) {
	var val=parseInt(this.superclass.deserialize.call(this,typeInfo,element));
	if(isNaN(val)) throw new soapSerializeEx("Illegal value: "+val,"soapIntSerializer.deserialize");
	return val;
}


//*********************************
//soapFloatSerializer(QName xmlType)
//*********************************
function soapFloatSerializer(xmlType) {
	soapSimpleSerializer.call(this,xmlType);
}
soapFloatSerializer.extend(soapSimpleSerializer);
soapFloatSerializer.prototype.serialize=function(value,name,typeInfo,writer) {
	if(typeof value!="number") throw new soapSerializeEx("Illegal type: "+(typeof value),"soapFloatSerializer.serialize");
	if(isNaN(value)) throw new soapSerializeEx("Illegal value: "+value,"soapFloatSerializer.serialize");
	this.superclass.serialize(value,name,typeInfo,writer);
}
soapFloatSerializer.prototype.deserialize=function(typeInfo,element) {
	var val=parseFloat(this.superclass.deserialize.call(this,typeInfo,element));
	if(isNaN(val)) throw new soapSerializeEx("Illegal value: "+val,"soapFloatSerializer.deserialize");
	return val;
}


//*********************************
//soapStringSerializer(QName xmlType)
//*********************************
function soapStringSerializer(xmlType) {
	soapSimpleSerializer.call(this,xmlType);
}
soapStringSerializer.extend(soapSimpleSerializer);
soapStringSerializer.prototype.serialize=function(value,name,typeInfo,writer) {
	if(typeof value!="string") throw new soapSerializeEx("Illegal type: "+(typeof value),"soapStringSerializer.serialize");
	this.superclass.serialize(value,name,typeInfo,writer);
}
soapStringSerializer.prototype.deserialize=function(typeInfo,element) {
	return this.superclass.deserialize.call(this,typeInfo,element);
}


//*********************************
//soapBooleanSerializer(QName xmlType)
//*********************************
function soapBooleanSerializer(xmlType) {
	soapSimpleSerializer.call(this,xmlType);
}
soapBooleanSerializer.extend(soapSimpleSerializer);
soapBooleanSerializer.prototype.serialize=function(value,name,typeInfo,writer) {
	if(typeof value!="boolean") throw new soapSerializeEx("Illegal type: "+(typeof value),"soapBooleanSerializer.serialize");
	this.superclass.serialize(value,name,typeInfo,writer);
}
soapBooleanSerializer.prototype.deserialize=function(typeInfo,element) {
	var str=this.superclass.deserialize.call(this,typeInfo,element);
	if(str=='true') val=true;
	else if(str=='false') val=false;
	else throw new soapSerializeEx("Illegal value: "+str,"soapBooleanSerializer.deserialize");
	return val;
}


//*********************************
//soapDateTimeSerializer(QName xmlType)
//*********************************
function soapDateTimeSerializer(xmlType) {
	soapSimpleSerializer.call(this,xmlType);
}
soapDateTimeSerializer.extend(soapSimpleSerializer);
soapDateTimeSerializer.prototype.fillNulls=function(value,length) {
	var valLen=(""+value).length;
	var filler="";
	for(var i=0;i<(length-valLen);i++) filler+="0"; 
	return filler+value;
}
soapDateTimeSerializer.prototype.parseDate=function(dateStr) {
	var date=new Date();
	var year=dateStr.substr(0,4);
	date.setUTCFullYear(year);
	var month=dateStr.substr(5,2);
	date.setUTCMonth(month-1);
	var day=dateStr.substr(8,2);
	date.setUTCDate(day);
	var hours=dateStr.substr(11,2);
	date.setUTCHours(hours);
	var minutes=dateStr.substr(14,2);
	date.setUTCMinutes(minutes);
	var seconds=dateStr.substr(17,2);
	date.setUTCSeconds(seconds);
	var millis=dateStr.substr(20,3);
	date.setUTCMilliseconds(millis);
	return date;
}
soapDateTimeSerializer.prototype.serialize=function(value,name,typeInfo,writer) {
	if(!(value instanceof Date)) throw new soapSerializeEx("Illegal type: "+(typeof value),"soapDateTimeSerializer.serialize");
	var date=value.getUTCFullYear()+"-"+this.fillNulls(value.getUTCMonth()+1,2)+"-"+
					this.fillNulls(value.getUTCDate(),2)+"T"+this.fillNulls(value.getUTCHours(),2)+":"+
					this.fillNulls(value.getUTCMinutes(),2)+":"+this.fillNulls(value.getUTCSeconds(),2)+"."+
					this.fillNulls(value.getUTCMilliseconds(),3)+"Z";
	this.superclass.serialize(date,name,typeInfo,writer);
}
soapDateTimeSerializer.prototype.deserialize=function(typeInfo,element) {
	var val=this.parseDate(this.superclass.deserialize.call(this,typeInfo,element));
	return val;
}

//*********************************
//soapArraySerializer
//*********************************
function soapArraySerializer(xmlType) {
	soapSimpleSerializer.call(this,xmlType);
}
soapArraySerializer.extend(soapSimpleSerializer);
soapArraySerializer.prototype.serializeSub=function(value,name,typeInfo,dim,writer) {
	if(dim>0 && value instanceof Array) {
		writer.startElement(name);
		if(dim==typeInfo.dimension) {
			var prefix=writer.getPrefix(QNAME_ARRAY.namespaceUri);
			writer.writeAttribute(QNAME_XSI_TYPE,prefix+":"+QNAME_ARRAY.localpart);
		}
		var dimStr="";
		for(var j=0;j<dim;j++) dimStr+="[]";
		dimStr=dimStr.replace(/\[\]$/,"["+value.length+"]");
		
		var nsuri=typeInfo.arrayType.xmlType.namespaceUri;
		var prefix=writer.currentCtx.getPrefix(nsuri);
		if(prefix==null) {
			prefix=writer.getPrefix(nsuri);
			writer.writeNamespaceDeclaration(nsuri);
		}
		
		writer.writeAttribute(QNAME_ARRAY_TYPE,prefix+":"+typeInfo.arrayType.xmlType.localpart+dimStr);
		for(var i=0;i<value.length;i++) {
			this.serializeSub(value[i],"item",typeInfo,dim-1,writer);
		}
		writer.endElement(name);
	} else {
		var serializer=typeMapping.getSerializerByInfo(typeInfo.arrayType);
		serializer.serialize(value,name,typeInfo.arrayType,writer);
	}
}

soapArraySerializer.prototype.serialize=function(value,name,typeInfo,writer) {
	this.serializeSub(value,name,typeInfo,typeInfo.dimension,writer);
}

soapArraySerializer.prototype.deserializeSub=function(typeInfo,dim,element) {
	if(dim>1) {
		var items=xmlUtils.getChildrenByName(element,"item");
		var array=new Array();
		for(var i=0;i<items.length;i++) {
			var subArray=this.deserializeSub(typeInfo,dim-1,items[i]);
			array.push(subArray);
		}
		return array;
	} else if(dim==1) {
		var items=xmlUtils.getChildrenByName(element,"item");
		var array=new Array();
		if(items!=null) {
			var deserializer=typeMapping.getSerializerByInfo(typeInfo.arrayType);
			for(var i=0;i<items.length;i++) {	
				var val=deserializer.deserialize(typeInfo.arrayType,items[i]);
				array.push(val);
			}
		}
		return array;
	} else throw new soapSerializeEx("Illegal array dimension: "+dim,"soapArraySerializer.deserializeSub");
}

soapArraySerializer.prototype.deserialize=function(typeInfo,element) {
	return this.deserializeSub(typeInfo,typeInfo.dimension,element);
}


//*********************************
//soapBeanSerializer(QName type)
//*********************************
function soapBeanSerializer(type) {
	soapSimpleSerializer.call(this,type);
}
soapBeanSerializer.extend(soapSimpleSerializer);
soapBeanSerializer.prototype.serialize=function(value,name,typeInfo,writer) {
	writer.startElement(name);
	for(var i=0;i<typeInfo.propNames.length;i++) {
		var propName=typeInfo.propNames[i];
		var propInfo=typeInfo.propToInfo[propName];
		var serializer=typeMapping.getSerializerByInfo(propInfo);
		var propVal=value[propName];
		if(propVal==undefined) throw new soapSerializeEx("Missing bean property: "+propName,"soapBeanSerializer.serialize");
		serializer.serialize(propVal,propName,propInfo,writer);
	}
	writer.endElement(name);
}
soapBeanSerializer.prototype.deserialize=function(typeInfo,element) {
	var obj=new Object();
	for(var i=0;i<typeInfo.propNames.length;i++) {
		var propName=typeInfo.propNames[i];
		var propInfo=typeInfo.propToInfo[propName];
		var serializer=typeMapping.getSerializerByInfo(propInfo);
		var items=xmlUtils.getChildrenByName(element,propName);
		if(items.length>0) {
			var deserializer=typeMapping.getSerializerByInfo(propInfo);
			var val=deserializer.deserialize(propInfo,items[0]);
			obj[propName]=val;
		}
	}
	return obj;
}

//*********************************
//soapElementSerializer(QName type)
//*********************************
function soapElementSerializer(type) {
	soapSimpleSerializer.call(this,type);
}
soapElementSerializer.extend(soapSimpleSerializer);
soapElementSerializer.prototype.serialize=function(value,name,typeInfo,writer) {
	writer.startElement(name);
	this.serializeSub(value,writer);
	writer.endElement(name);
}
soapElementSerializer.prototype.serializeSub=function(element,writer) {
	writer.startElement(element.nodeName);
	for(var j=0;j<element.attributes.length;j++) {
		var node=element.attributes.item(j);
		writer.writeAttribute(node.nodeName,node.nodeValue);
	}
	for(var i=0;i<element.childNodes.length;i++) {
		var node=element.childNodes[i];
		if(node.nodeType==1) this.serializeSub(node,writer);
		else if(node.nodeType==3) writer.writeChars(node.nodeValue);
	}
	writer.endElement(element.nodeName);
}
soapElementSerializer.prototype.deserialize=function(typeInfo,element) {
	for(var i=0;i<element.childNodes.length;i++) {
		var node=element.childNodes[i];
		if(node.nodeType==1) return node;
	}
}


//*********************************
//TypeMapping()
//*********************************
function TypeMapping() {
	this.mappings=new Array();
	this.init();
}

//init()
TypeMapping.prototype.init=function() {
	this.mappings[xmltypes.XSD_BOOLEAN.hashKey()]=new soapBooleanSerializer();
	this.mappings[xmltypes.XSD_INT.hashKey()]=new soapIntSerializer();
	this.mappings[xmltypes.XSD_FLOAT.hashKey()]=new soapFloatSerializer();	
	this.mappings[xmltypes.XSD_STRING.hashKey()]=new soapStringSerializer();
	this.mappings[xmltypes.XSD_DATETIME.hashKey()]=new soapDateTimeSerializer();
	this.mappings[xmltypes.SOAP_STRING.hashKey()]=new soapStringSerializer();
	this.mappings["ARRAY"]=new soapArraySerializer();
	this.mappings["BEAN"]=new soapBeanSerializer();
	this.mappings[xmltypes.APACHESOAP_ELEMENT.hashKey()]=new soapElementSerializer();
}

//register(QName xmlType,Serializer serializer)
TypeMapping.prototype.register=function(xmlType,serializer) {
	this.mappings[xmlType.hashKey()]=serializer;
}

//Serializer getSerializer(QName xmlType)
TypeMapping.prototype.getSerializer=function(xmlType) {
	if(arguments.length==1) {
		var serializer=this.mappings[xmlType.hashKey()];
		if(serializer==null) throw "Can't find serializer for type '"+xmlType.toString()+"'";
		return serializer;
	} else throw new coreIllegalArgsEx("Wrong number of arguments","TypeMapping.getSerializer");
}

//Serializer getSerializerByInfo(TypeInfo info)
TypeMapping.prototype.getSerializerByInfo=function(info) {
	if(arguments.length==1) {
		var serializer=this.mappings[info.xmlType.hashKey()];
		if(serializer==null && (info instanceof soapArrayInfo)) serializer=this.mappings["ARRAY"];
		if(serializer==null && (info instanceof soapBeanInfo)) serializer=this.mappings["BEAN"];
		if(serializer==null) throw "Can't find serializer for type '"+info.xmlType.toString()+"'";
		return serializer;
	} else throw new coreIllegalArgsEx("Wrong number of arguments","TypeMapping.getSerializerByInfo");
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
		var serializer=typeMapping.getSerializerByInfo(this.params[i].typeInfo);
		serializer.serialize(this.params[i].value,this.params[i].name,this.params[i].typeInfo,writer);
	}
	writer.endElement(this.opName);
}

RPCSerializer.prototype.deserialize=function(element) {
	var serializer=typeMapping.getSerializerByInfo(this.retTypeInfo);
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
  	this.userCallback=null;
  	this.request=null;
}

//setTargetEndpointAddress(address)
Call.prototype.setTargetEndpointAddress=function() {
	if(arguments.length==1) {
		this.endpoint=arguments[0];
	}
}

Call.prototype.setUserCallback=function(cb) {
	this.userCallback=cb;
}

//setOperationName(operationName)
Call.prototype.setOperationName=function() {
	if(arguments.length==1) {
		this.opName=arguments[0];
	}
}	

//addParameter(paramName,typeInfo,parameterMode)
Call.prototype.addParameter=function() {
	if(arguments.length==2) {
		var param=new Parameter(arguments[0],arguments[1]);
		this.params.push(param);
	} else if(arguments.length==3) {
		var param=new Parameter(arguments[0],arguments[1],arguments[2]);
		this.params.push(param);
	} else throw new coreIllegalArgsEx("Wrong number of arguments","Call.addParameter");
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
	if(this.params.length!=arguments.length-ind) throw new coreIllegalArgsEx("Wrong number of arguments","Call.invoke");
	for(var i=0;i<this.params.length;i++) {
		this.params[i].setValue(arguments[i+ind]);
	}
	var rpc=new RPCSerializer(this.opName,this.params,this.retTypeInfo);
	
	var bodyElem=new SOAPBodyElement(rpc);
	soapMsg.getSOAPPart().getEnvelope().getBody().addBodyElement(bodyElem);
	soapMsg.write(writer);
	
	
//alert(writer.xml);
  var resDoc;
  if( !this.userCallback ) {
    // sync
    this.request=new xmlRequest('POST',this.endpoint);
    resDoc=this.request.start(writer.xml); 
   
    //resDoc = new xmlRequest( 'ePOST', this.endpoint ).start( writer.xml );
    return this.callback(resDoc);
  } else {
    // async
    this.request=new xmlRequest( 'POST', this.endpoint, this.callback, this );
    return this.request.start(writer.xml);
  }
}

Call.prototype.callback=function(xml) {
	try {
		var soapMsg=new SOAPMessage(xml);
		var fault=soapMsg.getSOAPPart().getEnvelope().getBody().getFault();
		if(fault!=null) {
			var ex=new soapException(fault.toString(),"Call.callback");
			if(this.userCallback) this.userCallback(null,ex);
			else throw ex;
		} else {
			var rpc=new RPCSerializer( this.opName, null, this.retTypeInfo);
			var res = rpc.deserialize(soapMsg.getSOAPPart().getEnvelope().getBody().element);
  			if(this.userCallback) this.userCallback(res);
   		else return res;
   	}
  	} catch(ex) {
  		if(this.userCallback) this.userCallback(null,ex);
  		else throw ex;
  	}
};




//*********************************
// SOAPMessage()
// SOAPMessage(Document xml)
//*********************************
function SOAPMessage() {
	if(arguments.length==0) {
		this.soapPart=new SOAPPart();
	} else if(arguments.length==1) {
		this.soapPart=new SOAPPart(arguments[0]);
	} else throw new coreWrongArgNoEx("","SOAPMessage");
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
// SOAPPart(Document xml)
//*********************************
function SOAPPart() {
	if(arguments.length==0) {
		this.envelope=new SOAPEnvelope();
	} else if(arguments.length==1) {
		var e=xmlUtils.getChildrenByNameNS(arguments[0],"soapenv:Envelope")[0];
		if(e==null) throw new soapException("NO SOAP MESSAGE","SOAPPart");
		this.envelope=new SOAPEnvelope(e);
	} else throw new coreWrongArgNoEx("","SOAPPart");
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
// SOAPEnvelope(Element elem)
//*********************************
function SOAPEnvelope() {
	if(arguments.length==0) {
		this.header=new SOAPHeader();
		this.body=new SOAPBody();
		this.element=null;
	} else if(arguments.length==1) {
		var e=xmlUtils.getChildrenByNameNS(arguments[0],"soapenv:Header")[0];
		if(e!=null) this.header=new SOAPHeader(e);
		else this.header=new SOAPHeader();
		e=xmlUtils.getChildrenByNameNS(arguments[0],"soapenv:Body")[0];
		if(e!=null) this.body=new SOAPBody(e);
		else throw new soapException("NO MESSAGE BODY","SOAPEnvelope");
		this.element=arguments[0];
	} else throw new coreWrongArgNoEx("","SOAPEnvelope");
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
// SOAPBody(Element elem)
//*********************************
function SOAPBody() {
	if(arguments.length==0) {
		this.fault=null;
		this.bodyElems=new Array();
		this.element=null;
	} else if(arguments.length==1) {
		var e=xmlUtils.getChildrenByNameNS(arguments[0],"soapenv:Fault")[0];
		if(e!=null) this.fault=new SOAPFault(e);
		this.element=arguments[0];
	} else throw new coreWrongArgNoEx("","SOAPEnvelope");
}

//addBodyElement(SOAPBodyElement bodyElem)
SOAPBody.prototype.addBodyElement=function(bodyElem) {
	this.bodyElems.push(bodyElem);	
}

//SOAPFault getFault()
SOAPBody.prototype.getFault=function(fault) {
	return this.fault;
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
// SOAPFault
// SOAPFault(Element elem)
//*********************************
function SOAPFault() {
	if(arguments.length==0) {
		this.faultCode=null;
		this.faultString=null;
		this.detail=null;
		this.element=null;
	} else if(arguments.length==1) {
		var nl=xmlUtils.getChildrenByName(arguments[0],"faultcode");
		if(nl!=null && nl.length>0) this.faultCode=xmlUtils.getText(nl[0]);
		nl=xmlUtils.getChildrenByName(arguments[0],"faultstring");
		if(nl!=null && nl.length>0) this.faultString=xmlUtils.getText(nl[0]);
		nl=xmlUtils.getChildrenByName(arguments[0],"detail");
		if(nl!=null && nl.length>0) this.detail=xmlUtils.getText(nl[0]);
		this.element=arguments[0];
	} else throw new coreWrongArgNoEx("","SOAPFault");
}
SOAPFault.prototype.toString=function() {
	var str="";
	if(this.faultCode!=null) str+=": "+this.faultCode;
	if(this.faultString!=null) str+=": "+this.faultString;
	return str;
}


//*********************************
// soapTypeInfo(QName xmlType)
//*********************************
function soapTypeInfo(xmlType) {
	this.xmlType=xmlType;
}

//*********************************
// soapArrayInfo(QName xmlType)
// soapArrayInfo(QName xmlType,TypeInfo arrayType,Number dimension)
//*********************************
function soapArrayInfo(xmlType,arrayType,dimension) {
	soapTypeInfo.call(this,xmlType);
	if(arguments.length==3) {
		this.dimension=dimension;
		this.arrayType=arrayType;
	}
}
soapArrayInfo.extend(soapTypeInfo);
soapArrayInfo.prototype.populate=function(arrayType,dimension) {
	this.arrayType=arrayType;
	this.dimension=dimension;
}

//*********************************
// soapBeanInfo(QName xmlType)
// soapBeanInfo(QName xmlType,Array props)
//*********************************
function soapBeanInfo(xmlType,props) {
	soapTypeInfo.call(this,xmlType);
	this.props=null;
	this.propNames=null;
	this.propToInfo=null;
	if(arguments.length==2) {
		this.props=props;
		this.init();
	}
}
soapBeanInfo.extend(soapTypeInfo);
soapBeanInfo.prototype.init=function() {
	this.propNames=new Array();
	this.propToInfo=new Array();
	for(var i=0;i<this.props.length;i=i+2) {
		this.propNames.push(this.props[i]);
		this.propToInfo[this.props[i]]=this.props[i+1];
	}
}
soapBeanInfo.prototype.populate=function(props) {
	this.props=props;
	this.init();
}

//*********************************
// soapStub
//*********************************
function soapStub() {
	this._url="";
	this._typeInfos=new Array();
}

soapStub.prototype._createCall=function() {
  var call=new Call();
  call.setTargetEndpointAddress(this._url);
  return call;
}

soapStub.prototype._extractCallback=function(args,expLen) {
	var argLen=args.length;
	if(argLen==expLen+1 && typeof args[argLen-1]=="function") return args[argLen-1];
	else if(argLen!=expLen) throw new coreIllegalArgsEx("Wrong number of arguments","soapStub._extractCallback");
	return null;
}

soapStub.prototype._setURL=function(url) {
	this._url=url.replace(/(https?:\/\/)([^\/]+)(.*)/,"$1"+window.location.host+"$3");
	var session=window.location.href.replace(/.*(;jsessionid=[A-Z0-9]+\.[a-zA-Z0-9]+)(\?.*)?$/,"$1");
	this._url+=session;
}
