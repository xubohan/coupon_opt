package com.hexin.gift.common.external.rpc.dto;

import java.util.List;

/**
 * External DTO describing package subscribers.
 */
public class PackageTrackUsersDTO {

    private Long packageId;
    private String name;
    private String detailUrl;
    private List<Integer> userIds;

    public PackageTrackUsersDTO() {
    }

    public PackageTrackUsersDTO(Long packageId,
                                 String name,
                                 String detailUrl,
                                 List<Integer> userIds) {
        this.packageId = packageId;
        this.name = name;
        this.detailUrl = detailUrl;
        this.userIds = userIds;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
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
