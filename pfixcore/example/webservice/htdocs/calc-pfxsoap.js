function pfxsoapPrint(result,time) {
	document.getElementById('pfxsoap_result').value=result;
  	document.getElementById('pfxsoap_time').innerHTML=time;
  	document.getElementById('pfxsoap_error').innerHTML="";
}

function pfxsoapPrintError(msg,time) {
	document.getElementById('pfxsoap_error').innerHTML=msg;
	document.getElementById('pfxsoap_result').value="";
	document.getElementById('pfxsoap_time').innerHTML=time;
}

var wsCalc=new wsCalculator();

var t1=null;

function pfxsoapCallback(result,exception) {
	var d2=new Date();
   var t2=d2.getTime();
   var t=t2-t1;
  	if(exception==undefined) pfxsoapPrint(result,t);
  	else pfxsoapPrintError(exception.toString(),t);
}

function pfxsoapCall(method,val1,val2) {

	var d1=new Date();
	t1=d1.getTime();

	
	var param1=parseInt(val1);
	var param2=parseInt(val2);
	var result=null;
	
	try {
		if(method=="add") result=wsCalc.add(param1,param2,pfxsoapCallback);
		else if(method=="subtract") result=wsCalc.subtract(param1,param2);
		else if(method=="multiply") result=wsCalc.multiply(param1,param2);
		else if(method=="divide") result=wsCalc.divide(param1,param2);
		var d2=new Date();
   	var t2=d2.getTime();
   	var t=t2-t1;
   	if(method!="add") pfxsoapPrint(result,t);
	} catch(x) {
		var d2=new Date();
   	var t2=d2.getTime();
   	var t=t2-t1;
		pfxsoapPrintError(x.toString(),t);
	}

	

}
