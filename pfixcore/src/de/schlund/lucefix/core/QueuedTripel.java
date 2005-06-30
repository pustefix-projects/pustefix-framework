package de.schlund.lucefix.core;

/**
 * @author schuppi
 * @date Jun 14, 2005
 */
public class QueuedTripel extends TripelImpl implements QueueElement {

    private QueuedTripel next;
    
    /**
     * @param product
     * @param part
     * @param filename
     * @param next
     */
    public QueuedTripel(String product, String part, String filename, QueuedTripel next, byte type) {
        super(product, part, filename, type);
        this.next = next;
    }
    public QueuedTripel(Tripel tripel, QueuedTripel next){
        super(tripel.getProduct(),tripel.getPart(),tripel.getFilename(),tripel.getType());
        this.next = next;
    }
    /*
     * @see de.schlund.lucefix.core.QueueElement#next()
     */
    public QueueElement next() {
        return next;
    }
    
    public QueuedTripel nextTripel(){
        return next;
    }

    /*
     * @see de.schlund.lucefix.core.QueueElement#setNext(de.schlund.lucefix.core.QueueElement)
     */
    public void setNext(QueuedTripel next) {
        this.next = next;
    }

    /*
     * @see de.schlund.lucefix.core.QueueElement#setNext(de.schlund.lucefix.core.QueueElement)
     */
    public void setNext(QueueElement next) {
        if (next instanceof QueuedTripel)
            setNext((QueuedTripel)next);
        throw new RuntimeException(next + " is not a valid QueuedTripel");
    }

}
