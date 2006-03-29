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

function mozsoapCall(method,val,sid) {

	var d1=new Date();
	var t1=d1.getTime();

	var call=new SOAPCall();;
	call.transportURI="http://"+window.location.host+"/xml/webservice/Counter;"+sid;

	var params=[];

	if(val!=null) {
		var param=new SOAPParameter();
		param.name="value";
		param.value=parseInt(val);
		params=[param];
	}

	call.encode(0,method,null,0,null,params.length,params);

	var response=call.invoke();

	var d2=new Date();
   var t2=d2.getTime();
   var t=t2-t1;;

	if(response.fault) {
		soapPrintError(response.fault.faultString,t);
	} else {
   	var results=new Array();
  		results=response.getParameters(false,{});
  		soapPrint(results[0].value,t);
  	}
  	
}
