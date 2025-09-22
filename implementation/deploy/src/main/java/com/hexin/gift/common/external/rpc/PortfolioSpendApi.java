package com.hexin.gift.common.external.rpc;

import com.hexin.gift.common.external.rpc.dto.PortfolioSpendDubboDTO;

import java.util.Collections;
import java.util.List;

public interface PortfolioSpendApi {

    default List<PortfolioSpendDubboDTO> getSpendByPortIdBuyer(List<Long> portfolioIdList, List<Integer> buyerIdList) {
        //using external api, please check
        return Collections.emptyList();
    }
}
