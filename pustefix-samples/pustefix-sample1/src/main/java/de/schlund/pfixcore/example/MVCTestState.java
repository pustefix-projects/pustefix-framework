package de.schlund.pfixcore.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.schlund.pfixcore.workflow.app.StaticState;

@SessionAttributes("myfilter")
public class MVCTestState extends StaticState {

    private ContextData contextData;

    @ModelAttribute("myfilter")
    public DataFilter getDataFilter() {
        //Setup initial filter, e.g. get settings from preference storage
        DataFilter dataFilter = new DataFilter();
        //dataFilter.getFilter().setEnabled(true);
        return dataFilter;
    }
    
    @RequestMapping("/mvctest")
    public void list(Model model, Pageable pageable, @ModelAttribute("myfilter") DataFilter filter) {
        if(pageable.getSort() == null) {
            pageable = new PageRequest(0, 10, new Sort(Sort.Direction.ASC, "id"));
        }
        model.addAttribute("data", contextData.getDataList(pageable, filter.getPropertyFilter()));
        if(filter != null) {
            model.addAttribute("filter", filter);
        }
    }

    @RequestMapping("/mvctest/data/{dataId}")
    public void details(@PathVariable long dataId, Model model) {
        model.addAttribute("data", contextData.getData(dataId));
    }
    
    @Autowired
    public void setContextData(ContextData contextData) {
        this.contextData = contextData;
    }

}
