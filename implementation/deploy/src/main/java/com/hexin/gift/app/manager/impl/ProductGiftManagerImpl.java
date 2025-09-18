package com.hexin.gift.app.manager.impl;

import com.hexin.gift.app.manager.ProductGiftManager;
import com.hexin.gift.interfaces.rest.query.ListGoodsQuery;
import com.hexin.gift.interfaces.rest.vo.GiftCandidateVO;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;
import com.hexin.gift.modules.gift.domain.service.GoodsAssembler;
import com.hexin.gift.modules.gift.domain.service.CandidatePolicy;
import com.hexin.gift.modules.gift.domain.service.EligibilityService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ProductGiftManagerImpl implements ProductGiftManager {

    private final GoodsAssembler goodsAssembler;
    private final CandidatePolicy candidatePolicy;
    private final EligibilityService eligibilityService;

    public ProductGiftManagerImpl(GoodsAssembler goodsAssembler,
                                  CandidatePolicy candidatePolicy,
                                  EligibilityService eligibilityService) {
        this.goodsAssembler = goodsAssembler;
        this.candidatePolicy = candidatePolicy;
        this.eligibilityService = eligibilityService;
    }

    @Override
    public List<GoodsBaseVO> listGoods(ListGoodsQuery query) {
        if (query == null) {
            return Collections.emptyList();
        }
        return goodsAssembler.listGoods(query.getAdvisorId());
    }

    @Override
    public List<GiftCandidateVO> listCandidates(GoodsBaseVO selectedGood, List<GoodsBaseVO> allGoods) {
        return candidatePolicy.listCandidates(selectedGood, allGoods);
    }

    @Override
    public List<Boolean> checkEligibility(GoodsBaseVO selectedGood, List<Integer> userIds) {
        return eligibilityService.checkEligibility(selectedGood, userIds);
    }

    @Override
    public List<Boolean> grantBatch(GoodsBaseVO selectedGood, List<GiftCandidateVO> candidates, Integer attr, String source) {
        // TODO delegate to GiftGrantService when implemented
        return Collections.emptyList();
    }
}
