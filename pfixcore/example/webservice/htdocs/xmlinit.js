function sleepMSec( msec ) {

  var t0 = (new Date()).getTime();
  var t1;

  while( (t1 = (new Date()).getTime()) && t1-t0<msec ) {
  }
}

var _reqLoad = new xmlRequest( "GET", 'http://webservice.dvorak.ue.schlund.de/xml/webservice/Calculator?WSDL', "cb" );

function init() {

//   //----------------------------------------------------------------------
//   var req = new xmlRequest();
//   req.method = "GET";
//   req.url = 'http://webservice.dvorak.ue.schlund.de/xml/webservice/Calculator?WSDL';
//   req.callback = "cb";

//   req.start();
//   //----------------------------------------------------------------------

//   var req = new xmlRequest( "GET", 'http://webservice.dvorak.ue.schlund.de/xml/webservice/Calculator?WSDL', "cb" );

//   req.start();
//   //----------------------------------------------------------------------

//   new xmlRequest( "GET", 'http://webservice.dvorak.ue.schlund.de/xml/webservice/Calculator?WSDL', "cb" ).start();
//   //----------------------------------------------------------------------
}

function cb( xml ) {

  //  sleepMSec( 1000 );

  document.getElementById("dbg").value += "\n<" + xml;

  //  return;
  //  var xDoc = xml.documentElement;
  //alert( xml instanceof XMLDocument );

  if( typeof xml.documentElement.xml != "undefined" ) {
    alert(xml.documentElement.xml);
  } else {
    alert((new XMLSerializer()).serializeToString(xml));
  }
}
