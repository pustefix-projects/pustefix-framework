//#****************************************************************************
//#
//#****************************************************************************
function getSessionId() {

  var url = document.location.href;

  if( /;jsessionid=([\w\.]+)/.test(url) ) {
    return RegExp.$1;
  }

  return null;
}

//#****************************************************************************
//#
//#****************************************************************************
function instanceType( obj ) {

  if( obj instanceof Array || /Array/.test(""+obj.constructor) ) {
    return "object:array";
  } else if( obj instanceof Date || /Date/.test(""+obj.constructor) ) {
    return "object:date";
  } else if( obj instanceof RegExp || /RegExp/.test(""+obj.constructor)) {
    return "object:regexp";
  } else {
    return( typeof obj );
  }
}

//#****************************************************************************
//#
//#****************************************************************************
function dataDump() {

  this.maxLevel = Number.MAX_VALUE;
  this.maxCount = Number.MAX_VALUE;

  this.indentUnit = "......";

  this.showFunction = false;
}

//#****************************************************************************
//#
//#****************************************************************************
dataDump.prototype.dump = function( obj, level ) {

  level = level || 0;

  var res = "", indent = "";
  var i;
  for( i=0; i<level; i++ ) {
    indent += level + this.indentUnit;
  }

  if( level == 0 && /^object/.test( instanceType(obj) ) ) {
    res += " (" + instanceType(obj) + ")\n\n";
  }

  try {
    var hasProperties = false;
    for( i in obj ) {
      hasProperties = true;

      res += indent + "obj[" + i + "]:";
      if( (/^function$/.test(instanceType(obj[i])) && this.showFunction) ||
          !/^function$/.test(instanceType(obj[i])) ) {
        res += obj[i];
      }
      res += " (" + instanceType(obj[i]) + ")\n";

      if( /^object/.test(instanceType(obj[i])) && level<this.maxLevel ) {
        res += this.dump( obj[i], level+1 );
      }
    }

    if( !hasProperties ) {
      //      res += indent + "(no properties)\n";      
    }
  } catch(e) {
    res += indent + "(not supported)\n";
  }

  return res;
}
//#****************************************************************************
//#****************************************************************************
