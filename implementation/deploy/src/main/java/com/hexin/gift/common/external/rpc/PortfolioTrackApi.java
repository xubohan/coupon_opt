package com.hexin.gift.common.external.rpc;

import com.hexin.gift.common.external.rpc.dto.PortfolioTrackUsersDTO;

import java.util.Collections;
import java.util.List;

public interface PortfolioTrackApi {

    default List<PortfolioTrackUsersDTO> portfoliotracklist(List<Long> portfolioIds) {
        //using external api, please check
        return Collections.emptyList();
    }

    default List<PortfolioTrackUsersDTO> getPortfolioTrackList(List<Long> portfolioIds) {
        //using external api, please check
        return Collections.emptyList();
    }
}
