function dataPrint(result,time) {
	var res="";
	if(result instanceof Array) {
		for(var i=0;i<result.length;i++) {
			res+="("+i+")"+result[i]+" ";
		}
		result=res;
	}
	document.getElementById('data_result').value=result;
}

function pfxsoapPrintError(msg,time) {
	document.getElementById('pfxsoap_error').innerHTML=msg;
	document.getElementById('pfxsoap_result').value="";
	document.getElementById('pfxsoap_time').innerHTML=time;
}

var wsData=new WS_Data();
var jwsData=new JWS_Data();

var timer=new Timer();

function serviceCallback(result,reqid,exception) {
	timer.stop();
	printTime(timer.getTime());
	if(exception==undefined || exception==null) dataPrint(result);
  	else printError(exception.toString());
}

function serviceCall(method,val1,val2,val3,val4) {
   timer.reset();
	timer.start();
	var ws=soapEnabled()?wsData:jwsData;
	var result=null;
	if(method=="exchangeData") {
			var reqStrSize=parseInt(val1);
			var resStrSize=parseInt(val2);
			var str="";
			for(var i=0;i<reqStrSize;i++) str+="X";
			ws.exchangeData(str,resStrSize,serviceCallback);
	} else if(method=="exchangeDataArray") {
			var reqArrSize=parseInt(val1);
			var reqStrSize=parseInt(val2);
			var resArrSize=parseInt(val3);
			var resStrSize=parseInt(val4);
			var str="";
			for(var i=0;i<reqStrSize;i++) str+="X";
			var arr=new Array(reqArrSize);
			for(var i=0;i<reqArrSize;i++) arr[i]=str;
			ws.exchangeDataArray(arr,resArrSize,resStrSize,serviceCallback);
	}	
}
