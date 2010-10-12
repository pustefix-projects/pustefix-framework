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

    private static Logger LOG = Logger.getLogger(LiveResolver.class);

    private LiveJarInfo liveJarInfo;

    private LiveJarInfo getLiveJarInfo() {

        if (liveJarInfo == null) {
            liveJarInfo = new LiveJarInfo();
        }
        // TODO: check if live.xml was modified and reload

        return liveJarInfo;
    }

    /**
     * Gets the live.xml file, null if none was detected.
     * @return the live.xml file, null if none was detected
     */
    public File getLiveXmlFile() {
        return getLiveJarInfo().getLiveFile();
    }

    /**
     * Resolves the live root for the given root and path. If the root represents a directory in file system we try to
     * resolve the <strong>docroot</strong> live root. If the root represents a file we try to resolve the
     * <strong>module</strong> live root.
     * @param root
     *            path to the root file or root directory;
     * @param path
     *            the resource path, relative to root. Must start with a slash
     * @return the resolved live root, or null if no live root was found
     */
    public File resolveLiveRoot(String root, String path) throws Exception {
        URL url;
        File file = new File(root);

        if (!path.startsWith("/")) {
            throw new IllegalArgumentException(path);
        }

        if (file.exists()) {
            url = file.toURI().toURL();
        } else {
            url = new URL(root);
        }

        if (file.isFile()) {
            URL liveModuleRoot = resolveLiveModuleRoot(new URL("jar:" + url.toString() + "!" + path), path);
            return liveModuleRoot != null ? new File(liveModuleRoot.getFile()) : null;
        } else {
            URL liveDocRoot = resolveLiveDocroot(root, path);
            if (liveDocRoot == null) {
                liveDocRoot = resolveLiveModuleRoot(url, path);
            }
            return liveDocRoot != null ? new File(liveDocRoot.getFile()) : null;
        }
    }

    /**
     * Resolves the module base.
     * @param url
     *            the original module jar URL or file target URL
     * @param path
     *            the resource path, relative to the URL
     * @return the resolved live module root, or null if no live module root was found
     * @throws Exception
     */
    public URL resolveLiveModuleRoot(URL url, String path) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolving live module root from live.xml for " + url + ":" + path);
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        File moduleRoot = getLiveJarInfo().getLiveModuleRoot(url, path);
        if (moduleRoot != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("  --> " + moduleRoot);
            }
            return moduleRoot.toURI().toURL();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("  --> not found");
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
    public URL resolveLiveDocroot(String docroot, String path) throws Exception {

        if (getLiveJarInfo().hasWarEntries() && !docroot.endsWith("src/main/webapp")) {
            // live.xml defines live folders for web applications, use this information
            if (LOG.isDebugEnabled()) {
                LOG.debug("Resolving live docroot from live.xml for " + docroot + ":" + path);
            }
            File liveDocroot = getLiveJarInfo().getLiveDocroot(docroot, path);
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
            File dir = guessFallbackDocroot(docroot);
            if (dir != null) {
                String fallbackDocroot = dir.getAbsolutePath();
                if (fallbackDocroot != null
                        && (path.startsWith("/core/") || path.startsWith("/modules/") || path.startsWith("/.cache/")
                                || path.startsWith("/wsscript/") || path.startsWith("/wsdl/"))) {
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
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("  --> excluded");
                        }
                        return null;
                    }
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("  --> " + liveDocroot);
                }
                return liveDocroot.toURI().toURL();
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("  --> not found");
        }
        return null;
    }

    private static File guessFallbackDocroot(String srcMainWebapp) throws Exception {
        File srcMainWebappDir = new File(srcMainWebapp);
        if (srcMainWebappDir.exists() && srcMainWebappDir.isDirectory()) {
            File projectDir = srcMainWebappDir.getParentFile().getParentFile().getParentFile();
            File pomFile = LiveUtils.guessPom(srcMainWebapp);
            File target = new File(projectDir, "target");
            if (pomFile != null && pomFile.exists() && target.exists() && target.isDirectory()) {
                String artifactId = LiveUtils.getArtifactFromPom(pomFile);
                for (File file : target.listFiles()) {
                    if (file.isDirectory() && file.getName().startsWith(artifactId)) {
                        File webInfDir = new File(file, "WEB-INF");
                        if (webInfDir.exists())
                            return file;
                    }
                }
            }
        }
        return null;
    }

    private static File guessLiveDocroot(String docroot) {
        File docrootDir = new File(docroot);
        if (docrootDir.exists() && docrootDir.isDirectory()) {
            File targetDir = docrootDir.getParentFile();
            if (targetDir != null && targetDir.getName().equals("target")) {
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
