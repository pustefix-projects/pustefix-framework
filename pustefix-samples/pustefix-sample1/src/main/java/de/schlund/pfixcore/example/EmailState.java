package de.schlund.pfixcore.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

public class EmailState extends MVCControllerState {

    @Autowired
    private ContextEmail contextEmail;
    
    @RequestMapping("/mvctest/data")
    public void list(Model model, Pageable pageable) {
    
        model.addAttribute("data", contextEmail.getEmailList(pageable));
        if(pageable.getSort() != null) {
            model.addAttribute("sort", pageable.getSort().iterator().next());
        }
    }
        
    @RequestMapping("/mvctest/data/{address}")
    public void details(@PathVariable String address, Model model) {
        
        model.addAttribute("data", contextEmail.getEmail(address));
    }

}
