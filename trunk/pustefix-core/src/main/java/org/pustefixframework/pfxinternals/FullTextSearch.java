package org.pustefixframework.pfxinternals.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.pustefixframework.live.LiveResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.util.JarFileURLConnection;
import de.schlund.pfixcore.util.ModuleDescriptor;
import de.schlund.pfixcore.util.ModuleInfo;
import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.util.FileUtils;

public class FullTextSearch {

    private Logger LOG = Logger.getLogger(FullTextSearch.class);

    public void search(Element root, Pattern filePattern, Pattern textPattern, boolean searchWebapp, boolean searchModules, String searchModule, boolean searchClassPath) {

        Element resultElem = root.getOwnerDocument().createElement("result");
        root.appendChild(resultElem);

        if(searchWebapp) {
            try {
                URL liveDocroot = LiveResolver.getInstance().resolveLiveDocroot(GlobalConfig.getDocroot(), "/");
                if(liveDocroot != null) {
                    if(liveDocroot.getProtocol().equals("file")) {
                        File resDir = new File(liveDocroot.toURI());
                        search(resDir, filePattern, textPattern, resultElem);
                    }
                }
            } catch(Exception x) {
                LOG.warn("Error while searching text [" + x + "]");
            }
        }

        Set<URL> searchedJars = new HashSet<URL>();

        if(searchModules) {

            SortedSet<String> sortedModules = new TreeSet<String>();
            if(searchModule != null) {
                if(ModuleInfo.getInstance().getModuleDescriptor(searchModule) != null) {
                    sortedModules.add(searchModule);
                }
            } else {
                sortedModules.addAll(ModuleInfo.getInstance().getModules());
            }

            for(String module: sortedModules) {
                ModuleDescriptor moduleDesc = ModuleInfo.getInstance().getModuleDescriptor(module);
                try {
                    URL url = LiveResolver.getInstance().resolveLiveModuleRoot(moduleDesc.getURL(), "/");
                    if(url != null) {
                        if(url.getProtocol().equals("file")) {
                            File resDir = new File(url.toURI());
                            search(resDir, filePattern, textPattern, resultElem);
                        }            
                    } else {
                        if(moduleDesc.getURL().getProtocol().equals("jar")) {
                            URL jarURL = getJarURL(moduleDesc.getURL());
                            //no "findbugs : DMI_COLLECTION_OF_URLS" here, because only file URLs in use
                            searchedJars.add(jarURL);
                            searchJar(jarURL, filePattern, textPattern, resultElem);
                        }
                    }
                } catch(Exception x) {
                    x.printStackTrace();
                }

            }
        }

        if(searchClassPath) {

            if(getClass().getClassLoader() instanceof URLClassLoader) {
                URLClassLoader cl = (URLClassLoader)getClass().getClassLoader();
                URL[] urls = cl.getURLs();
                for(URL url: urls) {
                    if(url.getProtocol().equals("file")) {
                        if(url.getPath().endsWith(".jar")) {
                            try {
                                URL jarURL = new URL("jar:" + url.toString() + "!/");
                                //no "findbugs : DMI_COLLECTION_OF_URLS" here, because only file URLs in use
                                if(!searchedJars.contains(jarURL)) {
                                    searchJar(jarURL, filePattern, textPattern, resultElem);
                                }
                            } catch(Exception x) {
                                x.printStackTrace();
                            }
                        } else {
                            try {
                            File dir = new File(url.toURI());
                            search(dir, filePattern, textPattern, resultElem);
                            } catch(Exception x) {
                                x.printStackTrace();
                            }
                        }
                    }
                }
            }

        }

    }

