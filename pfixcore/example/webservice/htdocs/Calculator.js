//*****************************************************************************
//
//*****************************************************************************

function ConstructorCalculator( url ) {
  this.url  = url;
}

//*****************************************************************************
//
//*****************************************************************************
ConstructorCalculator.prototype.multiply = function( int1, int2, callback ) {

  //  alert("this.url:" + this.url);

	var call = new Call();
	call.setTargetEndpointAddress( this.url );
	call.setOperationName(new QName("multiply"));
	call.addParameter("value1",xmltype.XSD_INT,"IN");
	call.addParameter("value2",xmltype.XSD_INT,"IN");
	call.setReturnType(xmltype.XSD_INT);
  if( callback ) {
    call.callback = "Calculator.multiplyCallback";
  }
  this.callback = callback;
	try {
    var xml = call.invoke( int1 , int2);
    if( !callback && xml != "iframe") {
      document.getElementById("dbg").value += xml;
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
ConstructorCalculator.prototype.multiplyCallback = function( xml ) {

  //  alert("Calculator.multiplyCallback()...");

  var res;
  var n = xml.getElementsByTagName( 'multiplyReturn');
  if( n != null && n.length==1 ) {
    res = n[0].firstChild.nodeValue;
  } else {
    alert("Error");
    return null;
  }

  document.getElementById("dbg").value += "\nresult:" + res + "\n";

  if( this.callback ) {
    this.callback( res );
    return true;
  } else {
    return res;
  }

  //  alert("callback:\nresult:" + res + "\n" + (new XMLSerializer()).serializeToString(xml));
};

var Calculator = new ConstructorCalculator( window.location.protocol + "//" + window.location.host + '/xml/webservice/Calculator' );

