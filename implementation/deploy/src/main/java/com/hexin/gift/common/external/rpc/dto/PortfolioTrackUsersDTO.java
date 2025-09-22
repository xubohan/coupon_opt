package com.hexin.gift.common.external.rpc.dto;

import java.util.List;

/**
 * External DTO describing portfolio subscribers.
 */
public class PortfolioTrackUsersDTO {

    private Long portfolioId;
    private String name;
    private String detailUrl;
    private List<Integer> userIds;

    public PortfolioTrackUsersDTO() {
    }

    public PortfolioTrackUsersDTO(Long portfolioId,
                                   String name,
                                   String detailUrl,
                                   List<Integer> userIds) {
        this.portfolioId = portfolioId;
        this.name = name;
        this.detailUrl = detailUrl;
        this.userIds = userIds;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(Long portfolioId) {
        this.portfolioId = portfolioId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public List<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
    }
}
