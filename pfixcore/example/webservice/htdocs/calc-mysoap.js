function ws() {
}

ws.str2int = function( s, radix ) {

  return parseInt( s, radix );
};

function mysoapCall() {
	var d1=new Date();
	var req=new XMLHttpRequest();
	req.open("POST","http://webservice.zap.ue.schlund.de/xml/webservice/Calculator",true);
	req.onreadystatechange=function() {
		if(req.readyState==4) {
			var d2=new Date();
			document.getElementById('mysoap_time').innerHTML=(d2.getTime()-d1.getTime());
			document.getElementById("mysoap_response").value=req.responseText;
		}
	}
	req.setRequestHeader("SOAPAction","\"\"");
	req.send(document.getElementById("mysoap_request").value);
}

function Calculator() {
	start='<env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/" '+
         'xmlns:enc="http://schemas.xmlsoap.org/soap/encoding/" '+
         'env:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" '+
         'xmlns:xs="http://www.w3.org/1999/XMLSchema" '+
         'xmlns:xsi="http://www.w3.org/1999/XMLSchema-instance">'+
         '<env:Header/>'+
   		'<env:Body>';
   end='</env:Body></env:Envelope>';
}

Calculator.prototype.send=function(meth) {
	var req;
	if (window.XMLHttpRequest) {
		req=new XMLHttpRequest();
	} else if(window.ActiveXObject) {
      req = new ActiveXObject("Microsoft.XMLHTTP");
	} else {
		alert("XMLHttpRequest not supported");
	}
	req.open("POST","http://webservice.zap.ue.schlund.de/xml/webservice/Calculator",false);
	req.setRequestHeader("SOAPAction",'""');
	var msg=start+meth+end;
	req.send(msg);
	return req.responseXML;
}

Calculator.prototype.add=function(val1,val2) {
  var d1=new Date();
  var meth='<a0:add xmlns:a0="urn:webservices.example.pfixcore.schlund.de">'+
    '<a0:in0 xsi:type="xs:int">' + val1 + '</a0:in0>'+
    '<a0:in1 xsi:type="xs:int">' + val2 + '</a0:in1>'+
    '</a0:add>';
  var res=this.send(meth);
  var val=res.getElementsByTagName('addReturn')[0].firstChild.nodeValue;
  var d2=new Date();
  document.getElementById('stub_time').innerHTML=(d2.getTime()-d1.getTime());
  return ws.str2int(val, 16);
}

var calculator=new Calculator();

