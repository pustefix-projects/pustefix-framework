package de.schlund.pfixcore.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.pustefixframework.web.mvc.filter.Filter;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class ContextData {
    
    private List<DataBean> list;
    private PagedListHolder<DataBean> dataList;
    
    public ContextData() {
        
        list = new ArrayList<DataBean>();
        //Set<Long> ids = new HashSet<Long>();
        Random random = new Random();
        for(int i=0; i<100; i++) {
            long id = i;
            //while(id < 1 || ids.contains(id)) {
            //    id = random.nextInt(1000);
            //}
            StringBuilder name = new StringBuilder();
            for(int j=0; j<5; j++) {
                name.append((char)(random.nextInt(26) + 65));
            }
            StringBuilder description = new StringBuilder();
            for(int j=0; j<20; j++) {
                description.append((char)(random.nextInt(26) + 65));
            }
            DataBean dataBean = new DataBean();
            dataBean.setId(id);
            dataBean.setName(name.toString());
            dataBean.setDescription(description.toString());
            dataBean.setEnabled(random.nextBoolean());
            list.add(dataBean);
            dataList = new PagedListHolder<DataBean>(list);
            //dataList.setSort(new MutableSortDefinition("name", true, true));
            //dataList.resort();
            
        }
        
    }
    
    public Page<DataBean> getDataList(Pageable pageable, Filter filter) {
        
        if(filter != null) {
            List<DataBean> filteredList = new ArrayList<DataBean>();
            for(DataBean bean: list) {
                if(filter.isSatisfiedBy(bean)) {
                    filteredList.add(bean);
                }
                dataList = new PagedListHolder<DataBean>(filteredList);
            }
        } else {
            dataList = new PagedListHolder<DataBean>(list);
        }
        
        if(pageable.getSort() != null) {
            Sort.Order order = pageable.getSort().iterator().next();
            dataList.setSort(new MutableSortDefinition(order.getProperty(), true, order.isAscending()));
            dataList.resort();
        }
        
        dataList.setPageSize(pageable.getPageSize());
        dataList.setPage(pageable.getPageNumber());
        return new PageImpl<DataBean>(dataList.getPageList(), pageable, dataList.getSource().size());
    }
    
    public DataBean getData(long dataId) {
        
        for(DataBean dataBean: list) {
            if(dataBean.getId() == dataId) {
                return dataBean;
            }
        }
        return null;
    }
    
}
