function consoleReset() {
    var p=document.getElementById("console");
    if(p!=null && p.childNodes!=null) {
        var len=p.childNodes.length;
        for(var i=0;i<len;i++) {
         p.removeChild(p.childNodes[0]);
        }
    }
}

function consolePrint(method,time,error) {
    var p=document.createElement("div");
    var t=document.createTextNode(method+" ("+time+"ms) ");
    p.appendChild(t);
    if(error!=undefined) {
        var msg="";
        if(error.message) msg=error.message;
                else msg=error.toString();
        var s=document.createElement("span");
        s.setAttribute("style","color:red;");
        var st=document.createTextNode(msg);
        s.appendChild(st);
        p.appendChild(s);
    }
    var out=document.getElementById("console");
    out.appendChild(p);
}

function arrayEquals(a1,a2) {
    if(a1.length!=a2.length) return false;
    for(var i=0;i<a1.length;a1++) {
        if((a1[i] instanceof Array) && (a2[i] instanceof Array)) {
            var equal=arrayEquals(a1[i],a2[i]);
            if(!equal) return false;
        } else {
            if(!equals(a1[i],a2[i])) return false; 
        }
    }
    return true;
}

function beanEquals(b1,b2) {
    for(var prop in b1) {
        var val1=b1[prop];
        var val2=b2[prop];
        var equal=equals(val1,val2);
        if(!equal && prop!='javaClass') return false;
    }
    for(var prop in b2) {
        var val1=b1[prop];
        var val2=b2[prop];
        var equal=equals(val1,val2);
        if(!equal && prop!='javaClass') return false;
    }
    return true;
}

function elementEquals(e1,e2) {
    if(e1.nodeType!=1 || (e1.nodeType!=e2.nodeType)) return false;
    if(e1.nodeName!=e2.nodeName) return false;
    //TODO: attribute/text/recursive check
    return true;
}

function equals(obj1,obj2) {
    var type1=typeof obj1;
    var type2=typeof obj2;
    if(type1!=type2) return false;
    if(type1=="string" || type1=="number" || type1=="boolean" || type1=="undefined" || type1=="null") {
        return obj1==obj2;
    }
    if((obj1 instanceof Array) || (obj2 instanceof Array)) {
      if(!((obj1 instanceof Array) && (obj2 instanceof Array))) return false;
        return arrayEquals(obj1,obj2);
    }
    if((obj1 instanceof Date) || (obj2 instanceof Date)) {
      if(!((obj1 instanceof Date) && (obj2 instanceof Date))) return false;
        return obj1.toGMTString()==obj2.toGMTString(); 
    }
    return beanEquals(obj1,obj2);
}


var wsTypeTest=new WS_TypeTest();
var jwsTypeTest=new JWS_TypeTest();

var timer=new Timer();

