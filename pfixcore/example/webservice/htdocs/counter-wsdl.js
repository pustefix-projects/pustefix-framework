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
	document.getElementById('wsdl_time').innerHTML=(t2-t1);
}

var proxy=null;
var wsdl_uri="http://webservice.zap.ue.schlund.de/Counter.wsdl";

var t1=0;

function wsdlCall(method,val,sid) {
	wsdl_uri="http://"+window.location.host+"/xml/webservice/Counter;"+sid+"?WSDL";
	var d1=new Date();
	t1=d1.getTime();
	if(!proxy) {
		var listener={ 
      	onLoad: function(aProxy) {
         	proxy=aProxy;
            proxy.setListener(listener);
            callMethod(method,val,sid);
			},
			onError: function (aError) {
         	wsdlPrintError(aError.message);
         },
         getValueCallback: function(result) {
         	wsdlPrint(result);
         },
         addValueCallback: function(result) { 
         	wsdlPrint(result);
         }
     	};
		createProxy(listener);
	} else {
		callMethod(method,val,sid);
	}	
}

function callMethod(method,val,sid) {
 
      //alert(proxy);
      //for(k in proxy) alert(k);
	switch(method) {
 		case "getValue":
 			proxy.getValue();
 			break;
 		case "addValue":
 			proxy.addValue(val);
 			break;
	}
}
    
function createProxy(aCreationListener) {
	try {
   	var factory = new WebServiceProxyFactory();
      factory.createProxyAsync(wsdl_uri, "Counter", "", true, aCreationListener);
	} catch (ex) {
   	alert(ex);
   }
}
    