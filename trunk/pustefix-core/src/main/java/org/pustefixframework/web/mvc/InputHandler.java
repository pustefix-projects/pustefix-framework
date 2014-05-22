package org.pustefixframework.web.mvc;

import de.schlund.pfixcore.generator.IWrapper;

/**
 * Successor of the {@link de.schlund.pfixcore.generator.IHandler} interface.
 * 
 * Should be preferred because its generic (you don't have to cast the IWrapper objects)
 * and the methods don't declare to throw {@link Exception} (which enforces sensible 
 * handling of exceptions thrown in the application code).
 * 
 */
public interface InputHandler<T extends IWrapper> {

    public void handleSubmittedData(T wrapper);
    public void retrieveCurrentStatus(T wrapper);
    public boolean prerequisitesMet();
    public boolean isActive();
    public boolean needsData();

}
