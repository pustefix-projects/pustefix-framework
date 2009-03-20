
importClass(Packages.de.schlund.pfixcore.example.StatusCodeLib);

// the LOG object is predefined (declared in BSF terms) in every script 
LOG.debug("Initializing ScriptingTShirt.js in script itself");

function handleSubmittedData(context, wrapper) {
  
    var ct = context.getContextResourceManager().getResource("de.schlund.pfixcore.example.ContextTShirt");
    
    if ( wrapper.size == "L" && wrapper.color == 2 ) {
        // The combination size "L" and color No. "2" is considered invalid (maybe out of stock) 
        var scode = StatusCodeLib.TSHIRT_SIZECOLOR_OUTOF_STOCK;
        wrapper.addSCodeSize(scode, ["L", "2"], "note");
        return;
    }

    // Everything was ok, store it.
    ct.size = wrapper.size;
    ct.color= wrapper.color;
    if ( wrapper.feature ) {
        ct.feature = wrapper.feature;
    } else {
        ct.feature [-1];
        // This is needed so we produce some output at all on retrieveCurrentStatus when
        // the user decided to NOT check any checkbox in the UI (this makes defaults work)
    }
    
    var scode = StatusCodeLib.TSHIRT_SCRIPTING_SUCCESS;
    context.addPageMessage(scode, null, null);
}

function retrieveCurrentStatus(context, wrapper) {

    var ct = context.getContextResourceManager().getResource("de.schlund.pfixcore.example.ContextTShirt");
    
    if ( !ct.needsData() ) {
        wrapper.color = ct.color;
        wrapper.size = ct.size;
        wrapper.feature = ct.feature;
    }
}

function needsData(context) {
    var ct = context.getContextResourceManager().getResource("de.schlund.pfixcore.example.ContextTShirt");
    return ct.needsData();
}

function prerequisitesMet(context) {
    return true;
}

function isActive(context) {
    return true;
}
