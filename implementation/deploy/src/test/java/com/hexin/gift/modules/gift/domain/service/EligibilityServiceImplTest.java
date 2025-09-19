package com.hexin.gift.modules.gift.domain.service;

import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;
import com.hexin.gift.modules.gift.domain.service.impl.EligibilityServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EligibilityServiceImplTest {

    private final EligibilityServiceImpl eligibilityService = new EligibilityServiceImpl();

    @Test
    void checkEligibility_shouldReturnEmptyWhenNoUsers() {
        GoodsBaseVO good = new GoodsBaseVO(1L, "portfolio", "PORTFOLIO", 88);
        List<Boolean> result = eligibilityService.checkEligibility(good, Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void checkEligibility_shouldReturnFalsePlaceholders() {
        GoodsBaseVO good = new GoodsBaseVO(1L, "portfolio", "PORTFOLIO", 88);
        List<Boolean> result = eligibilityService.checkEligibility(good, Arrays.asList(1001, 1002));
        assertEquals(Arrays.asList(Boolean.FALSE, Boolean.FALSE), result);
    }
}
