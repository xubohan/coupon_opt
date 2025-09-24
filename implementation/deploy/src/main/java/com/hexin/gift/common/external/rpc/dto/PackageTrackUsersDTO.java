package com.hexin.gift.common.external.rpc.dto;

import java.util.List;

/**
 * External DTO describing package subscribers.
 */
public class PackageTrackUsersDTO {

    private Integer packageId;
    private String name;
    private String detailUrl;
    private List<Integer> userIds;

    public PackageTrackUsersDTO() {
    }

    public PackageTrackUsersDTO(Integer packageId,
                                 String name,
                                 String detailUrl,
                                 List<Integer> userIds) {
        this.packageId = packageId;
        this.name = name;
        this.detailUrl = detailUrl;
        this.userIds = userIds;
    }

    public Integer getPackageId() {
        return packageId;
    }

    public void setPackageId(Integer packageId) {
        this.packageId = packageId;
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
