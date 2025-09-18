package com.hexin.gift.common.external.rpc.dto;

/**
 * External DTO describing package subscribers.
 */
public class PackageTrackUsersDTO {

    private Integer userId;
    private Long packageId;
    private String nickName;
    private String avatarUrl;
    private String productName;
    private Long subscribeStart;
    private Long purchaseTime;
    private Integer durationMonths;

    public PackageTrackUsersDTO() {
    }

    public PackageTrackUsersDTO(Integer userId,
                                 Long packageId,
                                 String nickName,
                                 String avatarUrl,
                                 String productName,
                                 Long subscribeStart,
                                 Long purchaseTime,
                                 Integer durationMonths) {
        this.userId = userId;
        this.packageId = packageId;
        this.nickName = nickName;
        this.avatarUrl = avatarUrl;
        this.productName = productName;
        this.subscribeStart = subscribeStart;
        this.purchaseTime = purchaseTime;
        this.durationMonths = durationMonths;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
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

    public Long getSubscribeStart() {
        return subscribeStart;
    }

    public void setSubscribeStart(Long subscribeStart) {
        this.subscribeStart = subscribeStart;
    }

    public Long getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(Long purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }
}
