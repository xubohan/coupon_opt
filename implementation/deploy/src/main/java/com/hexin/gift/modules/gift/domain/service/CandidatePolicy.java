package com.hexin.gift.modules.gift.domain.service;

import com.hexin.gift.interfaces.rest.vo.GiftCandidateVO;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;

import java.util.List;

/**
 * Encapsulates gifting candidate derivation logic.
 */
public interface CandidatePolicy {

    List<GiftCandidateVO> listCandidates(GoodsBaseVO selectedGood, List<GoodsBaseVO> allGoods);
}
