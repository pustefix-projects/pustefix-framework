function stopEvent(ev) {

  if( typeof ev.preventDefault != "undefined" ) {
    ev.preventDefault();
    ev.stopPropagation();
  } else {
    ev.cancelBubble = true;
    ev.returnValue = false;
  }
}

function unselectable() {

  if( window.document.attachEvent ) {
    window.document.attachEvent(      "onmousedown", stopEvent);
    window.document.body.attachEvent( "onkeypress",  stopEvent);
    window.document.body.attachEvent( "onfocus",     stopEvent);
  }

  if( window.addEventListener ) {
    window.addEventListener( "mousedown", stopEvent, true);
    window.addEventListener( "keypress",  stopEvent, true);
    window.addEventListener( "focus",     stopEvent, true);
  }
}

function fillLines() {

  var el = document.getElementById("bodynode");
  var pre = document.createElement("pre");
  pre.style.textAlign = "right";
  el.appendChild(pre);
  var node;

  for( var i=1; i<=3333; i++ ) {
    node = document.createTextNode(i + "\r\n");
    pre.appendChild(node);
  }
  



}
