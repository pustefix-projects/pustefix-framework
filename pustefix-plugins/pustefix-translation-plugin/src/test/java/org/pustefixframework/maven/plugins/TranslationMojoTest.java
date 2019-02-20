package org.pustefixframework.maven.plugins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import de.schlund.pfixxml.util.FileUtils;

public class TranslationMojoTest {

    @Test
    public void testIndexOfLine() {

        TranslationMojo mojo = new TranslationMojo();
        StringBuilder str = new StringBuilder("foo\nbar\rbaz\r\nhey\n\n");

        assertEquals(0, mojo.getIndexOfLine(str, 1));
        assertEquals(4, mojo.getIndexOfLine(str, 2));
        assertEquals(8, mojo.getIndexOfLine(str, 3));
        assertEquals(13, mojo.getIndexOfLine(str, 4));
        assertEquals(17, mojo.getIndexOfLine(str, 5));
        assertEquals(-1, mojo.getIndexOfLine(str, 6));

        str = new StringBuilder("");
        assertEquals(-1, mojo.getIndexOfLine(str, 1));

        str = new StringBuilder("\n");
        assertEquals(0, mojo.getIndexOfLine(str, 1));
        assertEquals(-1, mojo.getIndexOfLine(str, 2));

        str = new StringBuilder("foo");
        assertEquals(0, mojo.getIndexOfLine(str, 1));
        assertEquals(-1, mojo.getIndexOfLine(str, 2));

        assertThrows(IllegalArgumentException.class, () -> {
            mojo.getIndexOfLine(new StringBuilder("foo"), -1);
        });
    }

    @Test
    public void testInsertEntry() {

        TranslationMojo mojo = new TranslationMojo();
        StringBuilder str = new StringBuilder("foo\n\nbar\r\nbaz\n");

        mojo.insertEntry(str, 1, "111");
        mojo.insertEntry(str, 6, "333");
        mojo.insertEntry(str, 9, "555");
        mojo.insertEntry(str, 20, "777");
        assertEquals("\n111\n\nfoo\n\n333\n\nbar\r\n\n555\n\nbaz\n\n777\n", str.toString());

        assertThrows(IllegalArgumentException.class, () -> {
            mojo.insertEntry(str, 0, "000");
        });
    }

    @Test
    public void testEmptyLineCheck() {

        TranslationMojo mojo = new TranslationMojo();
        StringBuilder str = new StringBuilder("01\n34\n\n78\n \n2\n");

        assertFalse(mojo.hasEmptyLineBefore(str, 3));
        assertTrue(mojo.hasEmptyLineBefore(str, 7));
        assertFalse(mojo.hasEmptyLineBefore(str, 3));
        assertTrue(mojo.hasEmptyLineBefore(str, 12));
        assertFalse(mojo.hasEmptyLineBefore(str, 13));

        str = new StringBuilder("0");
        assertFalse(mojo.hasEmptyLineBefore(str, 0));

        str = new StringBuilder("\n\n");
        assertFalse(mojo.hasEmptyLineBefore(str, 1));

        str = new StringBuilder("0\r\n\r\n5");
        assertTrue(mojo.hasEmptyLineBefore(str, 5));

        str = new StringBuilder("foo\n");
        assertFalse(mojo.hasEmptyLineBefore(str, 4));

        str = new StringBuilder("fo\n\n");
        assertTrue(mojo.hasEmptyLineBefore(str, 4));
    }

    @Test
    public void testTranslation() throws IOException {

        TranslationMojo mojo = new TranslationMojo();

        Locale srcLocale = Locale.ENGLISH;
        Locale targetLocale = Locale.GERMAN;
        Translator translator = new TestTranslationService();
        File srcFile = new File("src/test/resources/test1_en.po");
        File preFile = new File("src/test/resources/test1_pre_de.po");
        File postFile = new File("src/test/resources/test1_post_de.po");
        File testDir = new File("target/generated-test-resources");
        if(!testDir.exists()) {
            testDir.mkdirs();
        }
        File targetFile = new File(testDir, "test1_de.po");
        FileUtils.copyFile(preFile, targetFile);
        mojo.translate(srcFile, targetFile, srcLocale, targetLocale, translator, "TODO: review");

        String expectedContent = FileUtils.load(postFile, "utf8");
        String actualContent = FileUtils.load(targetFile, "utf8");
        assertEquals(expectedContent, actualContent);
    }

    @Test
    public void testNewTranslation() throws IOException {

        TranslationMojo mojo = new TranslationMojo();

        Locale srcLocale = Locale.ENGLISH;
        Locale targetLocale = Locale.GERMAN;
        Translator translator = new TestTranslationService();
        File srcFile = new File("src/test/resources/test2_en.po");

        File postFile = new File("src/test/resources/test2_post_de.po");
        File testDir = new File("target/generated-test-resources");
        if(!testDir.exists()) {
            testDir.mkdirs();
        }
        File targetFile = new File(testDir, "test2_de.po");

        mojo.translate(srcFile, targetFile, srcLocale, targetLocale, translator, "TODO: review");

        String expectedContent = FileUtils.load(postFile, "utf8");
        String actualContent = FileUtils.load(targetFile, "utf8");
        assertEquals(expectedContent, actualContent);
    }

    @Test
    public void testUnsortedTranslation() throws IOException {

        TranslationMojo mojo = new TranslationMojo();

        Locale srcLocale = Locale.ENGLISH;
        Locale targetLocale = Locale.GERMAN;
        Translator translator = new TestTranslationService();
        File srcFile = new File("src/test/resources/test3_en.po");
        File preFile = new File("src/test/resources/test3_pre_de.po");
        File postFile = new File("src/test/resources/test3_post_de.po");
        File testDir = new File("target/generated-test-resources");
        if(!testDir.exists()) {
            testDir.mkdirs();
        }
        File targetFile = new File(testDir, "test3_de.po");
        FileUtils.copyFile(preFile, targetFile);
        mojo.translate(srcFile, targetFile, srcLocale, targetLocale, translator, "TODO: review");

        String expectedContent = FileUtils.load(postFile, "utf8");
        String actualContent = FileUtils.load(targetFile, "utf8");
        assertEquals(expectedContent, actualContent);
    }


    class TestTranslationService implements Translator {

        @Override
        public void init(Properties properties) {
        }

        @Override
        public String[] translate(Locale sourceLocale, Locale targetLocale, String[] text) {
            String[] result = new String[text.length];
            for(int i=0; i<text.length; i++) {
                result[i] = "[" + text[i] + "]";
            }
            return result;
        }
    }

}
