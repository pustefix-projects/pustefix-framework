//*****************************************************************************
//
//*****************************************************************************

var _xml = [];
var _xmlTimer = [];
var _xmlTimerCount = [];
var _xmlTimerCountMax = 400;
var _xmlTimerInterval = 5;
var _msXmlHttp;

//*****************************************************************************
//
//*****************************************************************************
function xmlRequest() {

  this.method   = arguments[0];
  this.url      = arguments[1];
  this.callback = arguments[2];

  this.timer = [];
  this.timerCount = [];
}

//*****************************************************************************
//
//*****************************************************************************
xmlRequest.prototype.start = function( content ) {

  // unique timestamp to prevent caching
  var uniq = ""+ new Date().getTime() + Math.floor(1000 * Math.random());
  //  this.url += ( ( this.url.indexOf('?')+1 ) ? '&' : '?' ) + uniq;

  if( this.callback ) {
    //    alert(this.callback.valueOf());
  }

  var i = _xml.length;

  if( 1 && window.XMLHttpRequest ) {

    //----------------
    // XMLHttpRequest
    //----------------

    try {
      _xml[i] = new XMLHttpRequest();

      if( this.callback ) {
        //alert(""+_xml['+i+'].readyState + (_xml['+i+'].readyState==4 ? _xml['+i+'].status : "XXX"));
        _xml[i].onreadystatechange = new Function( 'if( _xml['+i+'].readyState == 4 ) { if( _xml['+i+'].status < 300 ) { '+this.callback+'(_xml['+i+'].responseXML); } else { throw "Asynchronous call failed"; }}' );
      }
      _xml[i].open( this.method, this.url, this.callback ? true : false);
      _xml[i].setRequestHeader("SOAPAction", '""');
      _xml[i].send(content);

      if( !this.callback ) {
        return _xml[i].responseXML;
      } else {
        return true;
      }
    } catch(e) {
      _xml[i] = null;
      alert("Exception:" + e);
    }    
	}

  if( 0 && !_xml[i] && window.ActiveXObject ) {

    //---------
    // ActiveX
    //---------

    if( !_msXmlHttp ) {
      // determine working XMLHTTP component
      var msXmlHttpList = ["MSXML2.XMLHTTP.5.0", "MSXML2.XMLHTTP.4.0", "MSXML2.XMLHTTP.3.0", "MSXML2.XMLHTTP", "MICROSOFT.XMLHTTP.1.0", "MICROSOFT.XMLHTTP.1", "MICROSOFT.XMLHTTP"];
      
      for (var j=0; j<msXmlHttpList.length; j++) {
        try {
          _xml[i] = new ActiveXObject(msXmlHttpList[j]);
          
          // success ==> store, quit loop
          _msXmlHttp = msXmlHttpList[j];
          break;
        } catch(e) {
          // ignore failures
        } 
      }
    }
    alert("_msXmlHttp:" + _msXmlHttp);

    try {
      _xml[i] = new ActiveXObject(_msXmlHttp);
      _xml[i].onreadystatechange = new Function( 'if( _xml['+i+'].readyState == 4 ) { '+this.callback+'(_xml['+i+']); }' );
      _xml[i].open( this.method, this.url, this.callback ? true : false);
      _xml[i].setRequestHeader("SOAPAction", '""');
      _xml[i].send(content);

      if( !this.callback ) {
        return _xml[i].responseXML;
      } else {
        return true;
      }
    } catch(e) {
      _xml[i] = null;
      alert("Exception1:" + e.message);
    }
  }

  if( !_xml[i] && document.createElement && document.childNodes ) {

    //--------
    // iframe
    //--------

    var i = _xml.length;

    //    document.getElementById("dbg").value += "\n>" + i;

    var iframe = document.createElement("iframe");
		iframe.style.visibility = "hidden";
    //     iframe.style.position   = "absolute";
    //     iframe.style.left       = "0px";
    //     iframe.style.top        = "0px";
    iframe.style.width      = "0px";
    iframe.style.height     = "0px";    
    iframe.id               = "pfxxmliframe"+i;
    iframe.name             = "pfxxmliframe"+i;
    if( this.method == "GET" ) {
      iframe.src              = this.url;
    }
    document.body.appendChild(iframe);

    _xml[i] = this.callback;
    _xmlTimer[i] = true;
    _xmlTimerCount[i] = 0;

    var self = this;
    if( this.method == "POST" ) {
      setTimeout( function() {
        var iDoc = iframe.contentWindow.document;

        var attr;
        var form = iDoc.createElement("form");
        //         attr = iDoc.createAttribute("action");
        //         attr.nodeValue = self.url;
        //         form.setAttributeNode(attr);
        form.action = self.url;
        form.method = "POST";

        var field = iDoc.createElement("textarea");
        field.name = "soapmessage";
        field.value = content;
        //        field.value = '<env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/"              xmlns:enc="http://schemas.xmlsoap.org/soap/encoding/"              env:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"              xmlns:xs="http://www.w3.org/1999/XMLSchema"              xmlns:xsi="http://www.w3.org/1999/XMLSchema-instance">   <env:Header/>   <env:Body>      <a0:multiply xmlns:a0="urn:webservices.example.pfixcore.schlund.de">         <a0:in0 xsi:type="xs:int">3</a0:in0>         <a0:in1 xsi:type="xs:int">7</a0:in1>      </a0:multiply>   </env:Body></env:Envelope>';

        form.appendChild(field);

        iDoc.body.appendChild(form);
        
        //        alert( iDoc.body.innerHTML );
        iDoc.forms[iDoc.forms.length-1].submit();

        _xmlTimer[i] = window.setInterval('customOnReadyStateChange()', _xmlTimerInterval);
      }, 50 );
    } else {
      _xmlTimer[i] = window.setInterval('customOnReadyStateChange()', _xmlTimerInterval);
    }

    return "iframe";
  }

  // Error
  return false;
};

