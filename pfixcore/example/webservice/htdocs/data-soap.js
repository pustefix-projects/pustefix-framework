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

function soapCall(method,val,sid) {

	var d1=new Date();
	var t1=d1.getTime();

	var call=new SOAPCall();
	call.transportURI="http://webservice.zap.ue.schlund.de:80/xml/webservice/Data;jsessionid="+sid;

	
	var params=[];
	if(val!=null) {
		if(val==0) {
			var param=new SOAPParameter();
			param.name="data";
			param.value=dataBean;
			params=[param];
		} else if(val==1) {
			var param=new SOAPParameter();
			param.name="value";
			param.value="lll";
			param.schemaType=2;
			params=[param];
		}
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
//  		alert(results[0].element.getElementsByTagName('item')[0].firstChild.nodeValue);
 //		alert(results[0].element.getAttribute("href"));
 //		alert(results[0].element.parentElement.tagName);
  		//alert("XXX" + (results[0].element.nodeValue));
 // 		for(i in results[0]) {
  //			alert( i + "=" + results[0][i] );
  	//		}
  		soapPrint(results[0].value,t);
  	}
  	
}
