package de.schlund.lucefix.core;

/**
 * @author schuppi
 * @date Jun 14, 2005
 */
public class Queue {

    private QueuedTripel head  = null;
    private QueuedTripel tail  = null;
    private Object       mutex = new Object();
    private int          size  = 0;

    public void add(Tripel newtripel) {
        synchronized (mutex) {
            if (tail == null) {
                head = new QueuedTripel(newtripel, null);
                tail = head;
            } else {
                tail.setNext(new QueuedTripel(newtripel, null));
                tail = tail.nextTripel();
            }
            size++;
        }
    }

    public void addPrio(Tripel newtripel) {
        synchronized (mutex) {
            if (head == null) {
                head = new QueuedTripel(newtripel, null);
                tail = head;
            } else {
                QueuedTripel newelem = new QueuedTripel(newtripel, head);
                head = newelem;
            }
            size++;
        }
    }

    public Tripel next() {
        synchronized (mutex) {
            if (head == null) return null;
            
            size--;
            Tripel retval = head;
            if (head.next() == null) {
                head = null;
                tail = null;
                return retval;
            } else {
                head = head.nextTripel();
                return retval;
            }
        }
    }
    public int getSize(){
        return size;
    }
}