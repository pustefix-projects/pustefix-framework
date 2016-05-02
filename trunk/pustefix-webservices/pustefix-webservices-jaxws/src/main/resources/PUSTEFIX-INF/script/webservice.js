//Add inheritance support
Function.prototype.extend=function(base) {
  var derived=this.prototype=new base;
  this.prototype.superclass=base.prototype;
  return derived;
};



//==================================================================
//CORE_ (Pustefix core classes)
//==================================================================


//*********************************
//CORE_Exception(string msg,string src)
//*********************************
function CORE_Exception(msg,src) {
  this.msg=msg;
  this.src=src;
  this.name="CORE_Exception";
  this.desc="General error";
}
//string toString()
CORE_Exception.prototype.toString=function() {
  return this.name+":"+this.desc+"["+this.src+"] "+this.msg;
}

//*********************************
//CORE_IllegalArgsEx
//*********************************
function CORE_IllegalArgsEx(msg,src) {
  CORE_Exception.call(this,msg,src);
  this.name="IllegalArgumentException";
  this.desc="Illegal arguments";
}
CORE_IllegalArgsEx.extend(CORE_Exception);

//*********************************
//CORE_WrongArgNoEx
//*********************************
function CORE_WrongArgNoEx(msg,src) {
  CORE_IllegalArgsEx.call(this,msg,src);
  this.desc="Wrong number of arguments";
}
CORE_WrongArgNoEx.extend(CORE_IllegalArgsEx);



//==================================================================
//XML_ (Pustefix xml classes)
//==================================================================


//CONSTANTS
var XML_NS_XSD="http://www.w3.org/2001/XMLSchema";
var XML_NS_XSI="http://www.w3.org/2001/XMLSchema-instance";
var XML_NS_SOAPENV="http://schemas.xmlsoap.org/soap/envelope/";
var XML_NS_PREFIX_MAP=new Array();
XML_NS_PREFIX_MAP[XML_NS_XSD]="xsd";
XML_NS_PREFIX_MAP[XML_NS_XSI]="xsi";
XML_NS_PREFIX_MAP[XML_NS_SOAPENV]="S";


//*********************************
//XML_Exception(string msg,string src)
//*********************************
function XML_Exception(msg,src) {
  CORE_Exception.call(this,msg,src);
  this.name="XML_Exception";
  this.desc="XML error";
}
XML_Exception.extend(CORE_Exception);

//*********************************
//XML_Utilities
//*********************************
function XML_Utilities() {
  this.scopeChecked=false;
  this.scopeSupport=false;
}

XML_Utilities.prototype.getChildElements=function(node) {
  var nodes=new Array();
  var nl=node.childNodes;
  if(nl!=null) {
    for(var i=0;i<nl.length;i++) {
      if(nl[i].nodeType==1) nodes.push(nl[i]);
    }
  }
  return nodes;
}

XML_Utilities.prototype.getChildrenByName=function(node,name) {
  if(arguments.length!=2) throw new CORE_WrongArgNoEx("","XML_Utilities.getChildrenByName");
  var nodes=new Array();
  var nl=node.childNodes;
  if(nl!=null) {
    for(var i=0;i<nl.length;i++) {
      var localName=nl[i].nodeName;
      var ind=localName.indexOf(":");
      if(ind>-1) localName=localName.substring(ind+1,localName.length);
      if(localName==name) nodes.push(nl[i]);
    }
  }
  return nodes;
}

XML_Utilities.prototype.getChildrenByNameNS=function(node,nsuri,name) {
  if(arguments.length!=3) throw new CORE_WrongArgNoEx("","XML_Utilities.getChildrenByNameNS");
  var nl=node.childNodes;
  if(nl==null || nl.length==0) return null;
  if(!this.scopeChecked) {
    if(typeof node.scopeName!="undefined") this.scopeSupport=true;
    scopeChecked=true;
  }
  var nodes=new Array();
  for(var i=0;i<nl.length;i++) {
    var localName=nl[i].nodeName;
    var ind=localName.indexOf(":");
    if(ind>-1) localName=localName.substring(ind+1,localName.length);
    if(this.scopeSupport) {
      if(nl[i].tagUrn==nsuri && localName==name) nodes.push(nl[i]);
    } else {
      if(nl[i].namespaceURI==nsuri && localName==name) nodes.push(nl[i]);
    }
  }
  return nodes;
}

XML_Utilities.prototype.getText=function(node) {
  if(arguments.length!=1) throw new CORE_WrongArgNoEx("","XML_Utilities.getText");
  var nl=node.childNodes;
  if(nl==null) return null;
  var text="";
  for(var i=0;i<nl.length;i++) {
    var n=nl[i];
    if(n.nodeType==3) text+=n.nodeValue;
    else if(n.nodeType==1 || n.nodeType==10) text+=this.getText(n);
  }
  return text;
}

XML_Utilities.prototype.filterElements=function(elements,name) {
  var filtered=[];
  for(var i=0;i<elements.length;i++) {
    if(elements[i].nodeName==name) filtered.push(elements[i]);
  }
  return filtered;
}

var XML_Utilities=new XML_Utilities();

//*********************************
//XML_QName(localpart)
//XML_QName(namespaceUri,localpart)
//XML_QName(namespaceUri,localpart,prefix)
//*********************************
function XML_QName() {
  this.namespaceUri=null;
  this.localpart=null;
  this.prefix=null;
  this.init(arguments);
}

