var sid = getSessionId();
//alert("sid:" + sid);

var __webservices__ = new Array();

__webservices__['Counter'] = new webservice('Counter');


// browser identification
webservice.agt   = navigator.userAgent.toLowerCase();
webservice.is_ie = ((webservice.agt.indexOf("msie")  != -1) && 
                    (webservice.agt.indexOf("opera") == -1));

proxy = null;

var t0, t1;

//#****************************************************************************
//#
//#****************************************************************************
function webservice( name ) {

  //----------
  // IE + Moz
  //----------

  this.name = name;

  this.initialized = false;

  //---------
  // IE only
  //---------
  this.iCallId = null;

  //----------
  // Moz only
  //----------
  this.proxy = null;

  switch( name ) {
  case 'Counter':
    this.wsdl_uri = "http://webservice.zap.ue.schlund.de/xml/webservice/Counter;jsessionid=" + sid + "?WSDL";
    this.callback = {
      "getValue":        getValueCallback,
      "addValue":        addValueCallback
    };
    break;
  case 'Data':
    this.wsdl_uri = "http://webservice.zap.ue.schlund.de/xml/webservice/Data.wsdl",
    this.callback = {
      "getData":         getDataCallback,
      "getDataArray":    getDataArrayCallback,
      "getDataBean":     getDataBeanCallback,
      "echoDataBean":    echoDataBeanCallback
    };
    break;
  }
}

//#****************************************************************************
//#
//#****************************************************************************
webservice.prototype.init = function() {

  if( webservice.is_ie ) {

    service.useService( this.wsdl_uri, this.name );
    this.initialized = true;

  } else {

    var ref = this;

    setTimeout( function() {
      try {
        var factory  = new WebServiceProxyFactory();
        
        var listener = new Object();

        listener = ref.setListener(listener);
        var data = new dataDump();
        alert( data.dump(listener));

        factory.createProxyAsync( ref.wsdl_uri, ref.name, "", true, ref.setListener(listener));
      } catch(e) {
        alert(e);
      }
    }, 100);
  }
}

//#****************************************************************************
//#
//#****************************************************************************
webservice.prototype.call = function( meth, val1 ) {

  t0 = new Date().getTime();

  if( !this.initialized ) {
    this.init();
  }

  var ref = this;

  if( webservice.is_ie ) {
    var fun = new Function( "arg1", "arg2", "arg3", "service."+this.name+".callService(arg1, arg2, arg3)");
    ref.iCallId = fun( ref.callback[meth], meth, val1 );
    //    ref.iCallId = service.Data.callService( ref.callback[meth], meth, val1 );
  } else {
    
    var timer = setInterval( function(ref) {
      if( ref.proxy ) {
        clearInterval(timer);
        ref.initialized = true;

        var callme = eval( "ref.proxy."+meth+"Callback" );
        callme( ref.proxy, val1 );

//         var fun = new Function( "arg1", "arg2", "ref.proxy."+meth+".call(arg1, arg2)");
//         alert("1");
//         fun( ref.proxy, val1 );
        alert("2");
      }
    }, 1000 );
  }
};

//#****************************************************************************
//#
//#****************************************************************************
webservice.prototype.setListener = function( listener ) {

  var ref = this;

  listener = {
    onLoad: function(aProxy) {
      ref.proxy = aProxy;
      ref.proxy.setListener(listener);
      ref.initialized = true;
      //      alert(ref.proxy);
    },
  
    onError: function(aError) {
      ref.printError(aError);
    },
    getValueCallback: function(result) {
      alert(result);
    },
    addValueCallback: function(result) { 
      alert(result);
    }
  };

//   for( var meth in this.callback ) {
//     listener[meth + "Callback"] = new Function( "res", meth+"Callback(res)" );
//   }

  return listener;
}

//#****************************************************************************
//#
//#****************************************************************************
webservice.prototype.print = function( val ) {
  alert( "Print:\n" + val );
}

//#****************************************************************************
//#
//#****************************************************************************
webservice.prototype.printError = function( val ) {
  alert( "printError:\n" + val );
}

//#****************************************************************************
//#****************************************************************************

function getValueCallback(result) {

  alert("getValueCallback");
  var res;

  if( webservice.is_ie ) {
    res = result.value;
    document.forms[2].wsdl_result.value = res;
    document.getElementById('wsdlie_time').innerHTML=(t1-new Date().getTime());
  } else {
    res = result;
    document.forms[1].wsdl_result.value = res;
    document.getElementById('wsdl_time').innerHTML=(t1-new Date().getTime());
  }
  //  genericCallback("getValue", result);
}

function addValueCallback(result) {
  genericCallback("addValue", result);
}

function getDataCallback(result) {
  __webservice__.genericCallback("getData", result);
}

function getDataArrayCallback(result) {
  __webservice__.genericCallback("getDataArray", result);
}

function getDataBeanCallback(result) {
  __webservice__.genericCallback("getDataBean", result);
}

function echoDataBeanCallback(result) {
  __webservice__.genericCallback("echoDataBean", result);
}

//#****************************************************************************
//#
//#****************************************************************************
function genericCallback( meth, res ) {

  if(res.error) {
    // Pull the error information from the event.result.errorDetail properties
    var xfaultcode   = res.errorDetail.code;
    var xfaultstring = res.errorDetail.string;
    var xfaultsoap   = res.errorDetail.raw;
    
    alert(method + "<br>" + xfaultcode + "<br>" + xfaultstring + "<br>" + xfaultsoap );
  } else {

    //    var data = new dataDump();
    //    alert( data.dump( res ));
    alert(res);
  }
}


