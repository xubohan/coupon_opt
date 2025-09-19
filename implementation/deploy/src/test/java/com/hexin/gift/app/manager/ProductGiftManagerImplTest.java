package com.hexin.gift.app.manager;

import com.hexin.gift.app.manager.impl.ProductGiftManagerImpl;
import com.hexin.gift.interfaces.rest.query.ListGoodsQuery;
import com.hexin.gift.interfaces.rest.vo.GiftCandidateVO;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;
import com.hexin.gift.modules.gift.domain.service.CandidatePolicy;
import com.hexin.gift.modules.gift.domain.service.EligibilityService;
import com.hexin.gift.modules.gift.domain.service.GiftGrantService;
import com.hexin.gift.modules.gift.domain.service.GoodsAssembler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductGiftManagerImplTest {

    @Mock
    private GoodsAssembler goodsAssembler;

    @Mock
    private CandidatePolicy candidatePolicy;

    @Mock
    private EligibilityService eligibilityService;

    @Mock
    private GiftGrantService giftGrantService;

    @InjectMocks
    private ProductGiftManagerImpl manager;

    @Test
    void listGoods_shouldDelegateToAssembler() {
        List<GoodsBaseVO> goods = Collections.singletonList(new GoodsBaseVO(1L, "name", "PORTFOLIO", 88));
        when(goodsAssembler.listGoods(123)).thenReturn(goods);

        ListGoodsQuery query = new ListGoodsQuery(123);

        List<GoodsBaseVO> result = manager.listGoods(query);

        assertEquals(goods, result);
        verify(goodsAssembler).listGoods(123);
    }

    @Test
    void listCandidates_shouldDelegateToPolicy() {
        GoodsBaseVO selected = new GoodsBaseVO(1L, "portfolio", "PORTFOLIO", 88);
        List<GoodsBaseVO> allGoods = Collections.singletonList(selected);
        List<GiftCandidateVO> expected = Collections.singletonList(new GiftCandidateVO(1, "nick", null, "p", "20240101", 6));
        when(candidatePolicy.listCandidates(selected, allGoods)).thenReturn(expected);

        List<GiftCandidateVO> result = manager.listCandidates(selected, allGoods);

        assertEquals(expected, result);
        verify(candidatePolicy).listCandidates(selected, allGoods);
    }

    @Test
    void checkEligibility_shouldDelegateToService() {
        GoodsBaseVO selected = new GoodsBaseVO(1L, "portfolio", "PORTFOLIO", 88);
        List<Integer> userIds = Collections.singletonList(1001);
        List<Boolean> expected = Collections.singletonList(Boolean.TRUE);
        when(eligibilityService.checkEligibility(selected, userIds)).thenReturn(expected);

        List<Boolean> result = manager.checkEligibility(selected, userIds);

        assertEquals(expected, result);
        verify(eligibilityService).checkEligibility(selected, userIds);
    }

    @Test
    void grantBatch_shouldDelegateToService() {
        GoodsBaseVO selected = new GoodsBaseVO(1L, "portfolio", "PORTFOLIO", 88);
        List<Integer> candidateIds = Collections.singletonList(1001);
        List<Boolean> expected = Collections.singletonList(Boolean.TRUE);
        when(giftGrantService.grantBatch(selected, candidateIds, "period:7")).thenReturn(expected);

        List<Boolean> result = manager.grantBatch(selected, candidateIds, "period:7");

        assertEquals(expected, result);
        verify(giftGrantService).grantBatch(selected, candidateIds, "period:7");
    }
}
