package com.hexin.gift.modules.gift.domain.service.impl;

import com.hexin.gift.common.external.rpc.ActivityGiftRightsApi;
import com.hexin.gift.interfaces.rest.vo.GiftCandidateVO;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;
import com.hexin.gift.modules.gift.domain.service.GiftGrantService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GiftGrantServiceImpl implements GiftGrantService {

    private final ActivityGiftRightsApi activityGiftRightsApi;

    public GiftGrantServiceImpl(ActivityGiftRightsApi activityGiftRightsApi) {
        this.activityGiftRightsApi = activityGiftRightsApi;
    }

    @Override
    public List<Boolean> grantBatch(GoodsBaseVO selectedGood, List<GiftCandidateVO> candidates, Integer attr, String source) {
        if (selectedGood == null || candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        List<Boolean> result = new ArrayList<>(candidates.size());
        for (GiftCandidateVO candidate : candidates) {
            if (candidate == null || candidate.getUserId() == null) {
                result.add(Boolean.FALSE);
                continue;
            }
            try {
                // 逐个调用外部 Dubbo 接口发放权益，暂无法批量
                activityGiftRightsApi.addactivitygiftrights(selectedGood.getGoodsId(), attr, candidate.getUserId(), source);
                //using external api, please check
                result.add(Boolean.TRUE);
            } catch (Exception ex) {
                result.add(Boolean.FALSE);
            }
        }
        return result;
    }
}
