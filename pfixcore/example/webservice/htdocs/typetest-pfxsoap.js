function pfxsoapReset() {
	var p=document.getElementById("out");
	if(p!=null && p.childNodes!=null) {
		var len=p.childNodes.length;
		for(var i=0;i<len;i++) {
		 p.removeChild(p.childNodes[0]);
		}
	}
}

function pfxsoapPrint(method,time,error) {
	var p=document.createElement("p");
	var t=document.createTextNode(method+" ("+time+"ms) ");
	p.appendChild(t);
	if(error!=undefined) {
		var s=document.createElement("span");
		s.setAttribute("style","color:red;");
		var st=document.createTextNode(error);
		s.appendChild(st);
		p.appendChild(s);
	}
	var out=document.getElementById("out");
	out.appendChild(p);
}

function pfxsoapPrintTime(time) {
	document.getElementById('pfxsoap_time').innerHTML=time;
}

function pfxsoapPrintError(msg,time) {
	document.getElementById('pfxsoap_error').innerHTML=msg;
	document.getElementById('pfxsoap_time').innerHTML=time;
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
		if(!equal) return false;
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
	if((obj1 instanceof Array) && (obj2 instanceof Array)) {
		return arrayEquals(obj1,obj2);
	}
	if((obj1 instanceof Date) && (obj2 instanceof Date)) {
		return obj1.toGMTString()==obj2.toGMTString(); 
	}
	return beanEquals(obj1,obj2);
}

var wsType=new wsTypeTest();

function pfxsoapCallback(result,exception) {
	var d2=new Date();
   var t2=d2.getTime();
   var t=t2-t1;
  	if(exception==undefined) pfxsoapPrint(result,t);
  	else pfxsoapPrintError(exception.toString(),t);
}



