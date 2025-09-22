package com.hexin.gift.common.external.rpc;

import com.hexin.gift.common.external.rpc.dto.PackageSpendDTO;

import java.util.Collections;
import java.util.List;

public interface PackageStatProvider {

    default List<PackageSpendDTO> getSpendByPackageBuyer(List<Long> packageIds, List<Integer> buyers) {
        //using external api, please check
        return Collections.emptyList();
    }
}
