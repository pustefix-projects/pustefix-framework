function wsdlPrint(result) {
  var t2= (new Date()).getTime();
  document.getElementById('wsdl_result').value=result;
  //  document.getElementById('wsdl_time').innerHTML=(t2-t1) + " [" + _proxy.length + "]";
  document.getElementById('wsdl_error').innerHTML="";
}

function wsdlPrintError(msg) {
  document.getElementById('wsdl_error').innerHTML=msg;
  document.getElementById('wsdl_result').value="";
  document.getElementById('wsdl_time').innerHTML=time;
}

var _proxy = new Array();
var _calls = new Array();

var url_start = window.location.protocol + "//" + window.location.host;
var wsdl_uri=  url_start + "/Data.wsdl";
alert(wsdl_uri);
var t1=0;

function DataBean(id) {
  this.id=id;
}

DataBean.prototype.getId= function() {
  return this.id;
}

var dataBean=new DataBean("jstest");

function wsdlCall(method,val,sid) {

  switch( method ) {
  case "getData":
    wsdlPrint(data.getData());
    return;
  case "getDataArray":
    wsdlPrint(data.getDataArray());
    return;
  }

  if( method=="getDataBean" ) {
    method=="getDataBeanSid";
  }

  var d1=new Date();
  t1=d1.getTime();
  wsdl_uri= url_start + "/xml/webservice/Data;jsessionid="+sid+"?WSDL&sid="+sid;

  //  if(!proxy) {

//   if( _calls[method] ) {
//     return;
//   }
//   _calls[method] = 1;

  alert( wsdl_uri );

  var listener = { 
    onLoad: function(aProxy) {

      var idx = _proxy.length;
      _proxy[idx] = aProxy;
      _proxy[idx].setListener(listener);

      try {
        //        setTimeout( function() {
          callMethod(idx, method,val);
          //        }, 1000);
      } catch(e) {
        alert("Exception:" + e);
      }

    },
      onError: function (aError) {
        alert(aError);
      },
      getDataCallback: function(result) {
        wsdlPrint(result);
        _calls["getDataArray"] = 0;
      },
      getDataArrayCallback: function(result) {
        wsdlPrint(result);
        _calls["getDataArray"] = 0;
      },
      getDataBeanSidCallback: function(result) {
        wsdlPrint(result);
      },
      echoDataBeanCallback: function(result) {
        wsdlPrint(result);
      }
    };
    createProxy(listener);

//   } else {
//     try {
//       //        setTimeout( function() {
//       callMethod(method,val,sid);
//       //        }, 0);
//     } catch(e) {
//       alert(e);
//     }
//   }	
}

function callMethod( idx, method,val) {


  switch(method) {
  case "getData":
    _proxy[idx].getData();
    break;
  case "getDataArray":
    _proxy[idx].getDataArray();
    break;
  case "getDataBeanSid":
    _proxy[idx].getDataBean(sid);
    break;
  case "echoDataBean":
    _proxy[idx].echoDataBean(dataBean);
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


function sleep( msec ) {

  var t0 = (new Date()).getTime();
  var t1;

  while( (t1 = (new Date()).getTime()) && t1-t0<msec ) {
  }
}

function mysoapCall() {
  var d1=new Date();
  var req=new XMLHttpRequest();
  req.open("POST", url_start + "/xml/webservice/Data;jsessionid="+sid,true);
  req.onreadystatechange=function() {
    if(req.readyState==4) {
      var d2=new Date();
      document.getElementById('mysoap_time').innerHTML=(d2.getTime()-d1.getTime());
      document.getElementById("mysoap_response").value=req.responseText;
    }
  };
  req.setRequestHeader("SOAPAction","\"\"");
  req.send(document.getElementById("mysoap_request").value);
}

function Data() {
  this.start='<env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/" '+
    'xmlns:enc="http://schemas.xmlsoap.org/soap/encoding/" '+
    'env:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" '+
    'xmlns:xs="http://www.w3.org/1999/XMLSchema" '+
    'xmlns:xsi="http://www.w3.org/1999/XMLSchema-instance">'+
    '<env:Header/>'+
    '<env:Body>';
  this.end='</env:Body></env:Envelope>';
}

Data.prototype.send=function(meth) {
  var req;
  if (window.XMLHttpRequest) {
    req=new XMLHttpRequest();
  } else if(window.ActiveXObject) {
    req = new ActiveXObject("Microsoft.XMLHTTP");
  } else {
    alert("XMLHttpRequest not supported");
  }
  req.open("POST", url_start + "/xml/webservice/Data;jsessionid="+sid,false);
  req.setRequestHeader("SOAPAction",'""');
  var msg= this.start+meth+this.end;
  req.send(msg);
  return req.responseXML;
}

Data.prototype.getData = function() {
  var d1=new Date();
  var meth='<a0:getData xmlns:a0="urn:webservices.example.pfixcore.schlund.de"/>';
  var res=this.send(meth);
  var el = res.getElementsByTagName('getDataReturn')[0];

  var val = el.firstChild.nodeValue;
  val = val.replace( /data/g, "DATA");

  var d2=new Date();
  document.getElementById('wsdl_time').innerHTML=(d2.getTime()-d1.getTime());
  return val;
};

Data.prototype.getDataArray = function() {
  var d1=new Date();
  var meth='<a0:getDataArray xmlns:a0="urn:webservices.example.pfixcore.schlund.de"/>';
  var res=this.send(meth);
  var el = res.getElementsByTagName('getDataArrayReturn')[0];

  var arr2 = el.getElementsByTagName('item');

  var arr = new Array();
  for( var i=0; i<arr2.length; i++ ) {
    arr[i] = arr2[i].firstChild.nodeValue;
  }

  var d2=new Date();
  document.getElementById('wsdl_time').innerHTML=(d2.getTime()-d1.getTime());
  return arr.join("|");
};

var data=new Data();
