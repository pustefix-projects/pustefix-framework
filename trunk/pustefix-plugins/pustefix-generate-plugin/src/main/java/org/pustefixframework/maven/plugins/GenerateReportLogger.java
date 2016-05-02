package org.pustefixframework.maven.plugins;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.maven.plugin.logging.Log;

public class GenerateReportLogger extends Logger {
    
    private Log log;
    
    public GenerateReportLogger(Log log) {
        super(null, null);
        this.log = log;
    }
    
    @Override
    public void log(LogRecord record) {
        Level level = record.getLevel();
        String message = record.getMessage();
        Throwable error = record.getThrown();
        if(level == Level.SEVERE) {
            log.error(message, error);
        } else if (level == Level.WARNING) {
            log.warn(message);
        } else if(level == Level.INFO) {
            log.info(message);
        } else {
            log.debug(message);
        }
    }

}
