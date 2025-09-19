package com.hexin.gift.interfaces.rest.vo;

import javax.validation.constraints.NotNull;

/**
 * Minimal view object describing a purchasable good exposed to the UI.
 */
public class GoodsBaseVO {

    @NotNull
    private Long goodsId;

    @NotNull
    private String goodsName;

    @NotNull
    private String type;

    @NotNull
    private Integer advisorId;

    public GoodsBaseVO() {
    }

    public GoodsBaseVO(Long goodsId, String goodsName, String type, Integer advisorId) {
        this.goodsId = goodsId;
        this.goodsName = goodsName;
        this.type = type;
        this.advisorId = advisorId;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getAdvisorId() {
        return advisorId;
    }

    public void setAdvisorId(Integer advisorId) {
        this.advisorId = advisorId;
    }
}
