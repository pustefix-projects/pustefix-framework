window.onerror = function(message, url, lineNumber) {
  alert(message);
  return true;
};
function runJS() {
  var js=document.getElementById('run').value;
  eval(js);
}