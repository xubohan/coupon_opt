package com.hexin.gift.modules.gift.domain.service;

import com.hexin.gift.common.external.rpc.PackageStatProvider;
import com.hexin.gift.common.external.rpc.PackageTrackApi;
import com.hexin.gift.common.external.rpc.PortfolioSpendApi;
import com.hexin.gift.common.external.rpc.PortfolioTrackApi;
import com.hexin.gift.common.external.rpc.dto.PackageSpendDTO;
import com.hexin.gift.common.external.rpc.dto.PackageTrackUsersDTO;
import com.hexin.gift.common.external.rpc.dto.PortfolioSpendDubboDTO;
import com.hexin.gift.common.external.rpc.dto.PortfolioTrackUsersDTO;
import com.hexin.gift.interfaces.rest.vo.GiftCandidateVO;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;
import com.hexin.gift.modules.gift.domain.service.impl.CandidatePolicyImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidatePolicyTest {

    @Mock
    private PortfolioTrackApi portfolioTrackApi;

    @Mock
    private PackageTrackApi packageTrackApi;

    @Mock
    private PortfolioSpendApi portfolioSpendApi;

    @Mock
    private PackageStatProvider packageStatProvider;

    @InjectMocks
    private CandidatePolicyImpl candidatePolicy;

    private GoodsBaseVO selectedPortfolio;
    private GoodsBaseVO anotherPortfolio;
    private GoodsBaseVO somePackage;

    @BeforeEach
    void setUp() {
        selectedPortfolio = new GoodsBaseVO(1L, "portfolio-1", "PORTFOLIO", 88);
        anotherPortfolio = new GoodsBaseVO(2L, "portfolio-2", "PORTFOLIO", 88);
        somePackage = new GoodsBaseVO(100L, "package-1", "PACKAGE", 88);
    }

    @Test
    void listCandidates_shouldIncludeSpendInfoFromLatestOrder() {
        PortfolioTrackUsersDTO portfolioUsers = new PortfolioTrackUsersDTO(2L, "Product A", "detailA",
                Arrays.asList(2001, 2002));
        PortfolioTrackUsersDTO selectedUsers = new PortfolioTrackUsersDTO(1L, "Selected", "detailSel",
                Collections.singletonList(3001));
        when(portfolioTrackApi.getPortfolioTrackList(argThat(list -> list != null && list.size() > 1)))
                .thenReturn(Arrays.asList(portfolioUsers, selectedUsers));
        when(portfolioTrackApi.getPortfolioTrackList(argThat(list -> list != null && list.size() == 1
                && list.contains(selectedPortfolio.getProductId()))))
                .thenReturn(Collections.singletonList(selectedUsers));

        PackageTrackUsersDTO packageUsers = new PackageTrackUsersDTO(100L, "Product B", "detailB",
                Arrays.asList(2001, 4001));
        when(packageTrackApi.getPackageTrackList(argThat(list -> list != null && list.size() == 1
                && list.contains(somePackage.getProductId()))))
                .thenReturn(Collections.singletonList(packageUsers));

        LocalDateTime newestStart = LocalDateTime.of(2024, 2, 1, 10, 0);
        LocalDateTime newestEnd = newestStart.plusMonths(3);
        PortfolioSpendDubboDTO latest = new PortfolioSpendDubboDTO(2L, 2001, "Product A", newestStart, newestEnd);
        PortfolioSpendDubboDTO older = new PortfolioSpendDubboDTO(2L, 2001, "Product A", newestStart.minusMonths(2), newestEnd.plusMonths(1));
        when(portfolioSpendApi.getSpendByPortIdBuyer(anyList(), anyList()))
                .thenReturn(Arrays.asList(older, latest));

        long pkgStartMillis = LocalDateTime.of(2024, 3, 5, 12, 0).toInstant(ZoneOffset.UTC).toEpochMilli();
        long pkgEndMillis = LocalDateTime.of(2024, 5, 5, 12, 0).toInstant(ZoneOffset.UTC).toEpochMilli();
        PackageSpendDTO packageSpend = new PackageSpendDTO(100L, 4001, "Product B", pkgStartMillis, pkgEndMillis);
        when(packageStatProvider.getSpendByPackageBuyer(anyList(), anyList()))
                .thenReturn(Collections.singletonList(packageSpend));

        List<GiftCandidateVO> result = candidatePolicy.listCandidates(selectedPortfolio,
                Arrays.asList(selectedPortfolio, anotherPortfolio, somePackage));

        assertEquals(3, result.size());
        GiftCandidateVO userA = result.stream()
                .filter(vo -> vo.getUserId().equals(2001))
                .findFirst()
                .orElseThrow(() -> new AssertionError("userA missing"));
        assertEquals("Product A", userA.getProductName());
        assertEquals("2024-02-01 10:00:00", userA.getPurchaseDate());
        assertEquals(Integer.valueOf(3), userA.getDurationMonths());

        GiftCandidateVO userPackage = result.stream()
                .filter(vo -> vo.getUserId().equals(4001))
                .findFirst()
                .orElseThrow(() -> new AssertionError("userPackage missing"));
        assertEquals("Product B", userPackage.getProductName());
        String expectedPackagePurchase = java.time.Instant.ofEpochMilli(pkgStartMillis)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        assertEquals(expectedPackagePurchase, userPackage.getPurchaseDate());
        assertEquals(Integer.valueOf(2), userPackage.getDurationMonths());

        assertTrue(result.stream().noneMatch(vo -> vo.getUserId().equals(3001)));
    }
}
