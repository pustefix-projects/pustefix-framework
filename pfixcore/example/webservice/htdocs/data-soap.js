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

function DataBean(id) {
	this.id=id;
}

DataBean.prototype.getId= function() {
	return this.id;
}

var dataBean=new DataBean("jstest");

function soapCall(method,sid,val1,val2,val3,val4) {

	var d1=new Date();
	var t1=d1.getTime();

	var call=new SOAPCall();
	call.transportURI="http://webservice.zap.ue.schlund.de:80/xml/webservice/Data;jsessionid="+sid;

	
	
	if(method=="exchangeData") {
			var reqStrSize=parseInt(val1);
			var resStrSize=parseInt(val2);
			var str="";
			for(var i=0;i<reqStrSize;i++) str+="X";
			var param1=new SOAPParameter();
			param1.name="data";
			param1.value=str;
			var param2=new SOAPParameter();
			param2.name="strSize";
			param2.value=resStrSize;
			var params=new Array(param1,param2);
	} else if(method=="exchangeDataArray") {
			var reqArrSize=parseInt(val1);
			var reqStrSize=parseInt(val2);
			var resArrSize=parseInt(val3);
			var resStrSize=parseInt(val4);
			var str="";
			for(var i=0;i<reqStrSize;i++) str+="X";
			var arr=new Array(reqArrSize);
			for(var i=0;i<reqArrSize;i++) arr[i]=str;
			var param1=new SOAPParameter();
			param1.name="data";
			param1.value=arr;
			var param2=new SOAPParameter();
			param2.name="arrSize";
			param2.value=resArrSize;
			var param3=new SOAPParameter();
			param3.name="strSize";
			param3.value=resStrSize;
			var params=new Array(param1,param2,param3);	
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
