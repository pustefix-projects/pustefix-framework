package de.schlund.pfixcore.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

public class MVCTestState extends MVCControllerState {

    @Autowired
    private ContextData contextData;
    
    @RequestMapping("/mvctest/data")
    public void list(Model model, Pageable pageable) {
    
        model.addAttribute("data", contextData.getDataList(pageable));
        if(pageable.getSort() != null) {
            model.addAttribute("sort", pageable.getSort().iterator().next());
        }
    }
        
    @RequestMapping("/mvctest/data/{dataId}")
    public void details(@PathVariable long dataId, Model model) {
        
        model.addAttribute("data", contextData.getData(dataId));
    }

}
