package de.schlund.pfixxml.perflogging;

import java.util.LinkedHashMap;

import de.schlund.pfixxml.PfixServletRequest;

public interface AdditionalTrailInfo {
    LinkedHashMap<String,Object> getData(PfixServletRequest preq);
    void reset();
}
