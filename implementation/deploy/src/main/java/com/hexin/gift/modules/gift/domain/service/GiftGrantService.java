package com.hexin.gift.modules.gift.domain.service;

import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;

import java.util.List;

/**
 * Handles batch granting of gift rights for candidates.
 */
public interface GiftGrantService {

    List<Boolean> grantBatch(GoodsBaseVO selectedGood, List<Integer> candidateUserIds, String attr);
}
