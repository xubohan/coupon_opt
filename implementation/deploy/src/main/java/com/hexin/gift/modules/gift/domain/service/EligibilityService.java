package com.hexin.gift.modules.gift.domain.service;

import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;

import java.util.List;

/**
 * Determines whether users are eligible for the selected good.
 */
public interface EligibilityService {

    List<Boolean> checkEligibility(GoodsBaseVO selectedGood, List<Integer> userIds);
}