XML_QName.prototype.init=function(args){
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

XML_QName.prototype.hashKey=function() {
  return this.namespaceUri+"#"+this.localpart;
}

XML_QName.prototype.toString=function() {
  var name=this.localpart;
  if(this.prefix!=null) name=this.prefix+":"+name;
  if(this.namespaceUri!=null) {
    name+=" xmlns";
    if(this.prefix!=null) name=name+":"+this.prefix;
    name=name+"=\""+this.namespaceUri+"\"";
  }
  return name; 
}


//*********************************
//XML_Types
//*********************************
function XML_Types() {
  this.XSD_BASE64=new XML_QName(XML_NS_XSD,"base64Binary");
  this.XSD_BOOLEAN=new XML_QName(XML_NS_XSD,"boolean");
  this.XSD_BYTE=new XML_QName(XML_NS_XSD,"byte");
  this.XSD_DATETIME=new XML_QName(XML_NS_XSD,"dateTime");
  this.XSD_DECIMAL=new XML_QName(XML_NS_XSD,"decimal");
  this.XSD_DOUBLE=new XML_QName(XML_NS_XSD,"double");
  this.XSD_FLOAT=new XML_QName(XML_NS_XSD,"float");
  this.XSD_HEXBINARY=new XML_QName(XML_NS_XSD,"hexBinary");
  this.XSD_BYTE=new XML_QName(XML_NS_XSD,"byte");
  this.XSD_SHORT=new XML_QName(XML_NS_XSD,"short");
  this.XSD_INT=new XML_QName(XML_NS_XSD,"int");
  this.XSD_INTEGER=new XML_QName(XML_NS_XSD,"integer");
  this.XSD_LONG=new XML_QName(XML_NS_XSD,"long");
  this.XSD_QNAME=new XML_QName(XML_NS_XSD,"XML_QName");
  this.XSD_SHORT=new XML_QName(XML_NS_XSD,"short");
  this.XSD_STRING=new XML_QName(XML_NS_XSD,"string");  
}

var XML_Types=new XML_Types();


//*********************************
//XML_Context(XML_Context parent)
//*********************************
function XML_Context(parent) {
  this.parent=parent
    this.prefixMap=new Array();
}

//String getPrefix(String nsuri)
XML_Context.prototype.getPrefix=function(nsuri) {
  var prefix=this.prefixMap[nsuri];
  if(prefix==null && this.parent!=null) {
    prefix=this.parent.getPrefix(nsuri);
  }
  return prefix;
}

//setPrefix(String nsuri,String prefix)
XML_Context.prototype.setPrefix=function(nsuri,prefix) {
  this.prefixMap[nsuri]=prefix;
}


//*********************************
//XML_Writer()
//*********************************
function XML_Writer() {
  this.xml=null;
  this.currentCtx=null;
  this.init();
  this.inStart=false;
  this.prefixMap=new Array();
  this.prefixBase="ns";
  this.prefixCnt=0;
}

XML_Writer.prototype.init=function() {
  this.xml="";
}

//startElement(String name)
//startElement(XML_QName name)
XML_Writer.prototype.startElement=function(name) {
  this.currentCtx=new XML_Context(this.currentCtx);
  if(this.inStart) {
    this.xml+=">";
  }
  var tagName=null;
  var prefix=null;
  var nsuri=null;
  var newNS=false;
  if(name instanceof XML_QName) {
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
//endElement(XML_QName name)
XML_Writer.prototype.endElement=function(name) {
  if(this.inStart) {
    this.xml+="/>";
  } else {
    var tagName=null;
    var prefix=null;
    var nsuri=null;
    if(name instanceof XML_QName) {
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
//writeAttribute(XML_QName name,String value)
XML_Writer.prototype.writeAttribute=function(name,value) {
  var attrName=null;
  var prefix=null;
  var nsuri=null;
  var newNS=false;
  if(name instanceof XML_QName) {
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
XML_Writer.prototype.writeChars=function(chars) {
  if(this.inStart) {
    this.xml+=">";
    this.inStart=false;
  }
  chars=chars.replace(/&/g,"&amp;");
  chars=chars.replace(/</g,"&lt;");
  this.xml+=chars;
}

//writeNamespaceDeclaration(String nsuri)
//writeNamespaceDeclaration(String nsuri,String prefix)
XML_Writer.prototype.writeNamespaceDeclaration=function() {
  if(arguments.length==1) {
    var prefix=this.getPrefix(arguments[0]);
    this.writeNamespaceDeclaration(arguments[0],prefix);
  } else if(arguments.length==2) {
    this.xml+=" xmlns:"+arguments[1]+"=\""+arguments[0]+"\"";
    this.currentCtx.setPrefix(arguments[0],arguments[1]);
  }
}

//String getPrefix(String nsuri)
XML_Writer.prototype.getPrefix=function(nsuri) {
  var prefix=this.prefixMap[nsuri];
  if(prefix==null) {
    prefix=XML_NS_PREFIX_MAP[nsuri];
    if(prefix==null) {
      prefix=this.prefixBase+this.prefixCnt;
      this.prefixCnt++;
    }
  }
  this.prefixMap[nsuri]=prefix;
  return prefix;
}



//==================================================================
//SOAP_ (Pustefix SOAP classes)
//==================================================================


//CONSTANTS


var SOAP_XSI_TYPE=new XML_QName(XML_NS_XSI,"type");
var SOAP_XSI_NIL=new XML_QName(XML_NS_XSI,"nil");


//*********************************
//SOAP_Exception
//*********************************
function SOAP_Exception(msg,src) {
  CORE_Exception.call(this,msg,src);
  this.msg=msg;
  this.src=src;
  this.name="SOAP_Exception";
  this.desc="General error";
}
SOAP_Exception.extend(CORE_Exception);


//*********************************
//SOAP_SerializeEx
//*********************************
function SOAP_SerializeEx(msg,src) {
  SOAP_Exception.call(this,msg,src);
  this.name="SerializationException";
  this.desc="Serialization failed";
}
SOAP_SerializeEx.extend(SOAP_Exception);


//*********************************
//SOAP_SerializeCtx
//*********************************
function SOAP_SerializeCtx() {
  this.style=null;
  this.use=null;
}

SOAP_SerializeCtx.prototype.setEncoding=function(style,use) {
  this.style=style;
  this.use=use;
}


//*********************************
//SOAP_SimpleSerializer
//*********************************
function SOAP_SimpleSerializer() {
}
//serialize(value,name,typeInfo,writer,ctx)
SOAP_SimpleSerializer.prototype.serialize=function(value,name,typeInfo,writer,ctx) {
  if(value==null) return;
  writer.startElement(name);
  var nsuri=typeInfo.xmlType.namespaceUri;
  var prefix=writer.currentCtx.getPrefix(nsuri);
  if(prefix==null) {
    prefix=writer.getPrefix(nsuri);
    writer.writeNamespaceDeclaration(nsuri);
  }
  else writer.writeChars(""+value);
  writer.endElement(name);
}
//deserialize(typeInfo,element)
SOAP_SimpleSerializer.prototype.deserialize=function(typeInfo,element) {
  if(element.getAttribute(XML_NS_PREFIX_MAP[XML_NS_XSI]+":nil")) return null;
  var val="";
  var nl=element.childNodes;
  if(nl!=null) {
    for(var i=0;i<nl.length;i++) val+=nl[i].nodeValue;
  }
  return val; 
}


//*********************************
//SOAP_NumberSerializer(XML_QName xmlType)
//*********************************
function SOAP_NumberSerializer(xmlType) {
  SOAP_SimpleSerializer.call(this,xmlType);
}

SOAP_NumberSerializer.extend(SOAP_SimpleSerializer);
SOAP_NumberSerializer.prototype.serialize=function(value,name,typeInfo,writer,ctx) {
  if(value!=null) {
    if(typeof value!="number") throw new SOAP_SerializeEx("Illegal type: "+(typeof value),"SOAP_NumberSerializer.serialize");
    if(isNaN(value)) throw new SOAP_SerializeEx("Illegal value: "+value,"SOAP_NumberSerializer.serialize");
  }
  this.superclass.serialize(value,name,typeInfo,writer,ctx);
}
SOAP_NumberSerializer.prototype.deserialize=function(typeInfo,element) {
  var val=parseInt(this.superclass.deserialize.call(this,typeInfo,element));
  if( isNaN(val) ) {
    if(element.getAttribute(XML_NS_PREFIX_MAP[XML_NS_XSI]+":nil")) return null;
    throw new SOAP_SerializeEx("Illegal value: "+val,"SOAP_NumberSerializer.deserialize");
  }
  return val;
}


//*********************************
//SOAP_FloatSerializer(XML_QName xmlType)
//*********************************
function SOAP_FloatSerializer(xmlType) {
  SOAP_SimpleSerializer.call(this,xmlType);
}
SOAP_FloatSerializer.extend(SOAP_SimpleSerializer);
SOAP_FloatSerializer.prototype.serialize=function(value,name,typeInfo,writer,ctx) {
  if(value!=null) {
    if(typeof value!="number") throw new SOAP_SerializeEx("Illegal type: "+(typeof value),"SOAP_FloatSerializer.serialize");
    if(isNaN(value)) throw new SOAP_SerializeEx("Illegal value: "+value,"SOAP_FloatSerializer.serialize");
  }
  this.superclass.serialize(value,name,typeInfo,writer,ctx);
}
SOAP_FloatSerializer.prototype.deserialize=function(typeInfo,element) {
  var val=parseFloat(this.superclass.deserialize.call(this,typeInfo,element));
  if( isNaN(val) ) {
    if(element.getAttribute(XML_NS_PREFIX_MAP[XML_NS_XSI]+":nil")) return null;
    throw new SOAP_SerializeEx("Illegal value: "+val,"SOAP_FloatSerializer.deserialize");
  }
  return val;
}


//*********************************
//SOAP_StringSerializer(XML_QName xmlType)
//*********************************
function SOAP_StringSerializer(xmlType) {
  SOAP_SimpleSerializer.call(this,xmlType);
}
SOAP_StringSerializer.extend(SOAP_SimpleSerializer);
SOAP_StringSerializer.prototype.serialize=function(value,name,typeInfo,writer,ctx) {
  if(value!=null && typeof value!="string") throw new SOAP_SerializeEx("Illegal type: "+(typeof value),"SOAP_StringSerializer.serialize");
  this.superclass.serialize(value,name,typeInfo,writer,ctx);
}
SOAP_StringSerializer.prototype.deserialize=function(typeInfo,element) {
  return this.superclass.deserialize.call(this,typeInfo,element);
}


//*********************************
//SOAP_BooleanSerializer(XML_QName xmlType)
//*********************************
function SOAP_BooleanSerializer(xmlType) {
  SOAP_SimpleSerializer.call(this,xmlType);
}
SOAP_BooleanSerializer.extend(SOAP_SimpleSerializer);
SOAP_BooleanSerializer.prototype.serialize=function(value,name,typeInfo,writer,ctx) {
  if(value!=null && typeof value!="boolean") throw new SOAP_SerializeEx("Illegal type: "+(typeof value),"SOAP_BooleanSerializer.serialize");
  this.superclass.serialize(value,name,typeInfo,writer,ctx);
}
SOAP_BooleanSerializer.prototype.deserialize=function(typeInfo,element) {
  var str=this.superclass.deserialize.call(this,typeInfo,element);
  if(str=='true') val=true;
  else if(str=='false') val=false;
  else if(element.getAttribute(XML_NS_PREFIX_MAP[XML_NS_XSI]+":nil")) val=null;
  else throw new SOAP_SerializeEx("Illegal value: "+str,"SOAP_BooleanSerializer.deserialize");
  return val;
}


//*********************************
//SOAP_DateTimeSerializer(XML_QName xmlType)
//*********************************
function SOAP_DateTimeSerializer(xmlType) {
  SOAP_SimpleSerializer.call(this,xmlType);
}
SOAP_DateTimeSerializer.extend(SOAP_SimpleSerializer);
SOAP_DateTimeSerializer.prototype.fillNulls=function(value,length) {
  var valLen=(""+value).length;
  var filler="";
  for(var i=0;i<(length-valLen);i++) filler+="0"; 
  return filler+value;
}
SOAP_DateTimeSerializer.prototype.parseDate=function(dateStr) {
  if(!dateStr) return null;
  var date=new Date();
  var year=dateStr.substr(0,4);
  date.setFullYear(year);
  var month=dateStr.substr(5,2);
  date.setMonth(month-1);
  var day=dateStr.substr(8,2);
  date.setDate(day);
  var hours=dateStr.substr(11,2);
  date.setHours(hours);
  var minutes=dateStr.substr(14,2);
  date.setMinutes(minutes);
  var seconds=dateStr.substr(17,2);
  date.setSeconds(seconds);
  var millis=dateStr.substr(20,3);
  date.setMilliseconds(millis);
  return date;
}
SOAP_DateTimeSerializer.prototype.serialize=function(value,name,typeInfo,writer,ctx) {
  if(value!=null && !(value instanceof Date)) throw new SOAP_SerializeEx("Illegal type: "+(typeof value),"SOAP_DateTimeSerializer.serialize");
  if(value==null) {
    this.superclass.serialize(value,name,typeInfo,writer,ctx);
  } else {
    var offset=value.getTimezoneOffset();
    var tzp=offset>0?"-":"+";
    offset=Math.abs(offset);
    var tzh=Math.floor(offset / 60);
    var tzm=offset % 60;
    var tz=tzp+this.fillNulls(tzh,2)+":"+this.fillNulls(tzm,2);
    var date=value.getFullYear()+"-"+this.fillNulls(value.getMonth()+1,2)+"-"+
    this.fillNulls(value.getDate(),2)+"T"+this.fillNulls(value.getHours(),2)+":"+
    this.fillNulls(value.getMinutes(),2)+":"+this.fillNulls(value.getSeconds(),2)+"."+
    this.fillNulls(value.getMilliseconds(),3)+tz;
    this.superclass.serialize(date,name,typeInfo,writer,ctx);
  }
}
SOAP_DateTimeSerializer.prototype.deserialize=function(typeInfo,element) {
  return this.parseDate(this.superclass.deserialize.call(this,typeInfo,element));
}

//*********************************
//SOAP_ArraySerializer
//*********************************
function SOAP_ArraySerializer(xmlType) {
  SOAP_SimpleSerializer.call(this,xmlType);
}
SOAP_ArraySerializer.extend(SOAP_SimpleSerializer);
SOAP_ArraySerializer.prototype.serializeSub=function(value,name,typeInfo,dim,writer,ctx) {
  if(dim>0 && (value instanceof Array || (typeof value=="object" && typeof value.slice!="undefined")) ) {
    //var nsuri=typeInfo.arrayType.xmlType.namespaceUri;
    //var prefix=writer.currentCtx.getPrefix(nsuri);
    //if(prefix==null) {
    //  prefix=writer.getPrefix(nsuri);
    //  writer.writeNamespaceDeclaration(nsuri);
    //}
    for(var i=0;i<value.length;i++) {
      this.serializeSub(value[i],name,typeInfo,dim-1,writer,ctx);
    }
  } else {
    //writer.startElement(name);
    var serializer=SOAP_TypeMapping.getSerializerByInfo(typeInfo.arrayType);
    serializer.serialize(value,name,typeInfo.arrayType,writer,ctx);
    //writer.endElement(name);
  }
}

SOAP_ArraySerializer.prototype.serialize=function(value,name,typeInfo,writer,ctx) {
  this.serializeSub(value,name,typeInfo,typeInfo.dimension,writer,ctx);
}

SOAP_ArraySerializer.prototype.deserializeSub=function(typeInfo,dim,elements) {
  if(dim>1) {
    var array=new Array();
    for(var i=0;i<elements.length;i++) {
      var subArray=this.deserializeSub(typeInfo,dim-1,elements[i]);
      array.push(subArray);
    }
    return array;
  } else if(dim==1) {
    var array=new Array();
    if(elements!=null) {
      var deserializer=SOAP_TypeMapping.getSerializerByInfo(typeInfo.arrayType);
      for(var i=0;i<elements.length;i++) { 
        var val=deserializer.deserialize(typeInfo.arrayType,elements[i]);
        array.push(val);
      }
    }
    return array;
  } else throw new SOAP_SerializeEx("Illegal array dimension: "+dim,"SOAP_ArraySerializer.deserializeSub");
}

SOAP_ArraySerializer.prototype.deserialize=function(typeInfo,elements) {
  return this.deserializeSub(typeInfo,typeInfo.dimension,elements);
}


//*********************************
//SOAP_BeanSerializer(XML_QName type)
//*********************************
function SOAP_BeanSerializer(type) {
  SOAP_SimpleSerializer.call(this,type);
}
SOAP_BeanSerializer.extend(SOAP_SimpleSerializer);
SOAP_BeanSerializer.prototype.serialize=function(value,name,typeInfo,writer,ctx) {
  writer.startElement(name);
  
  for(var i=0;i<typeInfo.propNames.length;i++) {
    
    var propName=typeInfo.propNames[i];

    var propInfo=typeInfo.propToInfo[propName];
    var serializer=SOAP_TypeMapping.getSerializerByInfo(propInfo);
    var propVal=value[propName];
    if(!propVal && typeof propVal=='undefined') throw new SOAP_SerializeEx("Missing bean property: "+propName,"SOAP_BeanSerializer.serialize");
    serializer.serialize(propVal,propName,propInfo,writer,ctx);
  }
 
  writer.endElement(name);
}
SOAP_BeanSerializer.prototype.deserialize=function(typeInfo,element) {
  var obj=new Object();
  for(var i=0;i<typeInfo.propNames.length;i++) {
    var propName=typeInfo.propNames[i];
    var propInfo=typeInfo.propToInfo[propName];
    var items=XML_Utilities.getChildrenByName(element,propName);
    if(items.length>0) {
      var deserializer=SOAP_TypeMapping.getSerializerByInfo(propInfo);
      var val=null;
      if(propInfo instanceof SOAP_ArrayInfo) {
        val=deserializer.deserialize(propInfo,items);
      } else {
        val=deserializer.deserialize(propInfo,items[0]);
      }
      obj[propName]=val;
    } else {
      if(propInfo instanceof SOAP_ArrayInfo) obj[propName]=[];
      else obj[propName]=null;
    }
  }
  return obj;
}

//*********************************
//SOAP_ElementSerializer(XML_QName type)
//*********************************
function SOAP_ElementSerializer(type) {
  SOAP_SimpleSerializer.call(this,type);
}
SOAP_ElementSerializer.extend(SOAP_SimpleSerializer);
SOAP_ElementSerializer.prototype.serialize=function(value,name,typeInfo,writer,ctx) {
  writer.startElement(name);
  this.serializeSub(value,writer);
  writer.endElement(name);
}
SOAP_ElementSerializer.prototype.serializeSub=function(element,writer) {
  writer.startElement(element.nodeName);
  for(var j=0;j<element.attributes.length;j++) {
    var node=element.attributes.item(j);
    writer.writeAttribute(node.nodeName,node.nodeValue);
  }
  var nl=element.childNodes;
  if(nl!=null) {
    for(var i=0;i<nl.length;i++) {
      var node=nl[i];
      if(node.nodeType==1) this.serializeSub(node,writer);
      else if(node.nodeType==3) writer.writeChars(node.nodeValue);
    }
  }
  writer.endElement(element.nodeName);
}
SOAP_ElementSerializer.prototype.deserialize=function(typeInfo,element) {
  var nl=element.childNodes;
  if(nl!=null) {
    for(var i=0;i<nl.length;i++) {
      var node=nl[i];
      if(node.nodeType==1) return node;
    }
  }
}


//*********************************
//SOAP_TypeMapping()
//*********************************
function SOAP_TypeMapping() {
  this.mappings=new Array();
  this.init();
}

//init()
SOAP_TypeMapping.prototype.init=function() {
  this.mappings[XML_Types.XSD_BOOLEAN.hashKey()]=new SOAP_BooleanSerializer();
  var numSer=new SOAP_NumberSerializer();
  this.mappings[XML_Types.XSD_BYTE.hashKey()]=numSer;
  this.mappings[XML_Types.XSD_SHORT.hashKey()]=numSer;
  this.mappings[XML_Types.XSD_INT.hashKey()]=numSer;
  this.mappings[XML_Types.XSD_LONG.hashKey()]=numSer;
  var floatSer=new SOAP_FloatSerializer();  
  this.mappings[XML_Types.XSD_FLOAT.hashKey()]=floatSer;
  this.mappings[XML_Types.XSD_DOUBLE.hashKey()]=floatSer;
  this.mappings[XML_Types.XSD_STRING.hashKey()]=new SOAP_StringSerializer();
  this.mappings[XML_Types.XSD_DATETIME.hashKey()]=new SOAP_DateTimeSerializer();
  this.mappings["ARRAY"]=new SOAP_ArraySerializer();
  this.mappings["BEAN"]=new SOAP_BeanSerializer();
  this.mappings["ELEMENT"]=new SOAP_ElementSerializer();
}

//register(XML_QName xmlType,Serializer serializer)
SOAP_TypeMapping.prototype.register=function(xmlType,serializer) {
  this.mappings[xmlType.hashKey()]=serializer;
}

//Serializer getSerializer(XML_QName xmlType)
SOAP_TypeMapping.prototype.getSerializer=function(xmlType) {
  if(arguments.length==1) {
    var serializer=this.mappings[xmlType.hashKey()];
    if(serializer==null) throw "Can't find serializer for type '"+xmlType.toString()+"'";
    return serializer;
  } else throw new CORE_IllegalArgsEx("Wrong number of arguments","SOAP_TypeMapping.getSerializer");
}

//Serializer getSerializerByInfo(TypeInfo info)
SOAP_TypeMapping.prototype.getSerializerByInfo=function(info) {
  if(arguments.length==1) {
    var serializer=this.mappings[info.xmlType.hashKey()];
    if(serializer==null && (info instanceof SOAP_ArrayInfo)) serializer=this.mappings["ARRAY"];
    if(serializer==null && (info instanceof SOAP_BeanInfo)) serializer=this.mappings["BEAN"];
    if(serializer==null) throw "Can't find serializer for type '"+info.xmlType.toString()+"'";
    return serializer;
  } else throw new CORE_IllegalArgsEx("Wrong number of arguments","SOAP_TypeMapping.getSerializerByInfo");
}

var SOAP_TypeMapping=new SOAP_TypeMapping();


//*********************************
// SOAP_RPCSerializer(XML_QName opName,ArrayOfParameter params,values,...)
//*********************************
function SOAP_RPCSerializer(opName,params,targetNamespace,retTypeInfo,serCtx) {
  this.opName=opName;
  this.params=params;
  this.targetNamespace=targetNamespace;
  this.retTypeInfo=retTypeInfo;
  this.serCtx=serCtx;
}

SOAP_RPCSerializer.prototype.serialize=function(writer) {
  writer.startElement(new XML_QName(this.targetNamespace,this.opName));
  for(var i=0;i<this.params.length;i++) {
    var serializer=SOAP_TypeMapping.getSerializerByInfo(this.params[i].typeInfo);
    serializer.serialize(this.params[i].value,this.params[i].name,this.params[i].typeInfo,writer,this.serCtx);
  }
  writer.endElement(new XML_QName(this.targetNamespace,this.opName));
}

SOAP_RPCSerializer.prototype.deserialize=function(element) {
  if(this.retTypeInfo==null) return;
  var serializer=SOAP_TypeMapping.getSerializerByInfo(this.retTypeInfo);
  var retElems=element.getElementsByTagName("return");
  if(this.retTypeInfo instanceof SOAP_ArrayInfo) {
    if(retElems.length==0) return new Array();
    else return serializer.deserialize(this.retTypeInfo,retElems);
  } else {
    if(retElems.length==0) return null;
    return serializer.deserialize(this.retTypeInfo,retElems[0]);
  }
}


//*********************************
//SOAP_Param(String name,SOAP_TypeInfo typeInfo)
//SOAP_Param(String name,SOAP_TypeInfo typeInfo,parameterMode)
//*********************************
function SOAP_Param() {
  this.name=null;
  this.typeInfo=null;
  this.parameterMode=null;
  this.init(arguments);
  this.value=null;
  this.MODE_IN=0;
  this.MODE_INOUT=1;
  this.MODE_OUT=2;
}

SOAP_Param.prototype.init=function(args) {
  if(args.length==2) {
    this.name=args[0];
    this.typeInfo=args[1];
    this.parameterMode=this.MODE_IN;
  } else if(args.length==3) {
    this.name=args[0];
    this.typeInfo=args[1];
    this.parameterMode=args[2];
  } else throw new CORE_IllegalArgsEx("Wrong number of arguments","SOAP_Param.init");
}

SOAP_Param.prototype.setValue=function(value) {
  this.value=value;
}


//*********************************
// SOAP_Call()
//*********************************
function SOAP_Call() {
  this.endpoint=null;
  this.opName=null;
  this.params=new Array();
  this.targetNamespace=null;
  this.retTypeInfo=null;
  this.userCallback=null;
  this.userObject=null;
  this.requestID=null;
  this.request=null;
  this.style=null;
  this.use=null;
}

//setTargetEndpointAddress(address)
SOAP_Call.prototype.setTargetEndpointAddress=function() {
  if(arguments.length==1) {
    this.endpoint=arguments[0];
  }
}

SOAP_Call.prototype.setEncoding=function(style,use) {
  this.style=style;
  this.use=use;
}

SOAP_Call.prototype.setTargetNamespace=function(targetNamespace) {
  this.targetNamespace=targetNamespace;
}

SOAP_Call.prototype.setUserCallback=function(cb) {
  this.userCallback=cb;
}

SOAP_Call.prototype.setUserObject=function(cbObj) {
  this.userObject=cbObj;
}

SOAP_Call.prototype.setRequestID=function(id) {
  this.requestID=id;
}

//setOperationName(operationName)
SOAP_Call.prototype.setOperationName=function() {
  if(arguments.length==1) {
    this.opName=arguments[0];
  }
} 

//addParameter(paramName,typeInfo,parameterMode)
SOAP_Call.prototype.addParameter=function() {
  if(arguments.length==2) {
    var param=new SOAP_Param(arguments[0],arguments[1]);
    this.params.push(param);
  } else if(arguments.length==3) {
    var param=new SOAP_Param(arguments[0],arguments[1],arguments[2]);
    this.params.push(param);
  } else throw new CORE_IllegalArgsEx("Wrong number of arguments","SOAP_Call.addParameter");
}

//setReturnType(retTypeInfo)
SOAP_Call.prototype.setReturnType=function(retTypeInfo) {
  this.retTypeInfo=retTypeInfo;
}

//invoke()
//invoke(values,...)
//invoke(XML_QName operationName,values,...)
SOAP_Call.prototype.invoke=function() {
  
  var writer=new XML_Writer();
  var soapMsg=new SOAP_Message();
  
  var ind=0;
  if(arguments.length>0) {
    if(arguments[0] instanceof XML_QName) {
      this.setOperationName(arguments[0]);
      ind++;
    }
  }
  if(this.params.length!=arguments.length-ind) throw new CORE_IllegalArgsEx("Wrong number of arguments","SOAP_Call.invoke");
  for(var i=0;i<this.params.length;i++) this.params[i].setValue(arguments[i+ind]);
  
  var serCtx=new SOAP_SerializeCtx();
  serCtx.setEncoding(this.style,this.use);
  var rpc=new SOAP_RPCSerializer(this.opName,this.params,this.targetNamespace,this.retTypeInfo,serCtx);
  
  var bodyElem=new SOAP_BodyElement(rpc);
  soapMsg.getSoapPart().getEnvelope().getBody().addBodyElement(bodyElem);
  soapMsg.write(writer);
  
  var resDoc;
  if( !this.userCallback && !this.userObject ) {
    // sync
    this.request=new XML_Request('POST',this.endpoint);
    this.request.setRequestHeader("Content-Type","text/xml");
    resDoc=this.request.start(writer.xml); 
    return this.callback(resDoc);
  } else {
    // async
    try {
      this.request=new XML_Request( 'POST', this.endpoint, this.callback, this );
      this.request.setRequestHeader("Content-Type","text/xml");
      if(this.requestID==null) this.request.start(writer.xml);
      else this.request.start(writer.xml,this.requestID);
    } catch(ex) { }
  }
}

SOAP_Call.prototype.callback=function(xml,reqID) {
  try {
    var soapMsg=new SOAP_Message(xml);
    var fault=soapMsg.getSoapPart().getEnvelope().getBody().getFault();
    if(fault!=null) {
      var ex=new Error();
      var nl=XML_Utilities.getChildrenByName(fault.element,"detail");
      if(nl!=null && nl.length>0) {
        nl=XML_Utilities.getChildrenByName(nl[0],"exception");
        if(nl!=null && nl.length>0) {
          ex.name=nl[0].getAttribute("class");
        }
      }
      ex.message=fault.faultString;
      if(this.userCallback) this.userCallback(null,reqID,ex);
      else if(this.userObject) this.userObject[this.opName].call(this.userObject,null,reqID,ex);
      else throw ex;
    } else {
      var rpc=new SOAP_RPCSerializer( this.opName, null, this.targetNamespace, this.retTypeInfo);
      var res = rpc.deserialize(soapMsg.getSoapPart().getEnvelope().getBody().element);
      if(this.userCallback) this.userCallback(res,reqID,null);
      else if(this.userObject) this.userObject[this.opName].call(this.userObject,res,reqID,null);
      else return res;
    }
  } catch(ex) {
    if(this.userCallback) this.userCallback(null,reqID,ex);
    else if(this.userObject) this.userObject[this.opName].call(this.userObject,null,reqID,ex);
    else throw ex;
  }
};


//*********************************
// SOAP_Message()
// SOAP_Message(Document xml)
//*********************************
function SOAP_Message() {
  if(arguments.length==0) {
    this.soapPart=new SOAP_Part();
  } else if(arguments.length==1) {
    this.soapPart=new SOAP_Part(arguments[0]);
  } else throw new CORE_WrongArgNoEx("","SOAP_Message");
}

//SOAP_Part getSoapPart()
SOAP_Message.prototype.getSoapPart=function() {
  return this.soapPart;
}

//write(XML_Writer writer) {
SOAP_Message.prototype.write=function(writer) {
  this.soapPart.write(writer);
}

//*********************************
// SOAP_Part()
// SOAP_Part(Document xml)
//*********************************
function SOAP_Part() {
  if(arguments.length==0) {
    this.envelope=new SOAP_Envelope();
  } else if(arguments.length==1) {
    var e=XML_Utilities.getChildrenByNameNS(arguments[0],XML_NS_SOAPENV,"Envelope")[0];
    if(e==null) throw new SOAP_Exception("NO SOAP MESSAGE","SOAP_Part");
    this.envelope=new SOAP_Envelope(e);
  } else throw new CORE_WrongArgNoEx("","SOAP_Part");
}

//SOAP_Envelope getEnvelope()
SOAP_Part.prototype.getEnvelope=function() {
  return this.envelope;
}

//write(XML_Writer writer) {
SOAP_Part.prototype.write=function(writer) {
  this.envelope.write(writer);
}


//*********************************
// SOAP_Envelope()
// SOAP_Envelope(Element elem)
//*********************************
function SOAP_Envelope() {
  if(arguments.length==0) {
    this.header=null;
    this.body=new SOAP_Body();
    this.element=null;
  } else if(arguments.length==1) {
    var e=XML_Utilities.getChildrenByNameNS(arguments[0],XML_NS_SOAPENV,"Header")[0];
    if(e!=null) this.header=new SOAP_Header(e);
    else this.header=null;
    e=XML_Utilities.getChildrenByNameNS(arguments[0],XML_NS_SOAPENV,"Body")[0];
    if(e!=null) this.body=new SOAP_Body(e);
    else throw new SOAP_Exception("NO MESSAGE BODY","SOAP_Envelope");
    this.element=arguments[0];
  } else throw new CORE_WrongArgNoEx("","SOAP_Envelope");
}

//write(XML_Writer writer) {
SOAP_Envelope.prototype.write=function(writer) {
  var envName=new XML_QName(XML_NS_SOAPENV,"Envelope");
  writer.startElement(envName);
  writer.writeNamespaceDeclaration(XML_NS_XSI);
  writer.writeNamespaceDeclaration(XML_NS_XSD);
  if(this.header!=null) this.header.write(writer);
  this.body.write(writer);
  writer.endElement(envName);
}

//SOAP_Header addHeader()
SOAP_Envelope.prototype.addHeader=function() {
  if(this.header!=null) throw new SOAP_Exception("Message already contains header","SOAP_Envelope");
  this.header=new SOAP_Header();
  return this.header;
}

//SOAP_Header getHeader()
SOAP_Envelope.prototype.getHeader=function() {
  return this.header;
}

//SOAP_Body getBody()
SOAP_Envelope.prototype.getBody=function() {
  return this.body;
}

//*********************************
// SOAP_BodyElement(serializer)
//*********************************
function SOAP_BodyElement(serializer) {
  this.serializer=serializer;
}

SOAP_BodyElement.prototype.write=function(writer) {
  this.serializer.serialize(writer);
}

//*********************************
// SOAP_Body()
// SOAP_Body(Element elem)
//*********************************
function SOAP_Body() {
  if(arguments.length==0) {
    this.fault=null;
    this.bodyElems=new Array();
    this.element=null;
  } else if(arguments.length==1) {
    var e=XML_Utilities.getChildrenByNameNS(arguments[0],XML_NS_SOAPENV,"Fault")[0];
    if(e!=null) this.fault=new SOAP_Fault(e);
    this.element=arguments[0];
  } else throw new CORE_WrongArgNoEx("","SOAP_Envelope");
}

//addBodyElement(SOAP_BodyElement bodyElem)
SOAP_Body.prototype.addBodyElement=function(bodyElem) {
  this.bodyElems.push(bodyElem);  
}

//SOAP_Fault getFault()
SOAP_Body.prototype.getFault=function(fault) {
  return this.fault;
}

//setFault(SOAP_Fault fault) 
SOAP_Body.prototype.setFault=function(fault) {
  this.fault=fault;
}

//write(XML_Writer writer) {
SOAP_Body.prototype.write=function(writer) {
  var bodyName=new XML_QName(XML_NS_SOAPENV,"Body");
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
// SOAP_HeaderElement(XML_QName qname)
// SOAP_HeaderElement(Element elem);
//*********************************
function SOAP_HeaderElement(arg) {
  if(arg instanceof XML_QName) {
    this.qname=arg;
    this.text=null;
  } else {
    this.qname=new XML_QName(arg.nodeName);
    this.text=arg.nodeValue;
  }
}

SOAP_HeaderElement.prototype.write=function(writer) {
  writer.startElement(this.qname);
  if(this.text!=null) writer.writeChars(this.text);
  writer.endElement(this.qname);
}

SOAP_HeaderElement.prototype.setText=function(text) {
  this.text=text;
}

//*********************************
// SOAP_Header()
// SOAP_Header(Element elem)
//*********************************
function SOAP_Header() {
  if(arguments.length==0) {
    this.headerElems=new Array();
  } else if(arguments.length==1) {
    this.headerElems=new Array();
    var nodes=XML_Utilities.getChildElements(arguments[0]);
    for(var i=0;i<nodes.length;i++) {
      this.headerElems.push(new SOAP_HeaderElement(nodes[i]));
    }
  } else throw new CORE_WrongArgNoEx("","SOAP_Envelope");
}

//SOAP_HeaderElement addHeaderElement(XML_QName)
SOAP_Header.prototype.addHeaderElement=function(qname) {
  var elem=new SOAP_HeaderElement(qname);
  this.headerElems.push(elem);
  return elem;
}

SOAP_Header.prototype.getHeaderElement=function(name) {
  for(var i=0;i<this.headerElems.length;i++) {
    if(this.headerElems[i].qname.localpart==name) return this.headerElems[i];
  }
}

//write(XML_Writer writer) {
SOAP_Header.prototype.write=function(writer) {
  var headerName=new XML_QName(XML_NS_SOAPENV,"Header");
  writer.startElement(headerName);
  for(var i=0;i<this.headerElems.length;i++) {
    this.headerElems[i].write(writer);
  }
  writer.endElement(headerName);
}

//*********************************
// SOAP_Fault
// SOAP_Fault(Element elem)
//*********************************
function SOAP_Fault() {
  if(arguments.length==0) {
    this.faultCode=null;
    this.faultString=null;
    this.element=null;
  } else if(arguments.length==1) {
    var nl=XML_Utilities.getChildrenByName(arguments[0],"faultcode");
    if(nl!=null && nl.length>0) this.faultCode=XML_Utilities.getText(nl[0]);
    nl=XML_Utilities.getChildrenByName(arguments[0],"faultstring");
    if(nl!=null && nl.length>0) this.faultString=XML_Utilities.getText(nl[0]);
    this.element=arguments[0];
  } else throw new CORE_WrongArgNoEx("","SOAP_Fault");
}
SOAP_Fault.prototype.toString=function() {
  var str="";
  if(this.faultCode!=null) str+=": "+this.faultCode;
  if(this.faultString!=null) str+=": "+this.faultString;
  return str;
}


//*********************************
// SOAP_TypeInfo(XML_QName xmlType)
//*********************************
function SOAP_TypeInfo(xmlType) {
  this.xmlType=xmlType;
}

//*********************************
// SOAP_ArrayInfo(XML_QName xmlType)
// SOAP_ArrayInfo(XML_QName xmlType,TypeInfo arrayType,Number dimension)
//*********************************
function SOAP_ArrayInfo(xmlType,arrayType,dimension) {
  SOAP_TypeInfo.call(this,xmlType);
  if(arguments.length==3) {
    this.dimension=dimension;
    this.arrayType=arrayType;
  }
}
SOAP_ArrayInfo.extend(SOAP_TypeInfo);
SOAP_ArrayInfo.prototype.populate=function(arrayType,dimension) {
  this.arrayType=arrayType;
  this.dimension=dimension;
}

//*********************************
// SOAP_BeanInfo(XML_QName xmlType)
// SOAP_BeanInfo(XML_QName xmlType,Array props)
//*********************************
function SOAP_BeanInfo(xmlType,props) {
  SOAP_TypeInfo.call(this,xmlType);
  this.props=null;
  this.propNames=null;
  this.propToInfo=null;
  if(arguments.length==2) {
    this.props=props;
    this.init();
  }
}
SOAP_BeanInfo.extend(SOAP_TypeInfo);
SOAP_BeanInfo.prototype.init=function() {
  this.propNames=new Array();
  this.propToInfo=new Array();
  for(var i=0;i<this.props.length;i=i+2) {
    this.propNames.push(this.props[i]);
    this.propToInfo[this.props[i]]=this.props[i+1];
  }
}
SOAP_BeanInfo.prototype.populate=function(props) {
  this.props=props;
  this.init();
}

//*********************************
// SOAP_Stub
//*********************************
function SOAP_Stub() {
  this._requestPath="";
  this._service="";
  this._targetNamespace="";
  this._typeInfos=new Array();
}

SOAP_Stub.prototype._createCall=function() {
  var call=new SOAP_Call();
  call.setTargetEndpointAddress(this.getURI());
  if(this._cbObj!=null) call.setUserObject(this._cbObj);
  return call;
}

SOAP_Stub.prototype._extractCallback=function(call,args,expLen) {
  var argLen=args.length;
  if(argLen==expLen+1) {
    if(typeof args[argLen-1]=="function" || typeof args[argLen-1]=="object" ) call.setUserCallback(args[argLen-1]);
    else call.setRequestID(args[argLen-1]);
  } else if(argLen==expLen+2 && (typeof args[argLen-2]=="function" || typeof args[argLen-2]=="object") && typeof args[argLen-1]=="string") {
    call.setUserCallback(args[argLen-2]);
    call.setRequestID(args[argLen-1]);
  } else if(argLen!=expLen) throw new CORE_IllegalArgsEx("Wrong number of arguments","SOAP_Stub._extractCallback");
}

SOAP_Stub.prototype._setRequestPath=function(requestPath) {
  this._requestPath=requestPath;
}

SOAP_Stub.prototype._setService=function(service) {
  this._service=service;
}

SOAP_Stub.prototype.getURI=function() {
   if(this._uri==null) {
	  var session="";
      var matches=window.location.href.match(/;jsessionid=[A-Z0-9]+(\.[a-zA-Z0-9]+)?/);
      if(matches) session=matches[0];
      var reqpath=this._requestPath+"/"+this._service;
      this._uri=window.location.protocol+"//"+window.location.host+reqpath+session;
   }
   return this._uri;
}