//*****************************************************************************
//
//*****************************************************************************
function customOnReadyStateChange() {

  for( var i=0; i<_xml.length; i++ ) {
    if( _xmlTimer[i] &&_xml[i] ) {

      //      document.getElementById("dbg").value += "\n=" + _xmlTimerCount[i] + ", ";

      try {
        if( _xmlTimerCount[i]<_xmlTimerCountMax ) {
          if( window.frames['pfxxmliframe'+i] && window.frames['pfxxmliframe'+i].document && window.frames['pfxxmliframe'+i].document.body && !window.frames['pfxxmliframe'+i].document.innerHTML ) {
            _xmlTimerCount[i]++;
          } else {

            sleepMSec(1000);

            //            alert( _xmlTimerCount[i] + "\n" + document.getElementById("pfxxmliframe"+i).contentWindow.document );
            
            //            eval( _xml[i] + '(window.frames["pfxxmliframe'+i+'"].document);' );
            //            alert("_xml[i]:\n" + _xml[i]);
            //            alert("innerHTML:\n" + window.frames['pfxxmliframe'+i].document.innerHTML);
            //            _xml[i](window.frames["pfxxmliframe'+i+'"].document);
            eval( _xml[i] + '(document.getElementById("pfxxmliframe'+i+'").contentWindow.document);' );
            _xml[i] = null;
            cancelOnReadyStateChange(i);
          }
        } else {
          cancelOnReadyStateChange(i, "too many intervals");
        }
      } catch(e) {
        //        alert("Exception:" + e);
        cancelOnReadyStateChange(i, "Exception:" + e);
      }
    }
  }
}

//*****************************************************************************
//
//*****************************************************************************
function cancelOnReadyStateChange( i, msg ) {

  try {
    if( msg ) {
      //      document.getElementById("dbg").value += "\n=" + i + " " + msg;
    }
    window.clearInterval(_xmlTimer[i]);
    _xmlTimer[i] = null;
  } catch(e) {
    alert("Exception2:" + e);
  }

  try {
    //    alert(document.getElementById("pfxxmliframe"+i));
    //    alert(document.body.childNodes.length);
    //    for( var j=0; j<document.body.childNodes.length; j++ ) {
    //      alert( document.body.childNodes[j] );
    //    }
    document.body.removeChild(document.getElementById("pfxxmliframe"+i));
  } catch(e) {
    alert("Exception3:" + e);
  }
}

//*****************************************************************************
//
//*****************************************************************************
function sleepMSec( msec, i ) {

  return;

  var t0 = (new Date()).getTime();
  var t1;

  while( (new Date().getTime()-t0<msec) ) {
  }
}
//*****************************************************************************
//*****************************************************************************
