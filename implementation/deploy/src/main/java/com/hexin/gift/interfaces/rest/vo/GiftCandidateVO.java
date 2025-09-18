package com.hexin.gift.interfaces.rest.vo;

import javax.validation.constraints.NotNull;

/**
 * Candidate view exposed to the UI for gifting operations.
 */
public class GiftCandidateVO {

    @NotNull
    private Integer userId;

    private String nickName;

    private String avatarUrl;

    private String productName;

    private String purchaseDate;

    private Integer durationMonths;

    public GiftCandidateVO() {
    }

    public GiftCandidateVO(Integer userId,
                           String nickName,
                           String avatarUrl,
                           String productName,
                           String purchaseDate,
                           Integer durationMonths) {
        this.userId = userId;
        this.nickName = nickName;
        this.avatarUrl = avatarUrl;
        this.productName = productName;
        this.purchaseDate = purchaseDate;
        this.durationMonths = durationMonths;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }
}
