package com.hexin.gift.common.external.rpc.dto;

/**
 * Simplified DTO representing a package base entry from external service.
 */
public class PackageBaseDTO {

    private Long packageId;
    private String packageName;

    public PackageBaseDTO() {
    }

    public PackageBaseDTO(Long packageId, String packageName) {
        this.packageId = packageId;
        this.packageName = packageName;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
