importPackage(Packages.de.schlund.util.statuscodes);
importPackage(Packages.de.schlund.pfixcore.example);

function handleSubmittedData(context, wrapper) {
    
    var crm = context.getContextResourceManager()
    crm.getResource("de.schlund.pfixcore.example.ContextAdultInfo").adult = false;
    
    var ct = crm.getResource("de.schlund.pfixcore.example.ContextTShirt");

    if ( wrapper.size == "L" && wrapper.color == 2 ) {
        // The combination size "L" and color No. "2" is considered invalid (maybe out of stock) 
        wrapper.addSCodeSize(new StatusCodeFactory("pfixcore.example.tshirt").getStatusCode("SIZECOLOR_OUTOF_STOCK"),
                            ["L", "2"], 
                            "note");
        return;
    }

    // Everything was ok, store it.
    ct.size = wrapper.size;
    ct.color = wrapper.color;
    if ( wrapper.feature != null ) {
        ct.feature = wrapper.feature;
    } else {
        ct.feature = [-1];
        // This is needed so we produce some output at all on retrieveCurrentStatus when
        // the user decided to NOT check any checkbox in the UI (this makes defaults work)
    }
}
 
function needsData(context) {
    var crm = context.getContextResourceManager();
    return crm.getResource("de.schlund.pfixcore.example.ContextTShirt").needsData();
}

function prerequisitesMet(context) {
    return true;
}
function isActive(context) {
    return true;
}

function retrieveCurrentStatus(context, wrapper) {
    var ct = context.getContextResourceManager().getResource("de.schlund.pfixcore.example.ContextTShirt");
    
    if ( !ct.needsData() ) {
        wrapper.color = ct.color;
        wrapper.size = ct.size;
        wrapper.feature = ct.feature;
    }     
}
