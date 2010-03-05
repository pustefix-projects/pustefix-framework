package de.schlund.pfixxml.targets;

import java.io.File;
import java.io.Writer;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.schlund.pfixxml.config.BuildTimeProperties;
import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * Class used by Maven plugin to generate targets.
 * 
 * @author mleidig@schlund.de
 *
 */
public class TargetGeneratorRunner {
    
    public boolean run(File docroot, File config, File cache, String mode, Writer output, String logLevel) throws Exception {
        
        if(!docroot.exists()) throw new Exception("TargetGenerator docroot " + docroot.getAbsolutePath() + " doesn't exist");
        if(!config.exists()) throw new Exception("TargetGenerator configuration " + config.getAbsolutePath() + " doesn't exist");
        
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout("[%p] %c - %m\n"));
        Logger logger=Logger.getRootLogger();
        logger.setLevel(Level.toLevel(logLevel));
        logger.addAppender(appender);
        
        Properties props = new Properties();
        props.setProperty("mode", mode);
        BuildTimeProperties.setProperties(props);
        
        GlobalConfigurator.setDocroot(docroot.getPath());
        FileResource confFile = ResourceUtil.getFileResource(config.toURI());
        if(!cache.exists()) cache.mkdirs();
        FileResource cacheDir = ResourceUtil.getFileResource(cache.toURI());
        try { 
            TargetGeneratorFactory genFactory = TargetGeneratorFactory.getInstance();
            TargetGenerator gen = genFactory.createGenerator(confFile, cacheDir);
            gen.setIsGetModTimeMaybeUpdateSkipped(false);
            gen.generateAll();
            output.write(TargetGenerator.getReportAsString());
        } catch(Exception x) {
            throw new Exception("Generating targets failed", x);
        }
        return !TargetGenerator.errorsReported();
    }
    
}
