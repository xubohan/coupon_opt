package com.hexin.gift.common.external.rpc.dto;

public class PackageSpendDTO {

    private Long packageId;
    private Integer buyerId;
    private String packageName;
    private Long startTime;
    private Long endTime;

    public PackageSpendDTO() {
    }

    public PackageSpendDTO(Long packageId, Integer buyerId, String packageName, Long startTime, Long endTime) {
        this.packageId = packageId;
        this.buyerId = buyerId;
        this.packageName = packageName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public Integer getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Integer buyerId) {
        this.buyerId = buyerId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}
