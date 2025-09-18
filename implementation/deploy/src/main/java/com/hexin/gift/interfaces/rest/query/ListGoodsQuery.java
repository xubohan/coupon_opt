package com.hexin.gift.interfaces.rest.query;

import javax.validation.constraints.NotNull;

/**
 * Query object for listing goods by advisor.
 */
public class ListGoodsQuery {

    @NotNull
    private Integer advisorId;

    public ListGoodsQuery() {
    }

    public ListGoodsQuery(Integer advisorId) {
        this.advisorId = advisorId;
    }

    public Integer getAdvisorId() {
        return advisorId;
    }

    public void setAdvisorId(Integer advisorId) {
        this.advisorId = advisorId;
    }
}
