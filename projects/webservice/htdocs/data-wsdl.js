function wsdlPrint(result) {
	var res="";
	if(result instanceof Array) {
		for(var i=0;i<result.length;i++) {
			res+="("+i+")"+result[i]+" ";
		}
		result=res;
	}
	var d2=new Date();
	var t2=d2.getTime();
	document.getElementById('wsdl_result').value=result;
  	document.getElementById('wsdl_time').innerHTML=(t2-t1);
  	document.getElementById('wsdl_error').innerHTML="";
}

function wsdlPrintError(msg) {
	document.getElementById('wsdl_error').innerHTML=msg;
	document.getElementById('wsdl_result').value="";
	document.getElementById('wsdl_time').innerHTML=(t2-t1);
}

var proxy=null;
var wsdl_uri="http://webservice.zap.ue.schlund.de/Data.wsdl";

var t1=0;

function wsdlCall(method,sid,val1,val2,val3,val4) {
	wsdl_uri="http://"+window.location.host+"/xml/webservice/Data;"+sid+"?WSDL";
	var d1=new Date();
	t1=d1.getTime();
	if(!proxy) {
		var listener={ 
      	onLoad: function(aProxy) {
         	proxy=aProxy;
            proxy.setListener(listener);
            callMethod(method,val1,val2,val3,val4);
			},
			onError: function (aError) {
         	wsdlPrintError(aError.message);
         },
         exchangeDataCallback: function(result) {
         	wsdlPrint(result);
         },
         exchangeDataArrayCallback: function(result) {
         	wsdlPrint(result);
         }
     	};
		createProxy(listener);
	} else {
		callMethod(method,val1,val2,val3,val4);
	}	
}

function callMethod(method,val1,val2,val3,val4) {
	switch(method) {
 		case "exchangeData":
 			var reqStrSize=parseInt(val1);
			var resStrSize=parseInt(val2);
			var str="";
			for(var i=0;i<reqStrSize;i++) str+="X";
 			proxy.exchangeData(str,resStrSize);
 			break;
 		case "exchangeDataArray":
 			var reqArrSize=parseInt(val1);
			var reqStrSize=parseInt(val2);
			var resArrSize=parseInt(val3);
			var resStrSize=parseInt(val4);
			var str="";
			for(var i=0;i<reqStrSize;i++) str+="X";
			var arr=new Array(reqArrSize);
			for(var i=0;i<reqArrSize;i++) arr[i]=str;
 			proxy.exchangeDataArray(arr,resArrSize,resStrSize);
 			break;
	}
}
    
function createProxy(aCreationListener) {
	try {
   	var factory = new WebServiceProxyFactory();
      factory.createProxyAsync(wsdl_uri, "Data", "", true, aCreationListener);
	} catch (ex) {
   	alert(ex);
   }
}
    