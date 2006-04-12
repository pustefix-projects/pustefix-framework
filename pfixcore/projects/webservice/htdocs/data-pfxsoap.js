function pfxsoapPrint(result,time) {
	var res="";
	if(result instanceof Array) {
		for(var i=0;i<result.length;i++) {
			res+="("+i+")"+result[i]+" ";
		}
		result=res;
	}
	document.getElementById('pfxsoap_result').value=result;
  	document.getElementById('pfxsoap_time').innerHTML=time;
  	document.getElementById('pfxsoap_error').innerHTML="";
}

function pfxsoapPrintError(msg,time) {
	document.getElementById('pfxsoap_error').innerHTML=msg;
	document.getElementById('pfxsoap_result').value="";
	document.getElementById('pfxsoap_time').innerHTML=time;
}

var wsData=new WS_Data();

var t1=null;

function pfxsoapCallback(result,exception) {
	var d2=new Date();
   var t2=d2.getTime();
   var t=t2-t1;
  	if(exception==undefined) pfxsoapPrint(result,t);
  	else pfxsoapPrintError(exception.toString(),t);
}

function pfxsoapCall(method,val1,val2,val3,val4) {

	var d1=new Date();
	t1=d1.getTime();

	var result=null;
	
	try {
		if(method=="exchangeData") {
			var reqStrSize=parseInt(val1);
			var resStrSize=parseInt(val2);
			var str="";
			for(var i=0;i<reqStrSize;i++) str+="X";
			result=wsData.exchangeData(str,resStrSize);
		} else if(method=="exchangeDataArray") {
			var reqArrSize=parseInt(val1);
			var reqStrSize=parseInt(val2);
			var resArrSize=parseInt(val3);
			var resStrSize=parseInt(val4);
			var str="";
			for(var i=0;i<reqStrSize;i++) str+="X";
			var arr=new Array(reqArrSize);
			for(var i=0;i<reqArrSize;i++) arr[i]=str;
			result=wsData.exchangeDataArray(arr,resArrSize,resStrSize,pfxsoapCallback);
		}
		var d2=new Date();
   	var t2=d2.getTime();
   	var t=t2-t1;
   	if(method!="exchangeDataArray") pfxsoapPrint(result,t);
	} catch(x) {
		var msg="";
                if(x.message) msg=x.message;
                else msg=x.toString();
		var d2=new Date();
   	var t2=d2.getTime();
   	var t=t2-t1;
		pfxsoapPrintError(msg,t);
	}

	

}
