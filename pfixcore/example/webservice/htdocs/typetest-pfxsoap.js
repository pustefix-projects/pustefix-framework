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
			alert((typeof a1[i])+" "+(typeof a2[i]));
			if(a1[i]!=a2[i]) return false; 
		}
	}
	return true;
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
   	
   	
   	var total2=(new Date()).getTime();
		pfxsoapPrintTime((total2-total1));	

}
