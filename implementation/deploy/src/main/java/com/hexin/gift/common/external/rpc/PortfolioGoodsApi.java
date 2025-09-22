package com.hexin.gift.common.external.rpc;

public interface PortfolioGoodsApi {

    default Long getGoodsIdById(Long portfolioId) {
        //using external api, please check
        return portfolioId;
    }
}
