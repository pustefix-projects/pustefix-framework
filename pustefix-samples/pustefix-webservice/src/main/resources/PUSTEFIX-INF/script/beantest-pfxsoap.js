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

function checkResult(res,ref) {
   for(var prop in res) {
      var resVal=res[prop];
      var refVal=ref[prop];
      if(resVal!=refVal) return false;
   }
   for(var prop in ref) {
      var refVal=ref[prop];
      var resVal=res[prop];
      if(refVal!=resVal) return false;
   }
   return true;
}

var wsCall=new WS_BeanTest();
//var jwsCall=new JWS_BeanTest();
var ws=null;

var wsRef1={"foo":1,"bar":2,"baz":3,"pub":4,"ro":5,"blah":9};
var jwsRef1={"foo":1,"test":3,"public":4,"ro":5};
var ref1=null;

var wsRef2={"foo":1,"bar":2,"baz":3,"pub":4,"ro":5,"blah":9,"one":10,"two":11,"three":12};
var jwsRef2={"foo":1,"test":3,"public":4,"ro":5,"one":10,"oneplusone":11};
var ref2=null;

function initWS() {
   //if(soapEnabled()) {
   //   ws=wsCall;
   //   ref1=wsRef1;
   //   ref2=wsRef2;
   //} else {
   //   ws=jwsCall;
   //   ref1=jwsRef1;
   //   ref2=jwsRef2;
   //}
   ws=wsCall;
   ref1=jwsRef1;
   ref2=jwsRef2;
}

function testAsync1() {
  var test="bean with annotations (ignored by soap)";
  var f=function(res,id,ex) {
    if(res!=null && checkResult(res,ref1)) consolePrint(test+" OK");
    else consolePrint(test+" ERROR");
    stopTimer();
  }
  ws.echoWeird(ref1,f); 
}

function testAsync2() {
  var test="bean with xml metadata (ignored by soap)";
  var f=function(res,id,ex) {
    if(res!=null && checkResult(res,ref2)) consolePrint(test+" OK");
    else consolePrint(test+" ERROR");
    stopTimer();
  }
  ws.echoWeirdSub(ref2,f); 
}

var timer=new Timer();
var cnt; 
var totalCnt;

function serviceCall() {
  cnt=0;
  totalCnt=2;
  consoleReset();
  initWS();
  
  timer.reset();
  timer.start();
  
  testAsync1();
  testAsync2();
  
}

function stopTimer() {
  cnt++;
  if(cnt==totalCnt) {
    timer.stop();
    printTime(timer.getTime());
  }
}

