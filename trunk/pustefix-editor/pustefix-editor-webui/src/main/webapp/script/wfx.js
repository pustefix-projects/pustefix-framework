//#****************************************************************************
//#
//#****************************************************************************
function wfx( config ) {
  
  if (typeof config == "undefined") {
    // new (default) configuration
    this.config = new wfx.Config();
  } else {
    // existing configuration
    this.config = config;
  }  
}
//#****************************************************************************

// browser identification

wfx.agt      = navigator.userAgent.toLowerCase();
wfx.is_ie    = ((wfx.agt.indexOf("msie")  != -1) && 
		(wfx.agt.indexOf("opera") == -1));
wfx.is_gecko = (navigator.product == "Gecko");

//#****************************************************************************
//#
//#****************************************************************************
wfx.Config = function () {

  this.xhtml = "xhtml";
  
  this.doctype = {
    "xhtml" : '<' + '?xml version="1.0" encoding="UTF-8"?>\n' + 
    '<!' + 'DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">',
    "html"  : '<!' + 'DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">'
  }
}

// EOF
// Local variables: //
// c-basic-offset:2 //
// indent-tabs-mode:t //
// End: //
