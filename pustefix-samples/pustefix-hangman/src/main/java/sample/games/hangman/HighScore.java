package sample.games.hangman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;

public class HighScore implements ServletContextAware, Serializable, InitializingBean {
    
    private static final long serialVersionUID = -1250893468425754371L;
    
    private static final Logger LOG = Logger.getLogger(HighScore.class);
    
    private static int MAX_SIZE = 10;
    
    private SortedSet<Score> scores = new TreeSet<Score>();
    private ServletContext servletContext;
    
    public void afterPropertiesSet() throws Exception {
        deserialize();
    }
    
    public synchronized int addScore(Score score) {
        scores.add(score);
        if(scores.size() > MAX_SIZE) scores.remove(scores.last());
        int ind = -1;
        for(Score s: scores) {
            ind++;
            if(score == s) {
                serialize();
                return ind;
            }
        }
        return -1;
    }
    
    public synchronized Score[] getScores() {
        return scores.toArray(new Score[scores.size()]);
    }

    private void serialize() {
        try {
            File tmpDir = (File)servletContext.getAttribute("javax.servlet.context.tempdir");
            if(tmpDir != null && tmpDir.exists()) {
                File dataFile = new File(tmpDir, "highscore.ser");
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dataFile));
                out.writeObject(scores);
                out.close();
            }
        } catch(IOException x) {
            LOG.warn("Error while serializing highscore", x);
        }
    }

    @SuppressWarnings("unchecked")
    private void deserialize() {
        try {
            File tmpDir = (File)servletContext.getAttribute("javax.servlet.context.tempdir");
            if(tmpDir != null && tmpDir.exists()) {
                File dataFile = new File(tmpDir, "highscore.ser");
                if(dataFile.exists()) {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(dataFile));
                    scores = (SortedSet<Score>)in.readObject();
                }
            }
        } catch(Exception x) {
            LOG.warn("Error while deserializing highscore", x);
        }
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
}
