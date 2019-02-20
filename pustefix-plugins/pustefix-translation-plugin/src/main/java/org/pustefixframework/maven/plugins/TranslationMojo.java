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
package org.pustefixframework.maven.plugins;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.pustefixframework.maven.plugins.deepl.DeepLRestClient;
import org.pustefixframework.util.LocaleUtils;
import org.pustefixframework.util.i18n.POData;
import org.pustefixframework.util.i18n.POMessage;
import org.pustefixframework.util.i18n.POReader;

import de.schlund.pfixxml.util.FileUtils;

/**
 * Translates new messages from source PO files which
 * are missing in target PO files using a Translator
 * implementation (currently only DeepL supported).
 *
 * @goal translate
 * @phase process-sources
 * @threadSafe
 */
public class TranslationMojo extends AbstractMojo {

    /**
     * @parameter default-value="_"
     */
    private String localeSuffixSeparator;

    /**
     * @parameter default-value=".po"
     */
    private String localeFileExtension;

    /**
     * @parameter
     * @required
     */
    private String sourceLocale;

    /**
     * @parameter
     * @required
     */
    private String[] targetLocales;

    /**
     * @parameter default-value="TODO: review automatically translated text"
     */
    private String translatorComment;

    /**
     * @parameter default-value="DeepL"
     */
    private String translator;

    /**
     * @parameter
     */
    private TranslatorSettings translatorSettings;

    /**
     * @parameter property="project"
     * @required
     */
    private MavenProject project;


    public void execute() throws MojoExecutionException {

        File resourceDir = new File(project.getBasedir(), "src/main/webapp");
        if(!resourceDir.exists()) {
            resourceDir = new File(project.getBasedir(), "src/main/resources");
        }
        if(resourceDir.exists()) {
        Translator translationService;
            if(translator.equals("DeepL")) {
                translationService = new DeepLRestClient();
            } else {
                throw new MojoExecutionException("Unknown translator: " + translator);
            }
            if(translatorSettings != null) {
                Properties properties = new Properties();
                if(translatorSettings.getProperties() != null) {
                    properties.putAll(translatorSettings.getProperties());
                }
                if(translatorSettings.getPropertyFile() != null) {
                    try {
                        properties.load(new FileInputStream(translatorSettings.getPropertyFile()));
                    } catch(IOException x) {
                        throw new MojoExecutionException("Error loading translator configuration", x);
                    }
                }
                translationService.init(properties);
            }
            try {
                translate(resourceDir, translationService);
            } catch(IOException x) {
                throw new MojoExecutionException("Error during translation", x);
            }
        }
    }

    void translate(File dir, Translator translationService) throws IOException {

        File[] files = dir.listFiles();
        for(File file: files) {
            if(!file.isHidden()) {
                if(file.isFile()) {
                    if(file.getName().endsWith(".po")) {
                        if(file.getName().endsWith(localeSuffixSeparator + sourceLocale + localeFileExtension)) {
                            int len = file.getName().length() - localeSuffixSeparator.length()
                                    - sourceLocale.length() - localeFileExtension.length();
                            String prefix = file.getName().substring(0, len);
                            for(String targetLocale: targetLocales) {
                                File targetFile = new File(file.getParent(), prefix +
                                        localeSuffixSeparator + targetLocale + localeFileExtension);
                                translate(file, targetFile, LocaleUtils.getLocale(sourceLocale),
                                        LocaleUtils.getLocale(targetLocale), translationService,
                                        translatorComment);
                            }
                        }
                    }
                } else if(file.isDirectory()) {
                    translate(file, translationService);
                }
            }
        }
    }

