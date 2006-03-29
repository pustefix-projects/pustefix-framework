var url_start = window.location.protocol + "//" + window.location.host;

function soapPrint(result,time) {
	document.getElementById('soap_result').value=result;
  	document.getElementById('soap_time').innerHTML=time;
  	document.getElementById('soap_error').innerHTML="";
}

function soapPrintError(msg,time) {
	document.getElementById('soap_error').innerHTML=msg;
	document.getElementById('soap_result').value="";
	document.getElementById('soap_time').innerHTML=time;
}

function mozsoapCall(method,val1,val2) {

	var d1=new Date();
	var t1=d1.getTime();

	var call=new SOAPCall();;
	call.transportURI= url_start + "/xml/webservice/Calculator";

	var param1=new SOAPParameter();
	param1.name="value1";
	param1.value=parseInt(val1);
	
	var param2=new SOAPParameter();
	param2.name="value2";
	param2.value=parseInt(val2);

	var params=[param1,param2];

	call.encode(0,method,null,0,null,params.length,params);

	var response=call.invoke();

	var d2=new Date();
   var t2=d2.getTime();
   var t=t2-t1;
   
   

	if(response.fault) {
		soapPrintError(response.fault.faultString,t);
	} else {
   	var results=new Array();
  		results=response.getParameters(false,{});
  		soapPrint(results[0].value,t);
  	}
  	
}
