/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixxml;
import org.apache.log4j.Logger;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.TargetGeneratorFactory;
import de.schlund.pfixxml.targets.VirtualTarget;
import de.schlund.pfixxml.util.XsltContext;
    
/**
 * Describe class ImageThemedSrc here.
 *
 *
 * Created: Wed Mar 23 17:15:43 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class ImageThemedSrc {
    private final static Logger LOG = Logger.getLogger(ImageThemedSrc.class);

    /** xslt extension */
    public static String getSrc(XsltContext context, String src, String themed_path, String themed_img,
                                String parent_part_in, String parent_product_in,
                                String targetGen, String targetKey) throws Exception {

        String[]        themes    = null;
        FileResource    tgen_path = ResourceUtil.getFileResource(targetGen);
        TargetGenerator gen       = TargetGeneratorFactory.getInstance().createGenerator(tgen_path);
          
        if (!targetKey.equals("__NONE__")) {
            VirtualTarget target = (VirtualTarget) gen.getTarget(targetKey);
            themes               = target.getThemes().getThemesArr();
        }
        if (themes == null) {
            themes = gen.getGlobalThemes().getThemesArr();
        }
        
        if (isSimpleSrc(src, themed_path, themed_img)) {
            if (src.startsWith("/")) {
                src = src.substring(1);
            }
            LOG.debug("  -> Register image src '" + src + "'");
            DependencyTracker.logImage(context, src, parent_part_in, parent_product_in, targetGen, targetKey, "image");
            return src;
        } else if (isThemedSrc(src, themed_path, themed_img)) {
            if (themed_path.startsWith("/")) {
                themed_path = themed_path.substring(1);
            }

            String testsrc = null;
            for (int i = 0; i < themes.length; i++) {
                String currtheme = themes[i];
                testsrc = themed_path + "/" + currtheme + "/" + themed_img;
                LOG.info("  -> Trying to find image src '" + testsrc + "'");
                if (existsImage(testsrc)) {
                    LOG.info("    -> Found src '" + testsrc + "'");
                    DependencyTracker.logImage(context, testsrc, parent_part_in, parent_product_in, targetGen, targetKey, "image");
                    return testsrc;
                }
                if (i < (themes.length - 1)) {
                    // FIXME: the next commented line should be used sometime so we can discriminate between
                    // "real" missing and "missing, but we found a better version" -- but make sure editor copes with it.
                    //DependencyTracker.logImage(context, testsrc, parent_part_in, parent_product_in, targetGen, targetKey, "shadow");
                    DependencyTracker.logImage(context, testsrc, parent_part_in, parent_product_in, targetGen, targetKey, "image");
                    LOG.info("    -> Image src '" + testsrc + "' not found, trying next theme");
                } else {
                    DependencyTracker.logImage(context, testsrc, parent_part_in, parent_product_in, targetGen, targetKey, "image");
                    LOG.warn("    -> No themed image found!");
                }
            }
            return testsrc;
        } else {
            throw new XMLException("Need to have one of 'src' XOR both 'themed-path' and 'themed-img' given!");
        }
    }


    private static boolean isSimpleSrc(String src, String path, String img) {
        return (src != null && !src.equals("") && (path == null || path.equals("")) && (img == null || img.equals("")));
    }

    private static boolean isThemedSrc(String src, String path, String img) {
        return ((src == null || src.equals("")) && path != null && !path.equals("") && img != null && !img.equals(""));
    }

    private static boolean existsImage(String path) {
        FileResource img = ResourceUtil.getFileResourceFromDocroot(path);
        return (img.exists() && img.canRead() && img.isFile());
    }

}
