package com.hexin.gift.interfaces.rest.vo;

import javax.validation.constraints.NotNull;

/**
 * Minimal view object describing a purchasable good exposed to the UI.
 */
public class GoodsBaseVO {

    @NotNull
    private Long productId;

    @NotNull
    private String productName;

    @NotNull
    private String productType;

    @NotNull
    private Integer advisorId;

    public GoodsBaseVO() {
    }

    public GoodsBaseVO(Long productId, String productName, String productType, Integer advisorId) {
        this.productId = productId;
        this.productName = productName;
        this.productType = productType;
        this.advisorId = advisorId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public Integer getAdvisorId() {
        return advisorId;
    }

    public void setAdvisorId(Integer advisorId) {
        this.advisorId = advisorId;
    }
}
