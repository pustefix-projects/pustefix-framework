function sleepMSec( msec ) {

  var t0 = (new Date()).getTime();
  var t1;

  while( (t1 = (new Date()).getTime()) && t1-t0<msec ) {
  }
}

var _reqLoad = new xmlRequest( "GET", '/xml/webservice/Calculator?WSDL', "cb" );

function init() {

//   //----------------------------------------------------------------------
//   var req = new xmlRequest();
//   req.method = "GET";
//   req.url = '/xml/webservice/Calculator?WSDL';
//   req.callback = "cb";

//   req.start();
//   //----------------------------------------------------------------------

//   var req = new xmlRequest( "GET", '/xml/webservice/Calculator?WSDL', "cb" );

//   req.start();
//   //----------------------------------------------------------------------

//   new xmlRequest( "GET", '/xml/webservice/Calculator?WSDL', "cb" ).start();
//   //----------------------------------------------------------------------
}

function cb( xml ) {

  try {

    //  sleepMSec( 1000 );

    document.getElementById("dbg").value += "\n<" + xml;

    //  return;
    var xDoc = xml.documentElement;
    document.getElementById("dbg").value += xDoc;
  

    //  alert( xml instanceof XMLDocument );
    //  alert( xml.implementation.hasFeature( "XPath", "3.0" ));

    var n = xml.getElementsByTagNameNS( 'http://schemas.xmlsoap.org/soap/envelope/', 'Fault');
    if( n != null && n.length==1 ) {
      var tag, msg="";
      for( var i=0; i<n[0].childNodes.length; i++ ) {
        tag = n[0].childNodes[i].firstChild;
        if( tag && tag.nodeType==3 ) {
          msg += n[0].childNodes[i].nodeName + ": " + tag.nodeValue + "\n";
        }
      }
      if( msg != "" ) {
        alert("Error:\n" + msg );
      }
    }

    if( typeof xml.documentElement.xml != "undefined" ) {
      alert(xml.documentElement.xml);
    } else {
      alert((new XMLSerializer()).serializeToString(xml));
      document.getElementById("dbg").value += (new XMLSerializer()).serializeToString(xml);
    }

  } catch(e) {
    alert(e);
  }
}
