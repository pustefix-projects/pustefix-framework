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
package de.schlund.pfixxml.targets.cachestat;

/**
 * Reports statistics for a single Pustefix Cache.
 *
 * @author ffray
 *
 */
public class DetailedCacheStatistics implements DetailedCacheStatisticsMBean {

    private final SPCacheStatProxy<?, ?> statProxy;

    public DetailedCacheStatistics(SPCacheStatProxy<?, ?> statProxy) {
        this.statProxy = statProxy;
    }

    @Override
    public String getId() {
        return statProxy.getStatistic().getId();
    }

    @Override
    public int getSize() {
        return statProxy.getSize();
    }

    @Override
    public int getCapacity() {
        return statProxy.getCapacity();
    }

    @Override
    public double getSaturation() {
        return (double) statProxy.getSize() / statProxy.getCapacity()  * 100;
    }

    @Override
    public long getHits() {
        return statProxy.getStatistic().getHits();
    }

    @Override
    public long getMisses() {
        return statProxy.getStatistic().getMisses();
    }

    @Override
    public double getHitrate() {
        return calcHitrate(statProxy.getStatistic().getHits(), statProxy.getStatistic().getMisses());
    }

    private double calcHitrate(double hits, double misses) {
        double rate = 0;
        if (hits != 0 && (hits + misses != 0)) {
            rate = (hits / (misses + hits)) * 100;
            if (rate > 100) {
                rate = 100;
            }
        }
        return rate;
    }

}