    private void searchJar(URL url, Pattern filePattern, Pattern textPattern, Element root) {
        try {
            JarFileURLConnection con = new JarFileURLConnection(url);
            JarFile jarFile = con.getJarFile();
            Enumeration<JarEntry> entries = jarFile.entries();
            while(entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                int ind = name.lastIndexOf('/');
                if(ind > -1 && ind < name.length() - 1) {
                    name = name.substring(ind +1);
                }
                if(filePattern.matcher(name).matches()) {
                    InputStream in = jarFile.getInputStream(entry);
                    List<Element> matchElems = null;
                    if(textPattern != null) {
                        if(!FileUtils.isBinary(in)) {
                            in = jarFile.getInputStream(entry);
                            matchElems = doTextSearch(in, textPattern, root.getOwnerDocument());
                        }
                    }
                    if(textPattern == null || (matchElems != null && !matchElems.isEmpty())) {
                        Element resourceElem = root.getOwnerDocument().createElement("resource");
                        String moduleURL = url.toString();
                        int moduleInd = moduleURL.lastIndexOf('!');
                        if(moduleInd > -1) {
                            moduleURL = moduleURL.substring(0, moduleInd);
                        }
                        resourceElem.setAttribute("path", moduleURL + "!/" + entry.getName());
                        root.appendChild(resourceElem);
                        if(matchElems != null) {
                            for(Element matchElem: matchElems) {
                                resourceElem.appendChild(matchElem);
                            }
                        }
                    }
                }
            }
        } catch(IOException x) {
            LOG.warn("Error while searching text [" + x + "]");
        }
    }

    private void search(File file, Pattern filePattern, Pattern textPattern, Element root) {
        if(!file.isHidden()) {
            if(file.isDirectory()) {
                File[] subFiles = file.listFiles();
                for(File subFile: subFiles) {
                    search(subFile, filePattern, textPattern, root);
                }
            } else {
                if(filePattern.matcher(file.getName()).matches()) {
                    List<Element> matchElems = null;
                    if(textPattern != null) {
                        try {
                            if(!FileUtils.isBinary(file)) {
                                matchElems = doTextSearch(file, textPattern, root.getOwnerDocument());
                            }
                        } catch(IOException x) {
                            LOG.warn("Error while searching text [" + x + "]");
                        }
                    }
                    if(textPattern == null || (matchElems != null && !matchElems.isEmpty())) {
                        Element resourceElem = root.getOwnerDocument().createElement("resource");
                        resourceElem.setAttribute("path", file.getAbsolutePath());
                        root.appendChild(resourceElem);
                        if(matchElems != null) {
                            for(Element matchElem: matchElems) {
                                resourceElem.appendChild(matchElem);
                            }
                        }
                    }
                }
            }
        }
    }

    private List<Element> doTextSearch(File file, Pattern textPattern, Document doc) {
        try {
            FileInputStream in = new FileInputStream(file);
            return doTextSearch(in, textPattern, doc);
        } catch(IOException x) {
            LOG.warn("Error while searching text [" + x + "]");
            return new ArrayList<Element>();
        }
    }

    private List<Element> doTextSearch(InputStream in, Pattern textPattern, Document doc) {
        List<Element> matchElems = new ArrayList<Element>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
            String line = null;
            try {
                int lineCount = 0;
                while((line = reader.readLine()) != null) {
                    lineCount++;
                    Matcher matcher = textPattern.matcher(line);
                    if(matcher.find()) {
                        Element matchElem = doc.createElement("match");
                        matchElem.setAttribute("line", ""+lineCount);
                        int maxLen = 320;
                        if(line.length() > maxLen) {
                            line = cutOff(line, matcher.start(), matcher.end() - 1, maxLen);
                            matchElem.setAttribute("cut", "true");
                        }
                        matchElem.setTextContent(line);
                        matchElems.add(matchElem);
                    }
                }
             } finally {
                 in.close();
             }
        } catch(IOException x) {
            LOG.warn("Error while searching text [" + x + "]");
        }
        return matchElems;
    }

    private URL getJarURL(URL url) {
        if(url.getProtocol().equals("jar")) {
            String urlStr = url.toString();
            int ind = urlStr.indexOf('!');
            if(ind > -1 && urlStr.length() > ind + 1)  {
                urlStr = urlStr.substring(0, ind+2);
                try {
                    return new URL(urlStr);
                } catch(MalformedURLException x) {
                    LOG.warn("Error while searching text [" + x + "]");
                }
            }
        }
        return url;
    }

    static String cutOff(final String line, int start, int end, final int maxLen) {
        if(maxLen < 1) {
            return "";
        } else {
            if(line.length() > maxLen) {
                int len = end - start + 1;
                if(len < maxLen) {
                    //display the matching string and as much of the rest as possible
                    int rest = maxLen - len;
                    if(start >= rest) {
                        start = start - rest;
                    } else {
                        end = end + rest - start;
                        start = 0;
                    }
                } else {
                    //display as much of the matching string as possible
                    end = start + maxLen - 1;
                }
                return line.substring(start, end + 1);
            } else {
                return line;
            }
        }    
    }

}
