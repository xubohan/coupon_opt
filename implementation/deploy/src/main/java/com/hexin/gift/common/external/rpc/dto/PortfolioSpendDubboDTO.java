package com.hexin.gift.common.external.rpc.dto;

import java.time.LocalDateTime;

public class PortfolioSpendDubboDTO {

    private Long portfolioId;
    private Integer buyerId;
    private String productName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public PortfolioSpendDubboDTO() {
    }

    public PortfolioSpendDubboDTO(Long portfolioId, Integer buyerId, String productName, LocalDateTime startAt, LocalDateTime endAt) {
        this.portfolioId = portfolioId;
        this.buyerId = buyerId;
        this.productName = productName;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(Long portfolioId) {
        this.portfolioId = portfolioId;
    }

    public Integer getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Integer buyerId) {
        this.buyerId = buyerId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }
}
