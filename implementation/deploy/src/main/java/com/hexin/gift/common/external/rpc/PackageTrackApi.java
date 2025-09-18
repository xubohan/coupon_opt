package com.hexin.gift.common.external.rpc;

import com.hexin.gift.common.external.rpc.dto.PackageTrackUsersDTO;

import java.util.Collections;
import java.util.List;

public interface PackageTrackApi {

    default List<PackageTrackUsersDTO> packagetracklist(List<Long> packageIds) {
        //using external api, please check
        return Collections.emptyList();
    }
}
