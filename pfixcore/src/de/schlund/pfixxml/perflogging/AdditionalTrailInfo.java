package de.schlund.pfixxml.perflogging;

import de.schlund.pfixxml.PfixServletRequest;
import java.util.LinkedHashMap;

public interface AdditionalTrailInfo {
    LinkedHashMap<String,Object> getData(PfixServletRequest preq);
    void reset();
}
