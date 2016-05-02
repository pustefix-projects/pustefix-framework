package de.schlund.pfixxml.targets.cachestat;

import java.util.Iterator;

import de.schlund.pfixxml.targets.SPCache;

public class SPCacheStatProxy<T1,T2> implements SPCache<T1,T2> {
    
    private SPCache<T1, T2> delegate;
    private AdvanceCacheStatistic statistic;
    
    public SPCacheStatProxy(SPCache<T1, T2> delegate, AdvanceCacheStatistic statistic) {
        this.delegate = delegate;
        this.statistic = statistic;
    }
    
    public AdvanceCacheStatistic getStatistic() {
        return statistic;
    }
    
    public void createCache(int capacity) {
        delegate.createCache(capacity);
    }

    public Iterator<T1> getIterator() {
        return delegate.getIterator();
    }

    public T2 getValue(T1 key) {
        T2 value = delegate.getValue(key);
        if(value == null) statistic.registerMiss();
        else statistic.registerHit();
        return value;
    }

    public void setValue(T1 key, T2 value) {
        delegate.setValue(key, value);
    }

    public int getCapacity() {
        return delegate.getCapacity();
    }

    public int getSize() {
        return delegate.getSize();
    }

}
