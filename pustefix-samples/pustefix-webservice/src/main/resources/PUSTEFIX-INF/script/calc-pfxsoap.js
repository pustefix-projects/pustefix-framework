var wsCalc=new WS_Calculator();
//var jwsCalc=new pfx.ws.json.DynamicProxy("Calculator");
//var jwsCalc=new JWS_Calculator();
//jwsCalc.setDebug(true);
//alert(jwsCalc._protocol);
//alert(jwsCalc.add(2,4));

var timer=new Timer();

function serviceCallback(result,reqID,exception) {
	timer.stop();
   printTime(timer.getTime());
  	if(exception==undefined || exception==null) setFormResult(result);
  	else printError(exception.toString());
}

function serviceCall(method,val1,val2) {
   timer.reset();
	timer.start();
	//var ws=soapEnabled()?wsCalc:jwsCalc;
	var ws = wsCalc;
	var param1=parseInt(val1);
	var param2=parseInt(val2);
	if(method=="add") ws.add(param1,param2,serviceCallback);
	else if(method=="subtract") ws.subtract(param1,param2,serviceCallback);
	else if(method=="multiply") ws.multiply(param1,param2,serviceCallback);
	else if(method=="divide") ws.divide(param1,param2,serviceCallback);
}
