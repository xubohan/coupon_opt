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

    public GoodsBaseVO() {
    }

    public GoodsBaseVO(Long goodsId, String goodsName, String type) {
        this.goodsId = goodsId;
        this.goodsName = goodsName;
        this.type = type;
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
}
