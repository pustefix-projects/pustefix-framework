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

var wsCounter=new wsCounter();

var t1=null;

function foo(result) {
	var d2=new Date();
   var t2=d2.getTime();
   var t=t2-t1;
   pfxsoapPrint(result,t);
}

function pfxsoapCall(method,val1,val2) {

	var d1=new Date();
	t1=d1.getTime();

	
	var param1=parseInt(val1);
	var param2=parseInt(val2);
	var result=null;
	
	//try {
		
		if(method=="getValue") result=wsCounter.getValue();
		else if(method=="addValue") result=wsCounter.addValue(param1);
		var d2=new Date();
   	var t2=d2.getTime();
   	var t=t2-t1;
   	pfxsoapPrint(result,t);
	/*} catch(x) {
		var d2=new Date();
   	var t2=d2.getTime();
   	var t=t2-t1;
		pfxsoapPrintError(x.toString(),t);
	}*/

	

}
