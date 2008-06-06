/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package de.schlund.pfixcore.generator.iwrpgen;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import de.schlund.pfixcore.example.test.Bean;
import de.schlund.pfixcore.example.test.BeanWrapper;
import de.schlund.pfixcore.example.test.Data;
import de.schlund.pfixxml.config.GlobalConfigurator;

/**
 * @author mleidig@schlund.de
 */
public class IWrapperBeanTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        GlobalConfigurator.setDocroot((new File("projects").getAbsolutePath()));
    }

    public void testWrapperToBean() throws Exception {

        Bean bean = new Bean();
        bean.setText("text");
        ArrayList<Integer> values = new ArrayList<Integer>();
        values.add(1);
        values.add(2);
        values.add(3);
        bean.setValues(values);
        List<Float> floatValues = new ArrayList<Float>();
        floatValues.add(1.2f);
        bean.floatValues = floatValues;
        Data data = new Data();
        data.setText("datatext");
        bean.setData(data);
        List<Data> dataList = new ArrayList<Data>();
        dataList.add(data);
        bean.setDataList(dataList);

        // Manually init wrapper

        BeanWrapper wrapper = new BeanWrapper();
        wrapper.init("test");
        wrapper.setStringValText("text");
        wrapper.setStringValValues(new String[] { "1", "2", "3" });
        wrapper.setStringValFloatValues(new String[] { "1.2" });
        wrapper.setStringValData("datatext");
        wrapper.setStringValDataList(new String[] { "datatext" });
        wrapper.loadFromStringValues();

        assertEquals(wrapper.getText(), "text");
        assertEquals(wrapper.getValues().length, values.size());
        assertEquals(wrapper.getValues()[0], values.get(0));
        assertEquals(wrapper.getValues()[1], values.get(1));
        assertEquals(wrapper.getValues()[2], values.get(2));
        assertEquals(wrapper.getFloatValues().length, floatValues.size());
        assertEquals(wrapper.getFloatValues()[0], floatValues.get(0));
        assertEquals(wrapper.getData().getText(), "datatext");
        assertEquals(wrapper.getDataList()[0].getText(), "datatext");

        // Create bean from wrapper

        Bean myBean = IWrapperToBean.createBean(wrapper, Bean.class);

        assertEquals(myBean.getText(), "text");
        assertEquals(myBean.getValues(), values);
        assertEquals(myBean.floatValues, floatValues);
        assertEquals(myBean.getData().getText(), "datatext");
        assertEquals(myBean.getDataList().get(0).getText(), "datatext");

        // Init wrapper with bean

        BeanWrapper myWrapper = new BeanWrapper();
        myWrapper.init("test");
        BeanToIWrapper.populateIWrapper(myBean, myWrapper);
        myWrapper.loadFromStringValues();
        assertEquals(myWrapper.getText(), "text");
        assertEquals(myWrapper.getValues().length, values.size());
        assertEquals(myWrapper.getValues()[0], values.get(0));
        assertEquals(myWrapper.getValues()[1], values.get(1));
        assertEquals(myWrapper.getValues()[2], values.get(2));
        assertEquals(myWrapper.getFloatValues().length, floatValues.size());
        assertEquals(myWrapper.getFloatValues()[0], floatValues.get(0));
        assertEquals(myWrapper.getData().getText(), "datatext");
        assertEquals(myWrapper.getDataList()[0].getText(), "datatext");

    }

}
