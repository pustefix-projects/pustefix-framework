/*
 * Created on 23.04.2004
 */
package de.schlund.pfixxml.targets;


/**
 * @author Niels Schelbach
 * 23.04.2004
 */
public interface TargetGeneratorListener {
    
    
    public void finishedTarget(Target target);
    public void abortedTargetGeneration(Target target);
    public void generationException(Target target, TargetGenerationException exception);

    public boolean needsStop();

}
