package com.hexin.gift.modules.gift.domain.service;

import com.hexin.gift.common.external.rpc.PackageTrackApi;
import com.hexin.gift.common.external.rpc.PortfolioTrackApi;
import com.hexin.gift.common.external.rpc.dto.PackageTrackUsersDTO;
import com.hexin.gift.common.external.rpc.dto.PortfolioTrackUsersDTO;
import com.hexin.gift.interfaces.rest.vo.GiftCandidateVO;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;
import com.hexin.gift.modules.gift.domain.service.impl.CandidatePolicyImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidatePolicyTest {

    @Mock
    private PortfolioTrackApi portfolioTrackApi;

    @Mock
    private PackageTrackApi packageTrackApi;

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
    void listCandidates_shouldExcludeSelectedPaidUsers() {
        PortfolioTrackUsersDTO portfolioCandidate = new PortfolioTrackUsersDTO(2L, "Product A", "detailA",
                Arrays.asList(2001, 2002));
        PortfolioTrackUsersDTO selectedUsers = new PortfolioTrackUsersDTO(1L, "Selected Product", "detailSel",
                Collections.singletonList(3001));
        PackageTrackUsersDTO packageCandidateUnique = new PackageTrackUsersDTO(100L, "Product B", "detailB",
                Arrays.asList(2001, 4001));

        when(portfolioTrackApi.getPortfolioTrackList(argThat(list -> list != null && list.size() > 1)))
                .thenReturn(Arrays.asList(portfolioCandidate, selectedUsers));
        when(portfolioTrackApi.getPortfolioTrackList(argThat(list -> list != null && list.size() == 1
                && list.contains(selectedPortfolio.getGoodsId()))))
                .thenReturn(Collections.singletonList(selectedUsers));

        when(packageTrackApi.getPackageTrackList(argThat(list -> list != null && list.size() == 1
                && list.contains(somePackage.getGoodsId()))))
                .thenReturn(Collections.singletonList(packageCandidateUnique));

        List<GiftCandidateVO> result = candidatePolicy.listCandidates(selectedPortfolio,
                Arrays.asList(selectedPortfolio, anotherPortfolio, somePackage));

        assertEquals(3, result.size());
        assertTrue(result.stream().noneMatch(vo -> vo.getUserId().equals(3001)));
        GiftCandidateVO userA = result.stream()
                .filter(vo -> vo.getUserId().equals(2001))
                .findFirst()
                .orElseThrow(() -> new AssertionError("userA missing"));
        assertEquals("Product A", userA.getProductName());
        assertTrue(result.stream().anyMatch(vo -> vo.getUserId().equals(2002)));
        assertTrue(result.stream().anyMatch(vo -> vo.getUserId().equals(4001)));
    }
}
