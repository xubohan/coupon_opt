package com.hexin.gift.modules.gift.domain.service.impl;

import com.hexin.gift.common.external.rpc.ActivityGiftRightsApi;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GiftGrantServiceImplTest {

    @Mock
    private ActivityGiftRightsApi activityGiftRightsApi;

    private GiftGrantServiceImpl giftGrantService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2024-01-01T08:30:15Z"), ZoneOffset.UTC);
        giftGrantService = new GiftGrantServiceImpl(activityGiftRightsApi, fixedClock);
    }

    @Test
    void grantBatch_shouldReturnTrueWhenSuccess() {
        GoodsBaseVO good = new GoodsBaseVO(1L, "portfolio", "PORTFOLIO");
        doNothing().when(activityGiftRightsApi).addactivitygiftrights(anyLong(), anyInt(), anyInt(), anyString());

        List<Boolean> result = giftGrantService.grantBatch(good, Collections.singletonList(1001), 7, 88);

        assertEquals(Collections.singletonList(Boolean.TRUE), result);
        ArgumentCaptor<String> sourceCaptor = ArgumentCaptor.forClass(String.class);
        verify(activityGiftRightsApi).addactivitygiftrights(eq(1L), eq(7), eq(1001), sourceCaptor.capture());
        assertEquals("Advisor 88 granted product {1, PORTFOLIO} to user 1001 on 2024-01-01 08:30:15",
                sourceCaptor.getValue());
    }

    @Test
    void grantBatch_shouldReturnFalseWhenException() {
        GoodsBaseVO good = new GoodsBaseVO(1L, "portfolio", "PORTFOLIO");
        doNothing().when(activityGiftRightsApi)
                .addactivitygiftrights(eq(1L), eq(7), eq(1001), anyString());
        doThrow(new RuntimeException("fail"))
                .when(activityGiftRightsApi)
                .addactivitygiftrights(eq(1L), eq(7), eq(1002), anyString());

        List<Boolean> result = giftGrantService.grantBatch(good, Arrays.asList(1001, 1002), 7, 88);

        assertEquals(Arrays.asList(Boolean.TRUE, Boolean.FALSE), result);
        verify(activityGiftRightsApi).addactivitygiftrights(eq(1L), eq(7), eq(1001),
                eq("Advisor 88 granted product {1, PORTFOLIO} to user 1001 on 2024-01-01 08:30:15"));
        verify(activityGiftRightsApi).addactivitygiftrights(eq(1L), eq(7), eq(1002),
                eq("Advisor 88 granted product {1, PORTFOLIO} to user 1002 on 2024-01-01 08:30:15"));
    }
}
