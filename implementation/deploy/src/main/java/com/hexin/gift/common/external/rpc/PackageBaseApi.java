package com.hexin.gift.common.external.rpc;

import com.hexin.gift.common.external.rpc.dto.PackageBaseDTO;

import java.util.Collections;
import java.util.List;

public interface PackageBaseApi {

    default List<PackageBaseDTO> getpackagebaselist(Integer advisorId) {
        //using external api, please check
        return Collections.emptyList();
    }
}
