//*****************************************************************************
//
//*****************************************************************************
Function.prototype.extend = function(o)
{
  var _p = this.prototype = new o;
  _p.contructor = this;
  this.superClass = o.prototype;
  return _p;
};

//*****************************************************************************
//
//*****************************************************************************
function EngVariable(v)
{
  if (typeof v != "undefined") this.set(v);
};

var _p = EngVariable.prototype;
_p._className = "EngVariable";
_p.type = "";
_p._value = null;

_p.set = function(v)
{
  this._value = v;
  return true;
};

_p.get = function()
{
  return this._value;
};

_p.toString = function()
{
  return String(this.get());
};

//*****************************************************************************
//
//*****************************************************************************
function EngBoolean(v)
{
  if (typeof v != "undefined") this.set(v);  
};

var _p = EngBoolean.extend(EngVariable);

_p._className = "EngBoolean";
_p.type = "boolean";

_p.set = function(v)
{
  var r;
  if (typeof v == "string") {
    if (v=="true") {
      r = true;
    } else if(v=="false") {
      r = false;
    } else {
      r = Boolean(v);
    }
  } else {
    r = typeof v == "boolean" ? v : Boolean(v);
  }

  this._value = r;
  return true;  
};

//*****************************************************************************
//
//*****************************************************************************
function EngNumber(v, hint)
{
  this.hint = hint;
  if (typeof v != "undefined") this.set(v);
};

var _p = EngNumber.extend(EngVariable);

_p._className = "EngNumber";
_p.type = "number";

_p.parse = function(v) {  
  var r;
  if( (typeof this.hint != "undefined" && this.hint == "integer") || (v-Math.floor(v))==0 ) {
    // int
    r = parseInt(v);
  } else {
    // float
    r = parseFloat(v);
  }
  if( isNaN(r) ) {
    return 0;
  } else {
    return r;
  }
};

_p.set = function(v)
{
  if (typeof v == "number")
  {
    this._value = this.parse(v);
    return true;
  }
  else if (typeof v == "string")
  {
    if (v=="true"||v=="false")
    {
      this._value = this.parse(Boolean(v));
      return true;
    };
      
    var t = this.parse(v);
    if (!isNaN(t))
    {
      this._value = t;
      return true;
    };
  }
  else if (typeof v == "boolean")
  {
    this._value = this.parse(v);
    return true;
  };
    
  throw new Error("EngNumber: Not a valid number: " + v);   
};

//*****************************************************************************
//
//*****************************************************************************
function EngInteger(v)
{
  if (typeof v != "undefined") this.set(v);
};

var _p = EngInteger.extend(EngNumber);

_p.hint = "integer";
_p._className = "EngInteger";
_p.type = "integer";
_p._value = null;

_p.set = function(v)
{
  return EngInteger.superClass.set.call( this, v);
};

//*****************************************************************************
//
//*****************************************************************************
function EngFloat(v)
{
  if (typeof v != "undefined") this.set(v);
};

var _p = EngFloat.extend(EngNumber);

_p.hint = "float";
_p._className = "EngFloat";
_p.type = "float";
_p._value = null;

_p.set = function(v)
{
  return EngFloat.superClass.set.call( this, v);
};

_p.toString = function(v)
{
  if( v && (v-Math.floor(v)>0) ) {
    return String(this.get()) + ".0";
  } else {
    return String(this.get());
  }
};

//*****************************************************************************
//
//*****************************************************************************
function EngString(v)
{
  if (typeof v != "undefined") this.set(v);  
};

var _p = EngString.extend(EngVariable);

_p._className = "EngString";
_p.type = "string";

_p.set = function(v)
{
  this._value = typeof v == "string" ? v : String(v);
  
  return true;  
};

//*****************************************************************************
//
//*****************************************************************************
function EngDate(v)
{
  if (typeof v != "undefined") this.set(v);  
};

var _p = EngDate.extend(EngVariable);

_p._className = "EngDate";
_p.type = "date";

_p.set = function(v)
{
  this._value = typeof v == "date" ? v : Date(v);
  
  return true;  
};

//*****************************************************************************
//
//*****************************************************************************
Array.prototype.type = "array";

//*****************************************************************************
//*****************************************************************************