function pfxsoapCall() {
	pfxsoapReset();
	
		var total1=(new Date()).getTime();
	
		//echoInt
		var t1=(new Date()).getTime();
		try {
			var intVal=parseInt(1);
			var resVal=wsType.echoInt(intVal);
			var t2=(new Date()).getTime();
			if(resVal!=intVal) throw "Wrong result";
			pfxsoapPrint("echoInt",(t2-t1));
		} catch(ex) {
			var t2=(new Date()).getTime();
			pfxsoapPrint("echoInt",(t2-t1),ex);
		}

		//echoIntArray
		t1=(new Date()).getTime();
		try {
			var intVals=new Array(1,2);
			var resVals=wsType.echoIntArray(intVals);
			var t2=(new Date()).getTime();
			if(!arrayEquals(intVals,resVals)) throw "Wrong result";
			pfxsoapPrint("echoIntArray",(t2-t1));
		} catch(ex) {
			var t2=(new Date()).getTime();
			pfxsoapPrint("echoIntArray",(t2-t1),ex);
		}
		
		//echoFloat
		t1=(new Date()).getTime();
		try {
			var floatVal=parseFloat(2.1);
			var resVal=wsType.echoFloat(floatVal);
			var t2=(new Date()).getTime();
			if(resVal!=floatVal) throw "Wrong result";
			pfxsoapPrint("echoFloat",(t2-t1));
		} catch(ex) {
			var t2=(new Date()).getTime();
			pfxsoapPrint("echoFloat",(t2-t1),ex);
		}
		
		//echoFloatArray
		t1=(new Date()).getTime();
		try {
			var floatVals=new Array(1.1,2.2);
			var resVals=wsType.echoFloatArray(floatVals);
			var t2=(new Date()).getTime();
			if(!arrayEquals(floatVals,resVals)) throw "Wrong result";
			pfxsoapPrint("echoFloatArray",(t2-t1));
		} catch(ex) {
			var t2=(new Date()).getTime();
			pfxsoapPrint("echoFloatArray",(t2-t1),ex);
		}
		
		//echoString
		t1=(new Date()).getTime();
		try {
			var strVal="test";
			var resVal=wsType.echoString(strVal);
			var t2=(new Date()).getTime();
			if(resVal!=strVal) throw "Wrong result";
			pfxsoapPrint("echoString",(t2-t1));
		} catch(ex) {
			var t2=(new Date()).getTime();
   		pfxsoapPrint("echoString",(t2-t1),ex);
   	}
   	
   	//echoStringArray
		t1=(new Date()).getTime();
		try {
			var strVals=new Array("aaa","bbb");
			var resVals=wsType.echoStringArray(strVals);
			var t2=(new Date()).getTime();
			if(!arrayEquals(strVals,resVals)) throw "Wrong result";
			pfxsoapPrint("echoStringArray",(t2-t1));
		} catch(ex) {
			var t2=(new Date()).getTime();
			pfxsoapPrint("echoStringArray",(t2-t1),ex);
		}
		
		//echoStringMultiArray
		t1=(new Date()).getTime();
		try {
			var strVals=new Array(new Array("aaa","bbb"),new Array("ccc","ddd"));
			var resVals=wsType.echoStringMultiArray(strVals);
			var t2=(new Date()).getTime();
			if(!arrayEquals(strVals,resVals)) throw "Wrong result";
			pfxsoapPrint("echoStringMultiArray",(t2-t1));
		} catch(ex) {
			var t2=(new Date()).getTime();
			pfxsoapPrint("echoStringMultiArray",(t2-t1),ex);
		}
   	
   	//echoBoolean
		t1=(new Date()).getTime();
		try {
			var boolVal=true;
			var resVal=wsType.echoBoolean(boolVal);
			var t2=(new Date()).getTime();
			if(resVal!=boolVal) throw "Wrong result";
			pfxsoapPrint("echoBoolean",(t2-t1));
		} catch(ex) {
			var t2=(new Date()).getTime();
   		pfxsoapPrint("echoBoolean",(t2-t1),ex);
   	}
   	
   	//echoBooleanArray
		t1=(new Date()).getTime();
		try {
			var boolVals=new Array(true,false);
			var resVals=wsType.echoBooleanArray(boolVals);
			var t2=(new Date()).getTime();
			if(!arrayEquals(boolVals,resVals)) throw "Wrong result";		
			pfxsoapPrint("echoBooleanArray",(t2-t1));
		} catch(ex) {
			var t2=(new Date()).getTime();
			pfxsoapPrint("echoBooleanArray",(t2-t1),ex);
		}

   	//echoDate
		t1=(new Date()).getTime();
		try {
			var dateVal=new Date();
			var resVal=wsType.echoDate(dateVal);
			var t2=(new Date()).getTime();
			if(!equals(resVal,dateVal)) throw "Wrong result";
			pfxsoapPrint("echoDate",(t2-t1));
		} catch(ex) {
			var t2=(new Date()).getTime();
   		pfxsoapPrint("echoDate",(t2-t1),ex);
   	}

   	//echoDateArray
		t1=(new Date()).getTime();
		try {
			var dateVals=new Array(new Date(),new Date());
			var resVals=wsType.echoDateArray(dateVals);
			var t2=(new Date()).getTime();
			if(!arrayEquals(dateVals,resVals)) throw "Wrong result";		
			pfxsoapPrint("echoDateArray",(t2-t1));
		} catch(ex) {
			var t2=(new Date()).getTime();
			pfxsoapPrint("echoDateArray",(t2-t1),ex);
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
			var resBean=wsType.echoDataBean(bean);
			var t2=(new Date()).getTime();
			if(!equals(resBean,bean)) throw "Wrong result";
			pfxsoapPrint("echoDataBean",(t2-t1));
		} catch(ex) {
			var t2=(new Date()).getTime();
   		pfxsoapPrint("echoDataBean",(t2-t1),ex);
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
			var bean2=new Object();
			bean2["date"]=new Date();
			bean2["floatVals"]=new Array(1.2,2.1);
			bean2["intVal"]=2;
			bean2["name"]="TestBean2";
			bean2["children"]=new Array();
			var bean3=new Object();
			bean3["date"]=new Date();
			bean3["floatVals"]=new Array(1.2,2.1);
			bean3["intVal"]=2;
			bean3["name"]="TestBean3";
			bean3["children"]=new Array(bean1,bean2);
			var beans=new Array(bean3);
			var resBeans=wsType.echoDataBeanArray(beans);
			var t2=(new Date()).getTime();
			if(!equals(resBeans,beans)) throw "Wrong result";
			pfxsoapPrint("echoDataBeanArray",(t2-t1));
		} catch(ex) {
			var t2=(new Date()).getTime();
   		pfxsoapPrint("echoDataBeanArray",(t2-t1),ex);
   	}	
   	
   	//echoElement
   	t1=(new Date()).getTime();
   	try {
   		var doc=null;
   		var elem=null;
     		if(document.implementation && document.implementation.createDocument) {
       		doc=document.implementation.createDocument("","",null);
         } else if(window.ActiveXObject) {
            return new ActiveXObject(getDomDocumentPrefix()+".DomDocument");
         }
         elem=doc.createElement("test");
         var sub=doc.createElement("foo");
         elem.appendChild(sub);
         sub.setAttribute("id","dafdfd");
         var txt=doc.createTextNode("asdfghjklöä");
         sub.appendChild(txt);
			var resElem=wsType.echoElement(elem);
			var t2=(new Date()).getTime();
			if(!elementEquals(resElem,elem)) throw "Wrong result";
			pfxsoapPrint("echoElement",(t2-t1));
		} catch(ex) {
			var t2=(new Date()).getTime();
   		pfxsoapPrint("echoElement",(t2-t1),ex);
   	}
   	
   	var total2=(new Date()).getTime();
		pfxsoapPrintTime((total2-total1));	
		
			equals(null,null);
			equals(undefined,undefined);
			equals(2,2);

}
