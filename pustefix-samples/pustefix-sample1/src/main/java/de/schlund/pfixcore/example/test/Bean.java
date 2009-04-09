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
 *
 */
package de.schlund.pfixcore.example.test;

import java.util.ArrayList;
import java.util.List;

import de.schlund.pfixcore.generator.annotation.Caster;
import de.schlund.pfixcore.generator.annotation.IWrapper;

/**
 * @author mleidig@schlund.de
 */
@IWrapper(ihandler = BeanDummyHandler.class)
public class Bean {

    private String text;
    private ArrayList<Integer> values;
    public List<Float> floatValues;
    private Data data;
    private List<Data> dataList;

    public Bean() {

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ArrayList<Integer> getValues() {
        return values;
    }

    public void setValues(ArrayList<Integer> values) {
        this.values = values;
    }

    @Caster(type = DataCaster.class)
    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Caster(type = DataCaster.class)
    public List<Data> getDataList() {
        return dataList;
    }

    public void setDataList(List<Data> dataList) {
        this.dataList = dataList;
    }

}
