package com.hexin.gift.modules.gift.domain.service.impl;

import com.hexin.gift.common.external.rpc.PackageBaseApi;
import com.hexin.gift.common.external.rpc.PortfolioBaseApi;
import com.hexin.gift.common.external.rpc.dto.PackageBaseDTO;
import com.hexin.gift.common.external.rpc.dto.PortfolioBaseDTO;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;
import com.hexin.gift.modules.gift.domain.service.GoodsAssembler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class GoodsAssemblerImpl implements GoodsAssembler {

    private static final String TYPE_PORTFOLIO = "PORTFOLIO";
    private static final String TYPE_PACKAGE = "PACKAGE";

    private final PortfolioBaseApi portfolioBaseApi;
    private final PackageBaseApi packageBaseApi;

    public GoodsAssemblerImpl(PortfolioBaseApi portfolioBaseApi, PackageBaseApi packageBaseApi) {
        this.portfolioBaseApi = portfolioBaseApi;
        this.packageBaseApi = packageBaseApi;
    }

    @Override
    public List<GoodsBaseVO> listGoods(Integer advisorId) {
        List<GoodsBaseVO> goods = new ArrayList<>();

        //using external api, please check
        List<PortfolioBaseDTO> portfolioBaseList = Optional
                .ofNullable(portfolioBaseApi.getportfoliochargebaselist(advisorId))
                .orElse(Collections.emptyList());
        for (PortfolioBaseDTO dto : portfolioBaseList) {
            if (dto == null || dto.getPortfolioId() == null) {
                continue;
            }
            goods.add(new GoodsBaseVO(dto.getPortfolioId(), dto.getPortfolioName(), TYPE_PORTFOLIO, advisorId));
        }

        //using external api, please check
        List<PackageBaseDTO> packageBaseList = Optional
                .ofNullable(packageBaseApi.getpackagebaselist(advisorId))
                .orElse(Collections.emptyList());
        for (PackageBaseDTO dto : packageBaseList) {
            if (dto == null || dto.getPackageId() == null) {
                continue;
            }
            goods.add(new GoodsBaseVO(dto.getPackageId(), dto.getPackageName(), TYPE_PACKAGE, advisorId));
        }

        return goods;
    }
}
