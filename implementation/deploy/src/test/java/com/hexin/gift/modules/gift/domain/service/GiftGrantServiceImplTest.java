package com.hexin.gift.modules.gift.domain.service;

import com.hexin.gift.common.external.rpc.ActivityGiftRightsApi;
import com.hexin.gift.interfaces.rest.vo.GiftCandidateVO;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;
import com.hexin.gift.modules.gift.domain.service.impl.GiftGrantServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GiftGrantServiceImplTest {

    @Mock
    private ActivityGiftRightsApi activityGiftRightsApi;

    @InjectMocks
    private GiftGrantServiceImpl giftGrantService;

    @Test
    void grantBatch_shouldReturnTrueWhenSuccess() {
        GoodsBaseVO good = new GoodsBaseVO(1L, "portfolio", "PORTFOLIO");
        GiftCandidateVO candidate = new GiftCandidateVO(1001, "nick", null, "product", "20240101", 6);
        doNothing().when(activityGiftRightsApi).addactivitygiftrights(1L, 7, 1001, "source");

        List<Boolean> result = giftGrantService.grantBatch(good, Collections.singletonList(candidate), 7, "source");

        assertEquals(Collections.singletonList(Boolean.TRUE), result);
        verify(activityGiftRightsApi).addactivitygiftrights(1L, 7, 1001, "source");
    }

    @Test
    void grantBatch_shouldReturnFalseWhenException() {
        GoodsBaseVO good = new GoodsBaseVO(1L, "portfolio", "PORTFOLIO");
        GiftCandidateVO candidateOK = new GiftCandidateVO(1001, "nick", null, "product", "20240101", 6);
        GiftCandidateVO candidateFail = new GiftCandidateVO(1002, "nick2", null, "product2", "20240102", 3);
        doNothing().when(activityGiftRightsApi).addactivitygiftrights(1L, 7, 1001, "source");
        doThrow(new RuntimeException("fail")).when(activityGiftRightsApi).addactivitygiftrights(1L, 7, 1002, "source");

        List<Boolean> result = giftGrantService.grantBatch(good, Arrays.asList(candidateOK, candidateFail), 7, "source");

        assertEquals(Arrays.asList(Boolean.TRUE, Boolean.FALSE), result);
        verify(activityGiftRightsApi).addactivitygiftrights(1L, 7, 1001, "source");
        verify(activityGiftRightsApi).addactivitygiftrights(1L, 7, 1002, "source");
    }
}
