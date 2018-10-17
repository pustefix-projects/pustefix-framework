var ws=new WS_Counter();

var timer=new Timer();

function serviceCallback(result,reqID,exception) {
  timer.stop();
  printTime(timer.getTime());
  if(exception==undefined || exception==null) setFormResult(result);
  else printError(exception.toString());
}

function serviceCall(method,val) {
   timer.reset();
	timer.start();
	var param=parseInt(val);
	if(method=="getValue") ws.getValue(serviceCallback);
	else if(method=="addValue") ws.addValue(param,serviceCallback);
}