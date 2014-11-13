package de.schlund.pfixcore.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import de.schlund.pfixcore.workflow.app.DefaultIWrapperState;


public class MVCTestState extends DefaultIWrapperState {

    @Autowired
    private ContextData contextData;
    
    @RequestMapping("/mvctest")
    public void list(Model model, Pageable pageable, Filter filter) {
      
        model.addAttribute("data", contextData.getDataList(pageable, filter));
        if(pageable.getSort() != null) {
            model.addAttribute("sort", pageable.getSort().iterator().next());
        }
        if(filter.getProperty() != null) {
            model.addAttribute("filter", filter);
        }
    }
        
    @RequestMapping("/mvctest/data/{dataId}")
    public void details(@PathVariable long dataId, Model model) {
        
        model.addAttribute("data", contextData.getData(dataId));
    }

}
