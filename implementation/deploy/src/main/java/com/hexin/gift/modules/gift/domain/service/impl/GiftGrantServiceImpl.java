package com.hexin.gift.modules.gift.domain.service.impl;

import com.hexin.gift.common.external.rpc.ActivityGiftRightsApi;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;
import com.hexin.gift.modules.gift.domain.service.GiftGrantService;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GiftGrantServiceImpl implements GiftGrantService {

    private static final DateTimeFormatter SOURCE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ActivityGiftRightsApi activityGiftRightsApi;
    private final Clock clock;

    public GiftGrantServiceImpl(ActivityGiftRightsApi activityGiftRightsApi) {
        this(activityGiftRightsApi, Clock.systemDefaultZone());
    }

    GiftGrantServiceImpl(ActivityGiftRightsApi activityGiftRightsApi, Clock clock) {
        this.activityGiftRightsApi = activityGiftRightsApi;
        this.clock = clock == null ? Clock.systemDefaultZone() : clock;
    }

    @Override
    public List<Boolean> grantBatch(GoodsBaseVO selectedGood, List<Integer> candidateUserIds, Integer attr, Integer advisorId) {
        if (selectedGood == null || candidateUserIds == null || candidateUserIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Boolean> result = new ArrayList<>(candidateUserIds.size());
        for (Integer userId : candidateUserIds) {
            if (userId == null) {
                result.add(Boolean.FALSE);
                continue;
            }
            String source = buildSource(advisorId, selectedGood, userId);
            try {
                // 逐个调用外部 Dubbo 接口发放权益，暂无法批量
                activityGiftRightsApi.addactivitygiftrights(selectedGood.getGoodsId(), attr, userId, source);
                //using external api, please check
                result.add(Boolean.TRUE);
            } catch (Exception ex) {
                result.add(Boolean.FALSE);
            }
        }
        return result;
    }

    private String buildSource(Integer advisorId, GoodsBaseVO selectedGood, Integer userId) {
        int advisor = advisorId != null ? advisorId : -1;
        Long goodsId = selectedGood != null ? selectedGood.getGoodsId() : null;
        String type = selectedGood != null ? selectedGood.getType() : null;
        long safeGoodsId = goodsId != null ? goodsId : -1L;
        String safeType = type != null ? type : "UNKNOWN";
        String timestamp = LocalDateTime.now(clock).format(SOURCE_FORMATTER);
        return String.format("Advisor %d granted product {%d, %s} to user %d on %s", advisor, safeGoodsId, safeType, userId, timestamp);
    }
}
