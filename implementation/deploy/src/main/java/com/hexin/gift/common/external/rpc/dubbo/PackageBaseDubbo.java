package com.hexin.gift.common.external.rpc.dubbo;

import com.hexin.gift.common.external.rpc.dto.PackageBaseDTO;

import java.util.List;

public interface PackageBaseDubbo {

    List<PackageBaseDTO> getpackagebaselist(Integer advisorId);

    default List<PackageBaseDTO> getPackageChargeList(Integer advisorId) {
        return getpackagebaselist(advisorId);
    }
}
