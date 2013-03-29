package org.jboss.errai.aerogear.api.pipeline;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class wraps and builds the query parameters for filtering and pagination
 */
public class ReadFilter implements Serializable {

    private Integer limit = Integer.MAX_VALUE;
    private Integer offset = 0;

    private Map<String, String> where = new HashMap<String, String>();

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Map<String, String> getWhere() {
        return where;
    }

    public void setWhere(Map where) {
        this.where = where;
    }
}
