package com.hexin.gift.common.external.rpc.dto;

/**
 * Simplified DTO representing a portfolio base entry from external service.
 */
public class PortfolioBaseDTO {

    private Long portfolioId;
    private String name;

    public PortfolioBaseDTO() {
    }

    public PortfolioBaseDTO(Long portfolioId, String name) {
        this.portfolioId = portfolioId;
        this.name = name;
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
}
