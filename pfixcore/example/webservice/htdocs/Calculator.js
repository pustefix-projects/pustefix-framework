//*****************************************************************************
//
//*****************************************************************************

function WebserviceCalculator( url ) {
  this.url  = url;
}

//*****************************************************************************
//
//*****************************************************************************
WebserviceCalculator.prototype.multiply = function( int1, int2, callback ) {

  //  alert("this.url:" + this.url);

	var call = new Call();
	call.setTargetEndpointAddress( this.url );
	call.setOperationName(new QName("multiply"));

	call.addParameter("value1",new TypeInfo(xmltypes.XSD_INT),"IN");
	call.addParameter("value2",new TypeInfo(xmltypes.XSD_INT),"IN");

	call.setReturnType(new TypeInfo(xmltypes.XSD_INT));


  if( callback ) {
    call.callback = "Calculator.multiplyCallback";
  }
  this.callback = callback;

	try {
    var xml = call.invoke( int1 , int2 );
    if( !callback && xml != "iframe") {
      //------
      // sync
      //------
      //      document.getElementById("dbg").value += xml;
      return this.multiplyCallback( xml );
    }
	} catch(e) {
		alert(e);
    return null;
	}
};

//*****************************************************************************
//
//*****************************************************************************
WebserviceCalculator.prototype.multiplyCallback = function( xml ) {

  //    alert("Calculator.multiplyCallback()..." + xml);

  //-------------
  // deserialize
  //-------------

	var rpc=new RPCSerializer( "multiply", null, new TypeInfo(xmltypes.XSD_INT));

  var res;
	res = rpc.deserialize(xml.getElementsByTagNameNS(XMLNS_SOAPENV,"Body")[0]);

//   var n = xml.getElementsByTagName( 'multiplyReturn');
//   if( n != null && n.length==1 ) {
//     res = n[0].firstChild.nodeValue;
//   } else {
//     alert("Error");
//     return null;
//   }

  if( this.callback ) {
    // async
    this.callback( res );
    return true;
  } else {
    // sync
    return res;
  }

  //  alert("callback:\nresult:" + res + "\n" + (new XMLSerializer()).serializeToString(xml));
};

var Calculator = new WebserviceCalculator( window.location.protocol + "//" + window.location.host + '/xml/webservice/Calculator' );

