var service = new WS_NameCheck();
function serviceCallback(result, requestId, exception) {
  var elem = document.getElementById("registrationName");
  if(result) {
    elem.style.color="black";
  } else {
    elem.style.color="red";
  }
}
function checkName(input) {
  service.isValid(input.value, serviceCallback);
}