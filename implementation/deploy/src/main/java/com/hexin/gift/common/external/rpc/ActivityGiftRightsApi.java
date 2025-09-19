package com.hexin.gift.common.external.rpc;

import com.hexin.gift.interfaces.rest.vo.SellInfoAttr;

public interface ActivityGiftRightsApi {

    default void addactivitygiftrights(Long goodsId, SellInfoAttr attr, Integer userId, String source) {
        //using external api, please check
    }
}
