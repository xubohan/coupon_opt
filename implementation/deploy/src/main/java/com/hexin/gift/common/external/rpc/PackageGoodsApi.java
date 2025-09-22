package com.hexin.gift.common.external.rpc;

public interface PackageGoodsApi {

    default Long getGoodsIdById(Long packageId) {
        //using external api, please check
        return packageId;
    }
}
