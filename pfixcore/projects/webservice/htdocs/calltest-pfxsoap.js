function consoleReset() {
   var p=document.getElementById("console");
   if(p!=null && p.childNodes!=null) {
      var len=p.childNodes.length;
      for(var i=0;i<len;i++) {
       p.removeChild(p.childNodes[0]);
      }
   }
}

function consolePrint(str) {
   var p=document.createElement("div");
   var t=document.createTextNode(str);
   p.appendChild(t);
   var out=document.getElementById("console");
   out.appendChild(p);
}

var obj1={
  test: function(res,id,ex) {
    var test="asynchronous (with callback object)";
    if(res=='success') consolePrint(test+" OK");
    else consolePrint(test+" Error");
  },
  testError: function(res,id,ex) {
    var test="asynchronous (with callback object and exception)";
    if(ex.message==' Illegal value: error') consolePrint(test+" OK");
    else consolePrint(test+" Error");
  }
};

var obj2={
  test: function(res,id,ex) {
    var test="asynchronous (with callback object and request id)";
    if(res=='success' && id=='3') consolePrint(test+" OK");
    else consolePrint(test+" Error");
  },
  testError: function(res,id,ex) {
    var test="asynchronous (with callback object and request id and exception)";
    if(ex.message==' Illegal value: error' && id=='4') consolePrint(test+" OK");
    else consolePrint(test+" Error");
  }
};

var wsCall=new WS_CallTest();
var wsCallObj1=new WS_CallTest(obj1);
var wsCallObj2=new WS_CallTest(obj2);

function testSync() {
  var test="synchronous";
  var val=wsCall.test("success");
  if(val=='success') consolePrint(test+" OK");
  else consolePrint(test+" Error");
}

function testSyncEx() {
  var test="synchronous (with exception)";
  try {
    wsCall.testError("error");
  } catch(ex) {
    if(ex.message='Illegal value: error') consolePrint(test+" OK");
    else consolePrint(test+" Error");
  }
}

function testAsync() {
  var test="asynchronous (with callback function)";
  var f=function(res,id,ex) {
    if(res=='success') consolePrint(test+" OK");
    else consolePrint(test+" Error");
  }
  wsCall.test("success",f); 
}

function testAsyncId() {
  var test="asynchronous (with callback function and request id)";
  var f=function(res,id,ex) {
    if(res=='success' && id=='1') consolePrint(test+" OK");
    else consolePrint(test+" Error");
  }
  wsCall.test("success",f,"1");   
}

function testAsyncEx() {
  var test="asynchronous (with callback function and exception)";
  var f=function(res,id,ex) {
    if(ex.message==' Illegal value: error') consolePrint(test+" OK");
    else consolePrint(test+" Error");
  }
  wsCall.testError("error",f); 
}

function testAsyncIdEx() {
  var test="asynchronous (with callback function and request id and exception)";
  var f=function(res,id,ex) {
    if(ex.message==' Illegal value: error' && id=='2') consolePrint(test+" OK");
    else consolePrint(test+" Error");
  }
  wsCall.testError("error",f,"2");   
}

function testAsyncObj() {
  wsCallObj1.test("success");
}

function testAsyncObjId() {
  wsCallObj2.test("success","3");
}

function testAsyncObjEx() {
  wsCallObj1.testError("error");
}

function testAsyncObjExId() {
  wsCallObj2.testError("error","4");
}

function serviceCall() {
  consoleReset();
  testSync();
  testSyncEx();
  testAsync();
  testAsyncId();
  testAsyncEx();
  testAsyncIdEx();
  testAsyncObj();
  testAsyncObjId();
  testAsyncObjEx();
  testAsyncObjExId();
}


