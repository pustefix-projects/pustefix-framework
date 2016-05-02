function openTest(url, idSuffix) {
  var frameId = 'frame_' + idSuffix;
  var containerRef = document.getElementById('container_' + idSuffix);
  var frameRef = document.getElementById(frameId);
  if(frameRef) {
    containerRef.removeChild(frameRef);
  } else {
    var iframe = document.createElement('iframe');
    iframe.src = url;
    iframe.id = frameId;
    iframe.frameBorder = 0;
    iframe.onload = function() {
      iframe.style.height = iframe.contentWindow.document.body.scrollHeight + 'px';
      iframe.style.width = iframe.contentWindow.document.body.scrollWidth + 'px';
    };
    containerRef.appendChild(iframe);
  }
}