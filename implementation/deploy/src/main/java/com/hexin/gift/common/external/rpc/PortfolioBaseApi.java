package com.hexin.gift.common.external.rpc;

import com.hexin.gift.common.external.rpc.dto.PortfolioBaseDTO;

import java.util.Collections;
import java.util.List;

public interface PortfolioBaseApi {

    default List<PortfolioBaseDTO> getportfoliochargebaselist(Integer advisorId) {
        //using external api, please check
        return Collections.emptyList();
    }

    default List<PortfolioBaseDTO> getPortfolioBaseChargeList(Integer advisorId) {
        //using external api, please check
        return Collections.emptyList();
    }
}
