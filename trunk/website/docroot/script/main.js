var featuretimeout = [];

YAHOO.util.Event.onDOMReady(function() {
  window.topfeatures = document.getElementById('topfeatures');
  window.setTimeout('startAnimateBackground();', 500);  
  window.setTimeout('startAnimateFeatures();', 600);  
  window.setTimeout('startSwitchFeature();', 4500);  
  YAHOO.util.Event.addListener('nextfeature', 'click', nextFeature);
});

function startAnimateBackground() {
  for (var top = -144; top <= 0; top++) {
    window.setTimeout('animateBackground(' + top + ', 0);', (top + 144) * 2);
  }
}

function animateBackground(top, left) {
  topfeatures.style.backgroundPosition = left + 'px ' + top + 'px';
}

function startAnimateFeatures() {
  for (var count = 0; count <= 8; count++) {
    window.setTimeout('animateFeature(' + count + ');', count * 400);
  }
}

function animateFeature(number) {
  YAHOO.util.Dom.setStyle('feature_' + number, 'display', 'block');
  YAHOO.util.Dom.setStyle('feature_' + number, 'opacity', '0');
  var anim = new YAHOO.util.Anim('feature_' + number, { opacity: { to: 1 } }, 1); 
  anim.animate();
  YAHOO.util.Event.addListener('feature_' + number, 'click', selectFeature, number);
}

function nextFeature(event) {
  stopSwitchFeature();
  if (typeof activefeature != 'undefined') {
    if (activefeature == 8) {
      setFeatureActive(1);
    } else {
      setFeatureActive(activefeature + 1);
    }
  }
  YAHOO.util.Event.stopEvent(event);
}

function selectFeature(event, number) {
  stopSwitchFeature();
  setFeatureActive(number);
  YAHOO.util.Event.stopEvent(event);
}

function startSwitchFeature() {
  for (var i = 0; i <= 7; i++) {
    window.featuretimeout[i] = window.setTimeout('setFeatureActive(' + (i + 1) + ');', i * 15000);
  }
  window.switchtimeout = window.setTimeout('startSwitchFeature();', 120000);
}

function stopSwitchFeature(event) {
  if (window.featuretimeout) {
    for (var i = 0; i <= 7; i++) {
      window.clearTimeout(window.featuretimeout[i]);
    }
  }
  if (window.switchtimeout) {
    window.clearTimeout(window.switchtimeout);
  }
}

function setFeatureActive(number) {
  for (var i = 1; i <= 8; i++) {
    if (i != number) {
      document.getElementById('feature_' + i).className = '';
      YAHOO.util.Dom.setStyle('feature_text_' + i, 'display', 'none');
    }
  }
  document.getElementById('feature_' + number).className = 'highlight';
  YAHOO.util.Dom.setStyle('feature_text_' + number, 'display', 'block');
  YAHOO.util.Dom.setStyle('feature_text_' + number, 'opacity', '0');
  var anim = new YAHOO.util.Anim('feature_text_' + number, { opacity: { to: 1 } }, 1); 
  anim.animate();
  window.activefeature = number;
}