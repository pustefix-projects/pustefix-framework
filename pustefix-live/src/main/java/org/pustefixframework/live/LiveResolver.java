/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.live;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;

public class LiveResolver {

    private static Logger LOG = Logger.getLogger(LiveJarInfo.class);

    private static LiveJarInfo liveJarInfoInstance = new LiveJarInfo();

    private static synchronized LiveJarInfo getLiveJarInfoInstance() {

        if (liveJarInfoInstance == null) {
            liveJarInfoInstance = new LiveJarInfo();
        }
        // TODO: check if live.xml was modified and reload

        return liveJarInfoInstance;
    }

    /**
     * Resolves the live root for the given root and path. If the root represents a directory in file system we try to
     * resolve the <strong>docroot</strong> live root. If the root represents a jar file we try to resolve the
     * <strong>module</strong> live root.
     * @param root
     *            the root
     * @param path
     *            the resource path, relative to root
     * @return the resolved live root, or null if no live root was found
     */
    public File resolveLiveRoot(String root, String path) throws Exception {

        URL url;
        File file = new File(root);
        if (file.exists()) {
            url = file.toURI().toURL();
        } else {
            url = new URL(root);
        }

        if (root.endsWith(".jar")) {
            URL liveModuleRoot = resolveLiveModuleRoot(url, path);
            return liveModuleRoot != null ? new File(liveModuleRoot.getFile()) : null;
        } else {
            URL liveDocRoot = resolveLiveDocroot(root, path);
            return liveDocRoot != null ? new File(liveDocRoot.getFile()) : null;
        }
    }

    /**
     * Resolves the module base.
     * @param jarUrl
     *            the original module jar URL
     * @param path
     *            the resource path, relative to jarUrl
     * @return the resolved live module root, or null if no live module root was found
     * @throws Exception
     */
    // NOTE: Be careful when changing the method signature!
    // This method is invoked via reflection from pustefix-core!
    public URL resolveLiveModuleRoot(URL jarUrl, String path) throws Exception {
        File location = getLiveJarInfoInstance().getLiveModuleRoot(jarUrl, path);
        if (location != null && location.exists()) {
            return location.toURI().toURL();
        }

        return null;
    }

    /**
     * Resolves the docroot base.
     * @param docroot
     *            the original docroot
     * @param path
     *            the resource path, relative to docroot
     * @return the resolved live docroot, or null if no live docroot was found
     * @throws Exception
     *             the exception
     */
    // NOTE: Be careful when changing the method signature!
    // This method is invoked via reflection from pustefix-core!
    public URL resolveLiveDocroot(String docroot, String path) throws Exception {

        if (getLiveJarInfoInstance().hasWarEntries()) {
            // live.xml defines live folders for web applications, use this information
            if (LOG.isDebugEnabled()) {
                LOG.debug("Resolving live docroot from live.xml for " + docroot + ":" + path);
            }
            File liveDocroot = getLiveJarInfoInstance().getLiveDocroot(docroot, path);
            if (liveDocroot != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("  --> " + liveDocroot);
                }
                return liveDocroot.toURI().toURL();
            }
        } else if (docroot.endsWith("src/main/webapp")) {
            // Support for running webapps from source with 'mvn tomcat:run'
            // Set the target artifact directory as alternative docroot if docroot is source location
            if (LOG.isDebugEnabled()) {
                LOG.debug("Resolving fallback docroot for " + docroot + ":" + path);
            }
            File dir = guessFallbackDocroot();
            if (dir != null) {
                String fallbackDocroot = dir.getAbsolutePath();
                if (fallbackDocroot != null
                        && (path.startsWith("/core/") || path.startsWith("/modules/") || path.startsWith("/.cache/")
                                || path.startsWith("/wsscript/") || path.startsWith("/wsdl/") || path
                                .equals("/WEB-INF/buildtime.prop"))) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("  --> " + dir);
                    }
                    return dir.toURI().toURL();
                }
            }
        } else {
            // Support for running webapps from source with 'mvn tomcat:run-war'
            // Set the source artifact directory as alternative docroot if docroot is target location
            if (LOG.isDebugEnabled()) {
                LOG.debug("Resolving live docroot from source folder for " + docroot + ":" + path);
            }
            File liveDocroot = guessLiveDocroot(docroot);
            if (liveDocroot != null) {

                for (String s : LiveJarInfo.DEFAULT_DOCROOT_LIVE_EXCLUSIONS) {
                    if (path.startsWith(s)) {
                        return null;
                    }
                }

                File liveFile = new File(liveDocroot, path.substring(1));
                if (liveFile.exists()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("  --> " + liveDocroot);
                    }
                    return liveDocroot.toURI().toURL();
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("  --> non found");
        }
        return null;
    }

    private static File guessFallbackDocroot() {
        File target = new File("target");
        if (target.exists() && target.isDirectory()) {
            for (File file : target.listFiles()) {
                if (file.isDirectory()) {
                    File webInfDir = new File(file, "WEB-INF");
                    if (webInfDir.exists())
                        return file;
                }
            }
        }
        return null;
    }

    private static File guessLiveDocroot(String docroot) {
        File docrootDir = new File(docroot);
        if (docrootDir.exists() && docrootDir.isDirectory()) {
            File targetDir = docrootDir.getParentFile();
            if (targetDir != null && targetDir.getName().endsWith("target")) {
                File projectDir = targetDir.getParentFile();
                if (projectDir != null) {
                    File srcMainWebappDir = new File(projectDir, "src/main/webapp");
                    if (srcMainWebappDir.exists() && srcMainWebappDir.isDirectory()) {
                        return srcMainWebappDir;
                    }
                }
            }
        }
        return null;
    }

}
