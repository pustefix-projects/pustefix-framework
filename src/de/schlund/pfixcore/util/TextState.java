package de.schlund.pfixcore.util;

import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixxml.*;
import java.util.Properties;



/**
 * TextState.java
 *
 *
 * Created: Mon Apr  7 15:30:35 2003
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class TextState extends StateImpl {
    private static String def_mime  = "text/css";
    private static String def_cache = "max-age=\"3600\"";
    
    public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {
        ResultDocument resdoc = new ResultDocument(); 
        SPDocument     doc    = resdoc.getSPDocument();

        Properties     props  = context.getPropertiesForCurrentPageRequest();
        String         mime   = props.getProperty(MIMETYPE);
        if (mime != null) {
            doc.setResponseContentType(mime);
        } else {
            doc.setResponseContentType(def_mime);
        }
        doc.addResponseHeader("Cache-Control", def_cache);
        
        return resdoc;
    }

    
} // CssState
