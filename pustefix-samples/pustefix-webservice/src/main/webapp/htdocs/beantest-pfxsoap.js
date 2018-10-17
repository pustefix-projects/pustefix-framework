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

var ws=new WS_BeanTest();
var ref1={"foo":1,"test":3,"public":4,"ro":5};
var ref2={"foo":1,"test":3,"public":4,"ro":5,"one":10,"oneplusone":11};

function testAsync1() {
  var test="bean with annotations";
  var f=function(res,id,ex) {
    if(res!=null && checkResult(res,ref1)) consolePrint(test+" OK");
    else consolePrint(test+" ERROR");
    stopTimer();
  }
  ws.echoWeird(ref1,f); 
}

function testAsync2() {
  var test="bean with xml metadata";
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

