package de.schlund.pfixxml;

import de.schlund.pfixxml.util.Path;
import de.schlund.util.FactoryInit;
import java.io.File;
import java.util.Properties;

/**
 * Describe class PathFactory here.
 *
 *
 * Created: Tue Jun 29 18:04:32 2004
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class PathFactory implements FactoryInit {
    private static final String PROP_DOCROOT = "pustefix.docroot";
    private static PathFactory instance = new PathFactory(); 
    private File docroot;
    
    private PathFactory() {}

    public static PathFactory getInstance() {
        return instance;
    }

    public Path createPath(String relative) {
        if (relative.startsWith(File.separator)) {
            throw new IllegalArgumentException("**** Need a relative path: " + relative);
        }
        return Path.create(docroot, relative);
    }

    public String makePathStringRelative(String absolute) {
        if (!absolute.startsWith(docroot.getPath() + File.separator)) {
            throw new IllegalArgumentException("**** Absolute path " + absolute +
                                               " can't be made relative to docroot " + docroot.getPath());
        }
        return absolute.substring((docroot.getPath() + File.separator).length());
    }

    public void init(String docrootstr) {
        docroot = new File(docrootstr);
        if (!docroot.isDirectory()) {
            throw new RuntimeException("**** docroot " + docrootstr + " must be a directory! ****");
        }
        if (!docroot.isAbsolute()) {
            throw new RuntimeException("**** docroot " + docrootstr + " must be a absolute! ****");
        }
        System.out.println("Docroot is: " + docroot.getPath());
    }

    public void init(Properties props) {
        String docrootstr = props.getProperty(PROP_DOCROOT);
        if (docrootstr == null || docrootstr.equals("")) {
            throw new RuntimeException("**** Need the docroot property in factory.prop! ****");
        }
        init(docrootstr);
    }
}
