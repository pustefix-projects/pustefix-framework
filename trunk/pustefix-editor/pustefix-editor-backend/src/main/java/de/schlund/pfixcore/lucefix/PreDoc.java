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

package de.schlund.pfixcore.lucefix;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;


public class PreDoc {
    


    public static final String ATTRIBVALUES = "attribvalues";
    public static final String ATTRIBKEYS = "attribkeys";
    public static final String TAGS = "tags";
    public static final String COMMENTS = "comments";
    public static final String CONTENTS = "contents";
    public static final String LASTTOUCH = "lasttouch";
    public static final String PATH = "path";
    public static final String FILENAME = "file";
    public static final PerFieldAnalyzerWrapper ANALYZER = new PerFieldAnalyzerWrapper(new StandardAnalyzer());
    static{
        ANALYZER.addAnalyzer(TAGS, new WhitespaceAnalyzer());
        ANALYZER.addAnalyzer(COMMENTS, new WhitespaceAnalyzer());
        ANALYZER.addAnalyzer(ATTRIBKEYS, new WhitespaceAnalyzer());
        ANALYZER.addAnalyzer(ATTRIBVALUES, new WhitespaceAnalyzer());
        ANALYZER.addAnalyzer(CONTENTS, new StandardAnalyzer());
    } 
    
    @Override
    public String toString() {
        return this.getClass() + "\n\tFilename: " + filename + "\n\tPath: " + path + "\n\tLasttouch: " + lasttouch
                + "\n\tcomments: " + comments + "\n\ttags: " + tags + "\n\tattribKeys: " + attribKeys
                + "\n\tattribValues: " + attribValues + "\n\tcontent: " + content;
    }

    /**
     * @return
     */
    public Document toLuceneDocument() {
        Document doc = new Document();
        doc.add(Field.Keyword(PATH,path));
        doc.add(Field.Keyword(FILENAME,filename));
        doc.add(Field.Keyword(LASTTOUCH, DateField.timeToString(lasttouch)));
        
        doc.add(Field.Text(CONTENTS,  content.toString()));
        doc.add(Field.Text(COMMENTS, comments.toString()));
        doc.add(Field.Text(TAGS, tags.toString()));
        doc.add(Field.Text(ATTRIBKEYS, attribKeys.toString()));
        doc.add(Field.Text(ATTRIBVALUES, attribValues.toString()));
        return doc;
    }

    public PreDoc(String filename, String partname, String productname, long lasttouch) {
        this.lasttouch = lasttouch;
        this.filename = filename;
        this.path = filename + "/" + partname + "/" + productname;
    }

    private String       path;
    private String       filename;
    private long         lasttouch;
    private StringBuffer comments     = new StringBuffer();
    private StringBuffer tags         = new StringBuffer();
    private StringBuffer attribKeys   = new StringBuffer();
    private StringBuffer attribValues = new StringBuffer();
    private StringBuffer content      = new StringBuffer();

    void addComment(String newcomment) {
        if (comments.length() != 0) comments.append(' ');
        comments.append(newcomment);
    }

    void addComment(char newchar) {
        comments.append(newchar);
    }

    void addTag(String newtag) {
        if (tags.length() != 0) tags.append(' ');
        tags.append(newtag);
    }

    void addAttribKey(String newattribkey) {
        if (attribKeys.length() != 0) attribKeys.append(' ');
        attribKeys.append(newattribkey);
    }

    void addAttribValue(String newattribvalue) {
        if (attribValues.length() != 0) attribValues.append(' ');
        attribValues.append(newattribvalue);
    }

    void addContent(String newcontent) {
        if (content.length() != 0) content.append(' ');
        content.append(newcontent);
    }

    void addContent(char charc) {
        content.append(charc);
    }

    public StringBuffer getAttribKeys() {
        return attribKeys;
    }

    public void setAttribKeys(StringBuffer attribKeys) {
        this.attribKeys = attribKeys;
    }

    public StringBuffer getAttribValues() {
        return attribValues;
    }

    public void setAttribValues(StringBuffer attribValues) {
        this.attribValues = attribValues;
    }

    public StringBuffer getComments() {
        return comments;
    }

    public void setComments(StringBuffer comments) {
        this.comments = comments;
    }

    public StringBuffer getContent() {
        return content;
    }

    public void setContent(StringBuffer content) {
        this.content = content;
    }

    public StringBuffer getTags() {
        return tags;
    }

    public void setTags(StringBuffer tags) {
        this.tags = tags;
    }
}