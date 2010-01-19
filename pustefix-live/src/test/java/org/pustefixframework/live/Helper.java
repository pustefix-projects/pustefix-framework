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

import org.apache.commons.io.FileUtils;

public class Helper {

    public static final File HOME_DIR = new File(System.getProperty("user.home"));
    public static final File FALLBACK_LIFE_XML = new File(HOME_DIR, ".m2/life.xml");
    public static final File FALLBACK_LIVE_XML = new File(HOME_DIR, ".m2/live.xml");

    public static final File WORKSPACE_BASE_DIR = new File(Helper.class.getResource("/").getFile());
    public static final File WORKSPACE_LIVE_XML = new File(WORKSPACE_BASE_DIR, "live.xml");

    public static final File MOD1_BASE_DIR = new File(WORKSPACE_BASE_DIR, "mod1");
    public static final File MOD1_POM_XML = new File(MOD1_BASE_DIR, "pom.xml");
    public static final File MOD1_SRC_MAIN_RESOURCES_DIR = new File(MOD1_BASE_DIR, "src/main/resources");

    public static final File APP1_BASE_DIR = new File(WORKSPACE_BASE_DIR, "app1");
    public static final File APP1_POM_XML = new File(APP1_BASE_DIR, "pom.xml");
    public static final File APP1_SRC_MAIN_WEBAPP_DIR = new File(APP1_BASE_DIR, "src/main/webapp");
    public static final File APP1_TARGET_DIR = new File(APP1_BASE_DIR, "target/app1");
    public static final File EDITOR_TARGET_DIR = new File(APP1_BASE_DIR, "target/editor");
    public static final File EDITOR_POM_XML = new File(EDITOR_TARGET_DIR, "META-INF/maven/org.pustefixframework.editor/pustefix-editor-webui/pom.xml");

    // public static final File RESOURCES_DIR = new File(BASE_DIR, "src/main/resources");

    public static void createFallBackLife() throws Exception {
        String data = "<life><jar><id><group>a.b.c</group><artifact>d</artifact><version>0.1</version></id><directory>/tmp/d/src/main/resources</directory></jar></life>";
        FileUtils.writeStringToFile(FALLBACK_LIFE_XML, data, "UTF-8");
    }

    public static void createFallBackLive() throws Exception {
        String data = "<live><war><id><group>a.b.c</group><artifact>f</artifact><version>0.2</version></id><directory>/tmp/f/src/main/webapp</directory></war></live>";
        FileUtils.writeStringToFile(FALLBACK_LIVE_XML, data, "UTF-8");
    }

    public static void createWorkspaceLive() throws Exception {
        String data = "<live>";
        data += "<jar><id><group>a.b.c</group><artifact>mod1</artifact><version>0.1</version></id><directory>/tmp/mod1/src/main/resources</directory></jar>";
        data += "<jar><id><group>org.pustefixframework.samples.modules</group><artifact>sample-module-A</artifact><version>0.14.4-SNAPSHOT</version></id><directory>/tmp/sample/src/main/resources</directory></jar>";
        data += "<war><id><group>a.b.c</group><artifact>app1</artifact><version>0.3</version></id><directory>/tmp/app1/src/main/webapp</directory></war>";
        data += "</live>";
        FileUtils.writeStringToFile(WORKSPACE_LIVE_XML, data, "UTF-8");
    }

    public static void createWorkspacePom() throws Exception {
        String data = "<project>";
        data += "<groupId>a.b.c</groupId>";
        data += "<artifactId>app1</artifactId>";
        data += "<version>0.3</version>";
        data += "</project>";
        FileUtils.writeStringToFile(APP1_POM_XML, data, "UTF-8");
    }

    /**
     * <pre>
     * |-- pom.xml
     * |-- src
     * |   `-- main
     * |       `-- webapp
     * |           |-- core
     * |           |   `-- file.xml
     * |           `-- file.xml
     * `-- target
     *     `-- editor
     *         `-- META-INF
     *             `-- maven
     *                 `-- org.pustefixframework.editor
     *                     `-- pustefix-editor-webui
     *                         `-- pom.xml
     *     `-- app1
     *         |-- WEB-INF
     *         |   `-- buildTime.prop
     *         `-- core
     *             `-- file.xml
     * </pre>
     * @throws Exception
     */
    public static void createProjectLayout() throws Exception {

        FileUtils.forceMkdir(MOD1_SRC_MAIN_RESOURCES_DIR);

        FileUtils.forceMkdir(APP1_TARGET_DIR);
        FileUtils.forceMkdir(new File(APP1_TARGET_DIR, "WEB-INF"));
        FileUtils.writeStringToFile(new File(APP1_TARGET_DIR, "WEB-INF/buildTime.prop"), "dummy", "UTF-8");
        FileUtils.forceMkdir(new File(APP1_TARGET_DIR, "core"));
        FileUtils.writeStringToFile(new File(APP1_TARGET_DIR, "core/file.xml"), "dummy", "UTF-8");
        
        FileUtils.forceMkdir(EDITOR_POM_XML.getParentFile());
        String data = "<project>";
        data += "<groupId>org.pustefixframework.editor</groupId>";
        data += "<artifactId>pustefix-editor-webui</artifactId>";
        data += "<version>0.14.4-SNAPSHOT</version>";
        data += "</project>";
        FileUtils.writeStringToFile(EDITOR_POM_XML, data, "UTF-8");

        FileUtils.forceMkdir(APP1_SRC_MAIN_WEBAPP_DIR);
        FileUtils.writeStringToFile(new File(APP1_SRC_MAIN_WEBAPP_DIR, "file.xml"), "dummy", "UTF-8");
        FileUtils.forceMkdir(new File(APP1_SRC_MAIN_WEBAPP_DIR, "core"));
        FileUtils.writeStringToFile(new File(APP1_SRC_MAIN_WEBAPP_DIR, "core/file.xml"), "dummy", "UTF-8");

        createWorkspacePom();
    }

    public static void cleanUp() throws Exception {
        FileUtils.deleteDirectory(APP1_BASE_DIR);
        FileUtils.deleteDirectory(MOD1_BASE_DIR);
        FALLBACK_LIFE_XML.delete();
        FALLBACK_LIVE_XML.delete();
        WORKSPACE_LIVE_XML.delete();
    }

}
