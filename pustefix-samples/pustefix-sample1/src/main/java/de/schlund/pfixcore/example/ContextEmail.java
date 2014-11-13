package de.schlund.pfixcore.example;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class ContextEmail {
    
    private List<Email> list;
    private PagedListHolder<Email> emailList;
    
    public ContextEmail() {
        list = new ArrayList<Email>();
        Email email = new Email();
        email.setAddress("Peter.Mueller@Fotostudio-Mueller.de");
        email.setType("mailxchange");
        email.setActive(true);
        email.setVirusProtection(true);
        email.setSpamFilter(true);
        list.add(email);
        email = new Email();
        email.setAddress("Sarah.Mueller@Fotostudio-Mueller.de");
        email.setType("mailxchange");
        email.setActive(true);
        email.setVirusProtection(false);
        email.setSpamFilter(false);
        list.add(email);
        email = new Email();
        email.setAddress("info@Fotostudio-Mueller.de");
        email.setType("forward");
        email.setActive(true);
        email.setForwardTarget("Peter.Mueller@Fotostudio-Mueller.de");
        list.add(email);
        email = new Email();
        email.setAddress("Frank.Meier@Fotostudio-Mueller.de");
        email.setType("1and1mail");
        email.setActive(false);
        email.setVirusProtection(true);
        email.setSpamFilter(false);
        list.add(email);
        email = new Email();
        email.setAddress("e492170436");
        email.setType("intermediate");
        list.add(email);
        email = new Email();
        email.setAddress("Peter.Mueller@Fotostudio-Mueller.de");
        email.setType("mailxchange");
        email.setActive(true);
        email.setVirusProtection(true);
        email.setSpamFilter(true);
        list.add(email);
        email = new Email();
        email.setAddress("Sarah.Mueller@Fotostudio-Mueller.de");
        email.setType("mailxchange");
        email.setActive(true);
        email.setVirusProtection(false);
        email.setSpamFilter(false);
        list.add(email);
        email = new Email();
        email.setAddress("info@Fotostudio-Mueller.de");
        email.setType("forward");
        email.setActive(true);
        email.setForwardTarget("Peter.Mueller@Fotostudio-Mueller.de");
        list.add(email);
        email = new Email();
        email.setAddress("Frank.Meier@Fotostudio-Mueller.de");
        email.setType("1and1mail");
        email.setActive(false);
        email.setVirusProtection(true);
        email.setSpamFilter(false);
        list.add(email);
        email = new Email();
        email.setAddress("e492170436");
        email.setType("unknown");
        list.add(email);
        emailList = new PagedListHolder<Email>(list);
    }

    public Page<Email> getEmailList(Pageable pageable) {
        
        if(pageable.getSort() != null) {
            Sort.Order order = pageable.getSort().iterator().next();
            emailList.setSort(new MutableSortDefinition(order.getProperty(), true, order.isAscending()));
            emailList.resort();
        }
        emailList.setPageSize(pageable.getPageSize());
        emailList.setPage(pageable.getPageNumber());
        return new PageImpl<Email>(emailList.getPageList(), pageable, emailList.getSource().size());
    }
    
    public Email getEmail(String address) {
        
        for(Email emailBean: list) {
            if(emailBean.getAddress().equals(address)) {
                return emailBean;
            }
        }
        return null;
    }
    
}
