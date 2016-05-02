//#****************************************************************************
//# wfxhere.js
//#****************************************************************************

var wfxed, wfxtb;

function wfxinit() {

  if( document.getElementById("wfxedit01") == null || 
      document.getElementById("wfxtb01")   == null ||
      document.getElementById("wfxta01")   == null ) {
    return;
  }

  //--------
  // editor
  //--------

  wfxed = new wfxEditor();
  wfxed.generate("wfxedit01", document.getElementById("wfxta01") );
  //---------------------------------------------------------------------------

  //---------
  // toolbar 
  //---------

  wfxtb = new wfxToolbar();

  //---------------
  // customization
  //---------------

  var cfg = wfxtb.config;

  wfxtb.registerEditor(wfxed);
  wfxed.registerToolbar(wfxtb);

  wfxtb.generate("wfxtb01");
  //---------------------------------------------------------------------------

//  wfxed.syntaxrehighlighting = document.getElementById("wfxtoolbar__el__syntaxrehighlighting");
//
//  if( wfxed.syntaxrehighlighting ) {
//    wfxed.syntaxrehighlighting.style.backgroundColor = "#00ff00";
//    wfxed.syntaxrehighlighting.style.border = "1px solid red";
//    wfxed.syntaxrehighlighting.checked = "checked";
//  }

  function setFocus() {
    try {
      document.getElementById("wfxedit01").contentWindow.focus();
      
      wfxed.startIntervalRehighlighting();
    } catch(e) {
    }
  };
  setTimeout( setFocus, 1000 );
}

function scrollToLine( linenumber ) {
    wfxed._linebar.scroll( 0, wfxed._linepx * (linenumber-1));
    wfxed._editor.scroll( 0, wfxed._linepx * (linenumber-1));
    wfxed._colorizeLine(linenumber);
    wfxed._linenumber = linenumber;
}

// EOF
// Local variables: //
// c-basic-offset:2 //
// indent-tabs-mode:t //
// End: //
