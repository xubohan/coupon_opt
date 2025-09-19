package com.hexin.gift.app.manager;

import com.hexin.gift.interfaces.rest.query.ListGoodsQuery;
import com.hexin.gift.interfaces.rest.vo.GiftCandidateVO;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;

import java.util.List;

/**
 * Application service facade for gift related operations.
 */
public interface ProductGiftManager {

    List<GoodsBaseVO> listGoods(ListGoodsQuery query);

    List<GiftCandidateVO> listCandidates(GoodsBaseVO selectedGood, List<GoodsBaseVO> allGoods);

    List<Boolean> checkEligibility(GoodsBaseVO selectedGood, List<Integer> userIds);

    List<Boolean> grantBatch(GoodsBaseVO selectedGood, List<Integer> candidateUserIds, Integer attr, Integer advisorId);
}
