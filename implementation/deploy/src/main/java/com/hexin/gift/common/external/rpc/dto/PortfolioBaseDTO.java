package com.hexin.gift.common.external.rpc.dto;

/**
 * Simplified DTO representing a portfolio base entry from external service.
 */
public class PortfolioBaseDTO {

    private Long portfolioId;
    private String portfolioName;

    public PortfolioBaseDTO() {
    }

    public PortfolioBaseDTO(Long portfolioId, String portfolioName) {
        this.portfolioId = portfolioId;
        this.portfolioName = portfolioName;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(Long portfolioId) {
        this.portfolioId = portfolioId;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }
}
