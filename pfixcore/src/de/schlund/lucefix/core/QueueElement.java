package de.schlund.lucefix.core;

/**
 * @author schuppi
 * @date Jun 14, 2005
 */
public interface QueueElement {
    public QueueElement next();
    public void setNext(QueueElement next);
}
