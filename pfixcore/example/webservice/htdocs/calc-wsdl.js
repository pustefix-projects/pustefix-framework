function wsdlPrint(result) {
	var d2=new Date();
	var t2=d2.getTime();
	document.getElementById('wsdl_result').value=result;
  	document.getElementById('wsdl_time').innerHTML=(t2-t1);
  	document.getElementById('wsdl_error').innerHTML="";
}

function wsdlPrintError(msg) {
	document.getElementById('wsdl_error').innerHTML=msg;
	document.getElementById('wsdl_result').value="";
	document.getElementById('wsdl_time').innerHTML=time;
}

var proxy    = null;
var wsdl_uri = "http://webservice.zap.ue.schlund.de/xml/webservice/Calculator?WSDL";

var t1=0;

function wsdlCall(method,val1,val2) {
	var d1=new Date();
	t1=d1.getTime();
	if(!proxy) {
		var listener={ 
      	onLoad: function(aProxy) {
         	proxy=aProxy;
            proxy.setListener(listener);
            callMethod(method,val1,val2);
			},
			onError: function (aError) {
         	wsdlPrintError(aError.message);
         },
         addCallback: function(result) {
         	wsdlPrint(result);
         },
         subtractCallback: function(result) { 
         	wsdlPrint(result);
         },
         multiplyCallback: function(result) {
         	wsdlPrint(result);
         },
         divideCallback: function(result) {
         	wsdlPrint(result);
         }
     	};
		createProxy(listener);
	} else {
		callMethod(method,val1,val2);
	}	
}

function callMethod(method,val1,val2) {
	switch(method) {
 		case "add":
 			proxy.add(val1,val2);
 			break;
 		case "subtract":
 			proxy.subtract(val1,val2);
 			break;
 		case "multiply":
 			proxy.multiply(val1,val2);
 			break;
 		case "divide":
 			proxy.divide(val1,val2);
 			break;
	}
}
    
function createProxy(aCreationListener) {
	try {
   	var factory = new WebServiceProxyFactory();
      factory.createProxyAsync(wsdl_uri, "Calculator", "", true, aCreationListener);
	} catch (ex) {
   	alert(ex);
   }
}
    