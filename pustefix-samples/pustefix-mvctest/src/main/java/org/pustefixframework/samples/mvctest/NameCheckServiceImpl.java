package org.pustefixframework.samples.mvctest;

import org.springframework.stereotype.Service;

@Service
public class NameCheckServiceImpl implements NameCheckService {

    @Override
    public boolean isValid(String name) {
        return name.length() > 2;
    }

}