function serviceCall() {
    consoleReset();
    timer.reset();
    timer.start();
    var ws=soapEnabled()?wsTypeTest:jwsTypeTest;
    
    var total1=(new Date()).getTime();
        
    //echoByte
    var t1=(new Date()).getTime();
    try {
        var byteVal=parseInt(1);
        var resVal=ws.echoByte(byteVal);
        var t2=(new Date()).getTime();
        if(resVal!=byteVal) throw "Wrong result";
        consolePrint("echoByte",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoByte",(t2-t1),ex);
    }
        
    //echoByteObj
    var t1=(new Date()).getTime();
    try {
        var byteVal=parseInt(1);
        var resVal=ws.echoByteObj(byteVal);
        var t2=(new Date()).getTime();
        if(resVal!=byteVal) throw "Wrong result";
        consolePrint("echoByteObj",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoByteObj",(t2-t1),ex);
    }

    if(!soapEnabled()) {
    
    //echoChar
    var t1=(new Date()).getTime();
    try {
        var charVal='\"';
        var resVal=ws.echoChar(charVal);
        var t2=(new Date()).getTime();
        if(resVal!=charVal) throw "Wrong result";
        consolePrint("echoChar",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoChar",(t2-t1),ex);
    }
        
    //echoCharacter
    var t1=(new Date()).getTime();
    try {
        var charVal='\'';
        var resVal=ws.echoCharacter(charVal);
        var t2=(new Date()).getTime();
        if(resVal!=charVal) throw "Wrong result";
        consolePrint("echoCharacter",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoCharacter",(t2-t1),ex);
    }
    
    }
    
    //echoShort
    var t1=(new Date()).getTime();
    try {
        var shortVal=parseInt(1);
        var resVal=ws.echoShort(shortVal);
        var t2=(new Date()).getTime();
        if(resVal!=shortVal) throw "Wrong result";
        consolePrint("echoShort",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoShort",(t2-t1),ex);
    }
        
    //echoShortObj
    var t1=(new Date()).getTime();
    try {
        var shortVal=parseInt(1);
        var resVal=ws.echoShortObj(shortVal);
        var t2=(new Date()).getTime();
        if(resVal!=shortVal) throw "Wrong result";
        consolePrint("echoShortObj",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoShortObj",(t2-t1),ex);
    }

    //echoInt
    var t1=(new Date()).getTime();
    try {
        var intVal=parseInt(1);
        var resVal=ws.echoInt(intVal);
        var t2=(new Date()).getTime();
        if(resVal!=intVal) throw "Wrong result";
        consolePrint("echoInt",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoInt",(t2-t1),ex);
    }
        
    //echoIntObj
    var t1=(new Date()).getTime();
    try {
        var intVal=parseInt(1);
        var resVal=ws.echoIntObj(intVal);
        var t2=(new Date()).getTime();
        if(resVal!=intVal) throw "Wrong result";
        consolePrint("echoIntObj",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoIntObj",(t2-t1),ex);
    }
        
    //echoIntObj(null)
    var t1=(new Date()).getTime();
    try {
        var intVal=null;
        var resVal=ws.echoIntObj(intVal);
        var t2=(new Date()).getTime();
        if(resVal!=intVal) throw "Wrong result";
        consolePrint("echoIntObj(null)",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoIntObj(null)",(t2-t1),ex);
    }

    //echoIntArray
    t1=(new Date()).getTime();
    try {
        var intVals=new Array(1,2);
        var resVals=ws.echoIntArray(intVals);
        var t2=(new Date()).getTime();
        if(!arrayEquals(intVals,resVals)) throw "Wrong result";
        consolePrint("echoIntArray",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoIntArray",(t2-t1),ex);
    }
        
    //echoLong
    t1=(new Date()).getTime();
    try {
        var longVal=parseInt(2);
        var resVal=ws.echoLong(longVal);
        var t2=(new Date()).getTime();
        if(resVal!=longVal) throw "Wrong result";
        consolePrint("echoLong",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoLong",(t2-t1),ex);
    }
        
    //echoLongObj
    t1=(new Date()).getTime();
    try {
        var longVal=parseInt(2);
        var resVal=ws.echoLongObj(longVal);
        var t2=(new Date()).getTime();
        if(resVal!=longVal) throw "Wrong result";
        consolePrint("echoLongObj",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoLongObj",(t2-t1),ex);
    }
        
    //echoLongArray
    t1=(new Date()).getTime();
    try {
        var longVals=new Array(1,2);
        var resVals=ws.echoLongArray(longVals);
        var t2=(new Date()).getTime();
        if(!arrayEquals(longVals,resVals)) throw "Wrong result";
        consolePrint("echoLongArray",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoLongArray",(t2-t1),ex);
    }
    
    //echoFloat
    t1=(new Date()).getTime();
    try {
        var floatVal=parseFloat(2.1);
        var resVal=ws.echoFloat(floatVal);
        var t2=(new Date()).getTime();
        if(resVal!=floatVal) throw "Wrong result";
        consolePrint("echoFloat",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoFloat",(t2-t1),ex);
    }
    
    //echoFloatObj
    t1=(new Date()).getTime();
    try {
        var floatVal=parseFloat(2.1);
        var resVal=ws.echoFloatObj(floatVal);
        var t2=(new Date()).getTime();
        if(resVal!=floatVal) throw "Wrong result";
        consolePrint("echoFloatObj",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoFloatObj",(t2-t1),ex);
    }
  
    //echoFloatObj(null)
    t1=(new Date()).getTime();
    try {
        var floatVal=null;
        var resVal=ws.echoFloatObj(floatVal);
        var t2=(new Date()).getTime();
        if(resVal!=floatVal) throw "Wrong result";
        consolePrint("echoFloatObj(null)",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoFloatObj(null)",(t2-t1),ex);
    }
    
    //echoFloatArray
    t1=(new Date()).getTime();
    try {
        var floatVals=new Array(1.1,2.2);
        var resVals=ws.echoFloatArray(floatVals);
        var t2=(new Date()).getTime();
        if(!arrayEquals(floatVals,resVals)) throw "Wrong result";
        consolePrint("echoFloatArray",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoFloatArray",(t2-t1),ex);
    }
        
    //echoDouble
    t1=(new Date()).getTime();
    try {
        var doubleVal=parseFloat(2.1);
        var resVal=ws.echoDouble(doubleVal);
        var t2=(new Date()).getTime();
        if(resVal!=doubleVal) throw "Wrong result";
        consolePrint("echoDouble",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoDouble",(t2-t1),ex);
    }
    
    //echoDoubleObj
    t1=(new Date()).getTime();
    try {
        var doubleVal=parseFloat(2.1);
        var resVal=ws.echoDoubleObj(doubleVal);
        var t2=(new Date()).getTime();
        if(resVal!=doubleVal) throw "Wrong result";
        consolePrint("echoDoubleObj",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoDoubleObj",(t2-t1),ex);
    }
    
    //echoString
    t1=(new Date()).getTime();
    try {
        var strVal="test";
        var resVal=ws.echoString(strVal);
        var t2=(new Date()).getTime();
        if(resVal!=strVal) throw "Wrong result";
        consolePrint("echoString",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
       consolePrint("echoString",(t2-t1),ex);
    }
  
    //echoString(null)
    t1=(new Date()).getTime();
    try {
       var strVal=null;
       var resVal=ws.echoString(strVal);
       var t2=(new Date()).getTime();
       if(resVal!=strVal) throw "Wrong result";
       consolePrint("echoString(null)",(t2-t1));
    } catch(ex) {
       var t2=(new Date()).getTime();
       consolePrint("echoString(null)",(t2-t1),ex);
    }
       
   //echoStringArray
    t1=(new Date()).getTime();
    try {
        var strVals=new Array("aaa","bbb");
        var resVals=ws.echoStringArray(strVals);
        var t2=(new Date()).getTime();
        if(!arrayEquals(strVals,resVals)) throw "Wrong result";
        consolePrint("echoStringArray",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoStringArray",(t2-t1),ex);
    }
    
    //echoStringMultiArray
    t1=(new Date()).getTime();
    try {
        var strVals=new Array(new Array("aaa","bbb"),new Array("ccc","ddd"));
        var resVals=ws.echoStringMultiArray(strVals);
        var t2=(new Date()).getTime();
        if(!arrayEquals(strVals,resVals)) throw "Wrong result";
        consolePrint("echoStringMultiArray",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoStringMultiArray",(t2-t1),ex);
    }

    //echoStringArray(empty)
    t1=(new Date()).getTime();
    try {
      var strVals=new Array();
      var resVals=ws.echoStringArray(strVals);
      var t2=(new Date()).getTime();
      if(!arrayEquals(strVals,resVals)) throw "Wrong result";
      consolePrint("echoStringArray(empty)",(t2-t1));
    } catch(ex) {
      var t2=(new Date()).getTime();
      consolePrint("echoStringArray(empty)",(t2-t1),ex);
    }

    //echoBoolean
    t1=(new Date()).getTime();
    try {
        var boolVal=true;
        var resVal=ws.echoBoolean(boolVal);
        var t2=(new Date()).getTime();
        if(resVal!=boolVal) throw "Wrong result";
        consolePrint("echoBoolean",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
       consolePrint("echoBoolean",(t2-t1),ex);
    }
   
    //echoBooleanObj
    t1=(new Date()).getTime();
    try {
        var boolVal=true;
        var resVal=ws.echoBooleanObj(boolVal);
        var t2=(new Date()).getTime();
        if(resVal!=boolVal) throw "Wrong result";
        consolePrint("echoBooleanObj",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
       consolePrint("echoBooleanObj",(t2-t1),ex);
    }
      
    //echoBooleanObj(null)
    t1=(new Date()).getTime();
    try {
      var boolVal=null;
      var resVal=ws.echoBooleanObj(boolVal);
      var t2=(new Date()).getTime();
      if(resVal!=boolVal) throw "Wrong result";
      consolePrint("echoBooleanObj(null)",(t2-t1));
    } catch(ex) {
      var t2=(new Date()).getTime();
      consolePrint("echoBooleanObj(null)",(t2-t1),ex);
    }
   
    //echoBooleanArray
    t1=(new Date()).getTime();
    try {
        var boolVals=new Array(true,false);
        var resVals=ws.echoBooleanArray(boolVals);
        var t2=(new Date()).getTime();
        if(!arrayEquals(boolVals,resVals)) throw "Wrong result";        
        consolePrint("echoBooleanArray",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoBooleanArray",(t2-t1),ex);
    }

    //echoCalendar
    t1=(new Date()).getTime();
    try {
      var dateVal=new Date();
      var resVal=ws.echoCalendar(dateVal);
      var t2=(new Date()).getTime();
      if(!equals(resVal,dateVal)) throw "Wrong result";
      consolePrint("echoCalendar",(t2-t1));
    } catch(ex) {
      var t2=(new Date()).getTime();
      consolePrint("echoCalendar",(t2-t1),ex);
    }
  
    //echoCalendar(null)
    t1=(new Date()).getTime();
    try {
      var dateVal=null;
      var resVal=ws.echoCalendar(dateVal);
      var t2=(new Date()).getTime();
      if(!equals(resVal,dateVal)) throw "Wrong result";
      consolePrint("echoCalendar(null)",(t2-t1));
    } catch(ex) {
      var t2=(new Date()).getTime();
      consolePrint("echoCalendar(null)",(t2-t1),ex);
    }

    //echoCalendarArray
    t1=(new Date()).getTime();
    try {
      var dateVals=new Array(new Date(),new Date());
      var resVals=ws.echoCalendarArray(dateVals);
      var t2=(new Date()).getTime();
      if(!arrayEquals(dateVals,resVals)) throw "Wrong result";    
      consolePrint("echoCalendarArray",(t2-t1));
    } catch(ex) {
      var t2=(new Date()).getTime();
      consolePrint("echoCalendarArray",(t2-t1),ex);
    }

    //echoDate
    t1=(new Date()).getTime();
    try {
        var dateVal=new Date();
        var resVal=ws.echoDate(dateVal);
        var t2=(new Date()).getTime();
        if(!equals(resVal,dateVal)) throw "Wrong result";
        consolePrint("echoDate",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
       consolePrint("echoDate",(t2-t1),ex);
    }

    //echoDataBean
    t1=(new Date()).getTime();
    try {
        var bean=new Object();
        bean["date"]=new Date();
        bean["floatVals"]=new Array(1.2,2.1);
        bean["intVal"]=2;
        bean["name"]="TestBean";
        bean["children"]=new Array();
        bean["boolVal"]=true;
        var resBean=ws.echoDataBean(bean);
        var t2=(new Date()).getTime();
        if(!equals(resBean,bean)) throw "Wrong result";
        consolePrint("echoDataBean",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
       consolePrint("echoDataBean",(t2-t1),ex);
    }    

    //echoDataBean(Null-properties)
    t1=(new Date()).getTime();
    try {
      var bean=new Object();
      bean["date"]=null;
      bean["floatVals"]=new Array(1.2,2.1);
      bean["intVal"]=2;
      bean["name"]=null;
      bean["children"]=new Array();
      bean["boolVal"]=true;
      var resBean=ws.echoDataBean(bean);
      var t2=(new Date()).getTime();
      if(!equals(resBean,bean)) throw "Wrong result";
      consolePrint("echoDataBean(Null-properties)",(t2-t1));
    } catch(ex) {
      var t2=(new Date()).getTime();
      consolePrint("echoDataBean(Null-properties)",(t2-t1),ex);
    }  

    //echoDataBeanArray
    t1=(new Date()).getTime();
    try {
        var bean1=new Object();
        bean1["date"]=new Date();
        bean1["floatVals"]=new Array(1.2,2.1);
        bean1["intVal"]=2;
        bean1["name"]="TestBean1";
        bean1["children"]=new Array();
        bean1["boolVal"]=true;
        var bean2=new Object();
        bean2["date"]=new Date();
        bean2["floatVals"]=new Array(1.2,2.1);
        bean2["intVal"]=2;
        bean2["name"]="TestBean2";
        bean2["children"]=new Array();
        bean2["boolVal"]=true;
        var bean3=new Object();
        bean3["date"]=new Date();
        bean3["floatVals"]=new Array(1.2,2.1);
        bean3["intVal"]=2;
        bean3["name"]="TestBean3";
        bean3["children"]=new Array(bean1,bean2);
        bean3["boolVal"]=true;
        var beans=new Array(bean3);
        var resBeans=ws.echoDataBeanArray(beans);
        var t2=(new Date()).getTime();
        if(!equals(resBeans,beans)) throw "Wrong result";
        consolePrint("echoDataBeanArray",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
       consolePrint("echoDataBeanArray",(t2-t1),ex);
    }    
   
    //echoElement
    /*
    t1=(new Date()).getTime();
    try {
       var doc=null;
       var elem=null;
    try {
             if(document.implementation && document.implementation.createDocument) 
            doc=document.implementation.createDocument("","",null);
    } catch(dx) {}
    if(doc==null) {
        try {
            if(window.ActiveXObject)
                        doc=new ActiveXObject("MSXML.DomDocument");
        } catch(dx) {}
    }
    if(doc==null) throw "Document creation not supported by browser";
     elem=doc.createElement("test");
     var sub=doc.createElement("foo");
     elem.appendChild(sub);
     sub.setAttribute("id","dafdfd");
     var txt=doc.createTextNode("asdfghjkl??");
     sub.appendChild(txt);
        var resElem=ws.echoElement(elem);
        var t2=(new Date()).getTime();
        if(!elementEquals(resElem,elem)) throw "Wrong result";
        consolePrint("echoElement",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
       consolePrint("echoElement",(t2-t1),ex);
    }
    */
   
    //echoStringList
    t1=(new Date()).getTime();
    try {
        var strVals=["aaa","bbb"];
        var resVals=ws.echoStringList(strVals);
        var t2=(new Date()).getTime();
        if(!arrayEquals(strVals,resVals)) throw "Wrong result";
        consolePrint("echoStringList",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
        consolePrint("echoStringList",(t2-t1),ex);
    }
  
    //echoDataBeanList
    t1=(new Date()).getTime();
    try {
      var bean1=new Object();
      bean1["date"]=new Date();
      bean1["floatVals"]=new Array(1.2,2.1);
      bean1["intVal"]=2;
      bean1["name"]="TestBean1";
      bean1["children"]=new Array();
      bean1["boolVal"]=true;
      var bean2=new Object();
      bean2["date"]=new Date();
      bean2["floatVals"]=new Array(1.2,2.1);
      bean2["intVal"]=2;
      bean2["name"]="TestBean2";
      bean2["children"]=new Array();
      bean2["boolVal"]=true;
      var bean3=new Object();
      bean3["date"]=new Date();
      bean3["floatVals"]=new Array(1.2,2.1);
      bean3["intVal"]=2;
      bean3["name"]="TestBean3";
      bean3["children"]=new Array(bean1,bean2);
      bean3["boolVal"]=true;
      var beans=new Array(bean3);
      var resBeans=ws.echoDataBeanList(beans);
      var t2=(new Date()).getTime();
      if(!equals(resBeans,beans)) throw "Wrong result";
      consolePrint("echoDataBeanList",(t2-t1));
    } catch(ex) {
      var t2=(new Date()).getTime();
      consolePrint("echoDataBeanList",(t2-t1),ex);
    }  
  
    //echoStringMap
    t1=(new Date()).getTime();
    try {
      var strVals={"key1":"val1","key2":"val2"};
      var resVals=ws.echoStringMap(strVals);
      var t2=(new Date()).getTime();
      if(!equals(strVals,resVals)) throw "Wrong result";
      consolePrint("echoStringMap",(t2-t1));
    } catch(ex) {
      var t2=(new Date()).getTime();
      consolePrint("echoStringMap",(t2-t1),ex);
    }
   
    //echoDataBeanMap
    t1=(new Date()).getTime();
    try {
      var bean1=new Object();
      bean1["date"]=new Date();
      bean1["floatVals"]=new Array(1.2,2.1);
      bean1["intVal"]=2;
      bean1["name"]="TestBean1";
      bean1["children"]=new Array();
      bean1["boolVal"]=true;
      var bean2=new Object();
      bean2["date"]=new Date();
      bean2["floatVals"]=new Array(1.2,2.1);
      bean2["intVal"]=2;
      bean2["name"]="TestBean2";
      bean2["children"]=new Array();
      bean2["boolVal"]=true;
      var bean3=new Object();
      bean3["date"]=new Date();
      bean3["floatVals"]=new Array(1.2,2.1);
      bean3["intVal"]=2;
      bean3["name"]="TestBean3";
      bean3["children"]=new Array(bean1,bean2);
      bean3["boolVal"]=true;
      var beans={"bean1":bean1,"bean2":bean2,"bean3":bean3};
      var resBeans=ws.echoDataBeanMap(beans);
      var t2=(new Date()).getTime();
      if(!equals(resBeans,beans)) throw "Wrong result";
      consolePrint("echoDataBeanMap",(t2-t1));
    } catch(ex) {
      var t2=(new Date()).getTime();
      consolePrint("echoDataBeanMap",(t2-t1),ex);
    }  
  
    //echoBeanArray
    t1=(new Date()).getTime();
    try {
      var bean1=new Object();
      bean1["text"]="text1";
      bean1["value"]=1;
      bean1["javaClass"]="de.schlund.pfixcore.example.webservices.BeanImpl";
      var bean2=new Object();
      bean2["text"]="text2";
      bean2["value"]=2;
      bean2["enabled"]=true;
      bean2["javaClass"]="de.schlund.pfixcore.example.webservices.BeanSubImpl";
      var beans=[bean1,bean2];
      var resBeans=ws.echoBeanArray(beans);
      var t2=(new Date()).getTime();
      if(!equals(resBeans,beans)) throw "Wrong result";
      consolePrint("echoBeanArray",(t2-t1));
    } catch(ex) {
      var t2=(new Date()).getTime();
      consolePrint("echoBeanArray",(t2-t1),ex);
    }  
    
    //echoEnum
    t1=(new Date()).getTime();
    try {
        var enumVal="BLUE";
        var resVal=ws.echoEnum(enumVal);
        var t2=(new Date()).getTime();
        if(resVal!=enumVal) throw "Wrong result";
        consolePrint("echoEnum",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
       consolePrint("echoEnum",(t2-t1),ex);
    }

    //echoEnumBean
    t1=(new Date()).getTime();
    try {
        var bean=new Object();
        bean["innerTestEnum"]="BLUE";
        bean["testEnum"]="RED";
        var resBean=ws.echoEnumBean(bean);
        var t2=(new Date()).getTime();
        if(!equals(resBean,bean)) throw "Wrong result";
        consolePrint("echoEnumBean",(t2-t1));
    } catch(ex) {
        var t2=(new Date()).getTime();
       consolePrint("echoEnumBean",(t2-t1),ex);
    }    

    var total2=(new Date()).getTime();
       
    timer.stop();
    printTime(timer.getTime());

}