    void translate(File srcFile, File targetFile, Locale srcLocale, Locale targetLocale,
            Translator translator, String comment) throws IOException {

        POReader reader = new POReader();
        POData srcData = reader.read(new FileInputStream(srcFile), "utf8");
        Map<String, POMessage> srcMessages = srcData.getMessages();
        StringBuilder targetContent;
        int count = 0;

        if(targetFile.exists()) {
            POData targetData = reader.read(new FileInputStream(targetFile), "utf8");
            targetContent = new StringBuilder(FileUtils.load(targetFile, "utf8"));
            Map<String, POMessage> targetMessages = targetData.getMessages();
            String lastMsgId = null;
            for(String srcMsgId : srcMessages.keySet()) {
                if(!targetMessages.containsKey(srcMsgId)) {
                    int linePos = 0;
                    if(lastMsgId != null) {
                        POMessage precedingTargetMessage = targetMessages.get(lastMsgId);
                        linePos = precedingTargetMessage.getEndLineNo() + 1;
                    }
                    POMessage srcMessage = srcMessages.get(srcMsgId);
                    String translatedMessage = getTranslatedMessage(srcMessage, comment,
                            translator, srcLocale, targetLocale);
                    insertEntry(targetContent, linePos, translatedMessage);
                    count++;
                    targetData = reader.read(new ByteArrayInputStream(targetContent.toString().getBytes("utf8")), "utf8");
                    targetMessages = targetData.getMessages();
                }
                lastMsgId = srcMsgId;
            }
        } else {
            targetContent = new StringBuilder();
            for(String srcMsgId : srcMessages.keySet()) {
                POMessage srcMessage = srcMessages.get(srcMsgId);
                String translatedMessage = getTranslatedMessage(srcMessage, comment,
                        translator, srcLocale, targetLocale);
                targetContent.append(translatedMessage);
                if(count < srcMessages.size() - 1) {
                    targetContent.append(System.lineSeparator());
                }
                count++;
            }
        }
        if(count > 0) {
            getLog().info("Added " + count + " translated messages to " + targetFile.getAbsolutePath());
            FileUtils.save(targetContent.toString(), targetFile, "utf8");
        }
    }

    String getTranslatedMessage(POMessage msg, String comment, Translator translator,
            Locale srcLocale, Locale targetLocale) {

        StringBuilder sb = new StringBuilder();
        if(comment != null && !comment.isEmpty()) {
            sb.append("# " + comment + System.lineSeparator());
        }
        if(msg.getMessageContext() != null) {
            sb.append("msgctxt \"" + msg.getMessageContext() + "\"" + System.lineSeparator());
        }
        sb.append("msgid \"" + msg.getMessageId() + "\"" + System.lineSeparator());
        if(msg.getMessageIdPlural() != null) {
            sb.append("msgid_plural \"" + msg.getMessageIdPlural() + "\"" + System.lineSeparator());
        }
        String[] msgStrs = msg.getMessageStrings();
        String[] result = translator.translate(srcLocale, targetLocale, msgStrs);
        if(msgStrs.length == 1) {
            sb.append("msgstr \"" + result[0] + "\"" + System.lineSeparator());
        } else if(msgStrs.length > 1) {
            for(int i=0; i<msgStrs.length; i++) {
                sb.append("msgstr[" + i + "] \"" + result[i] + "\"" + System.lineSeparator());
            }
        }
        return sb.toString();
    }

    int getIndexOfLine(StringBuilder content, int lineNo) {
        if(lineNo < 1) {
            throw new IllegalArgumentException("Line number has to be greater than 0");
        }
        int currentLineNo = 1;
        for(int i = 0; i < content.length(); i++) {
            if(lineNo == currentLineNo) {
                return i;
            }
            char ch = content.charAt(i);
            if(ch == '\n') {
                currentLineNo++;
            } else if(ch == '\r') {
                if((i + 1) < content.length() && content.charAt(i + 1) == '\n') {
                    i++;
                }
                currentLineNo++;
            }
        }
        return -1;
    }

    void insertEntry(StringBuilder content, int lineNo, String entry) {
        if(lineNo < 1) {
            throw new IllegalArgumentException("Line number has to be greater than 0");
        }
        if(!(entry.endsWith("\n") || entry.endsWith("\r") || entry.endsWith("\r\n"))) {
            entry += System.lineSeparator();
        }
        int ind = getIndexOfLine(content, lineNo);
        if(ind == -1) {
            char ch = content.charAt(content.length() -1);
            if(!(ch == '\n' || ch == '\r')) {
                content.append(System.lineSeparator());
            }
            if(!hasEmptyLineBefore(content, content.length())) {
                content.append(System.lineSeparator());
            }
            content.append(entry);
        } else {
            entry += System.lineSeparator();
            if(!hasEmptyLineBefore(content, ind)) {
                entry = System.lineSeparator() + entry;
            }
            content.insert(ind, entry);
        }
    }

    boolean hasEmptyLineBefore(StringBuilder content, int startInd) {
        if(startInd > 0) {
            int ind = startInd - 1;
            char ch = content.charAt(ind);
            if(ch == '\n' || ch == '\r') {
                ind--;
                if(ind > -1 && ch == '\n' && content.charAt(ind) == '\r') {
                    ind--;
                }
                while(ind > 0) {
                    ch = content.charAt(ind);
                    if(ch == '\n') {
                        return true;
                    } else if(!Character.isWhitespace(ch)) {
                        return false;
                    }
                    ind--;
                }
            }
        }
        return false;
    }

}
