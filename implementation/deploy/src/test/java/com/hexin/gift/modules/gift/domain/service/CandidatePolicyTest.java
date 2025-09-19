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
    void listCandidates_shouldExcludeSelectedPaidUsersAndPreferLatestOrder() {
        PortfolioTrackUsersDTO portfolioCandidate = new PortfolioTrackUsersDTO(2001, 2L, "userA",
                "avatarA", "Product A", 1_000L, 900L, 6);
        PortfolioTrackUsersDTO selectedUser = new PortfolioTrackUsersDTO(3001, 1L, "selected",
                null, "Selected Product", 2_000L, 1_500L, 12);
        PackageTrackUsersDTO packageCandidateNewer = new PackageTrackUsersDTO(2001, 100L, "userA",
                "avatarA2", "Product B", 3_000L, 2_500L, 3);
        PackageTrackUsersDTO packageCandidateUnique = new PackageTrackUsersDTO(4001, 100L, "userB",
                "avatarB", "Product C", 2_500L, 2_000L, 1);

        when(portfolioTrackApi.portfoliotracklist(argThat(list -> list != null && list.size() > 1)))
                .thenReturn(Arrays.asList(portfolioCandidate, selectedUser));
        when(portfolioTrackApi.portfoliotracklist(argThat(list -> list != null && list.size() == 1
                && list.contains(selectedPortfolio.getGoodsId()))))
                .thenReturn(Collections.singletonList(selectedUser));

        when(packageTrackApi.packagetracklist(argThat(list -> list != null && list.size() == 1
                && list.contains(somePackage.getGoodsId()))))
                .thenReturn(Arrays.asList(packageCandidateNewer, packageCandidateUnique));

        List<GiftCandidateVO> result = candidatePolicy.listCandidates(selectedPortfolio,
                Arrays.asList(selectedPortfolio, anotherPortfolio, somePackage));

        assertEquals(2, result.size());
        GiftCandidateVO userA = result.stream()
                .filter(vo -> vo.getUserId().equals(2001))
                .findFirst()
                .orElseThrow(() -> new AssertionError("userA missing"));
        assertEquals("Product B", userA.getProductName());
        assertEquals("3000", userA.getPurchaseDate());
        assertEquals(Integer.valueOf(3), userA.getDurationMonths());

        assertTrue(result.stream().noneMatch(vo -> vo.getUserId().equals(3001)));
    }
}
