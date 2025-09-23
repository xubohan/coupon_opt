package com.hexin.gift.modules.gift.domain.service;

import com.hexin.gift.common.external.rpc.PackageBaseApi;
import com.hexin.gift.common.external.rpc.PortfolioBaseApi;
import com.hexin.gift.common.external.rpc.dto.PackageBaseDTO;
import com.hexin.gift.common.external.rpc.dto.PortfolioBaseDTO;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;
import com.hexin.gift.modules.gift.domain.service.impl.GoodsAssemblerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoodsAssemblerTest {

    @Mock
    private PortfolioBaseApi portfolioBaseApi;

    @Mock
    private PackageBaseApi packageBaseApi;

    @InjectMocks
    private GoodsAssemblerImpl goodsAssembler;

    private PortfolioBaseDTO portfolioOne;
    private PackageBaseDTO packageOne;

    @BeforeEach
    void setUp() {
        portfolioOne = new PortfolioBaseDTO(101L, "Advisor Portfolio");
        packageOne = new PackageBaseDTO(201L, "Advisor Package");
    }

    @Test
    void listGoods_shouldMergePortfolioAndPackage() {
        when(portfolioBaseApi.getPortfolioBaseChargeList(1))
                .thenReturn(Collections.singletonList(portfolioOne));
        when(packageBaseApi.getPackageChargeList(1))
                .thenReturn(Collections.singletonList(packageOne));

        List<GoodsBaseVO> goods = goodsAssembler.listGoods(1);

        assertEquals(2, goods.size());
        assertEquals(101L, goods.get(0).getProductId().longValue());
        assertEquals("PORTFOLIO", goods.get(0).getProductType());
        assertEquals(Integer.valueOf(1), goods.get(0).getAdvisorId());
        assertEquals(201L, goods.get(1).getProductId().longValue());
        assertEquals("PACKAGE", goods.get(1).getProductType());
        assertEquals(Integer.valueOf(1), goods.get(1).getAdvisorId());
    }

    @Test
    void listGoods_shouldHandleNullResponses() {
        when(portfolioBaseApi.getPortfolioBaseChargeList(2)).thenReturn(null);
        when(packageBaseApi.getPackageChargeList(2)).thenReturn(Arrays.asList(packageOne, new PackageBaseDTO(null, "invalid")));

        List<GoodsBaseVO> goods = goodsAssembler.listGoods(2);

        assertEquals(1, goods.size());
        assertEquals("PACKAGE", goods.get(0).getProductType());
        assertEquals(Integer.valueOf(2), goods.get(0).getAdvisorId());
    }
}
