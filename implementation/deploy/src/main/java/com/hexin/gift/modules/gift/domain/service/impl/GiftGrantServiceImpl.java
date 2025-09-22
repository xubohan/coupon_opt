package com.hexin.gift.modules.gift.domain.service.impl;

import com.hexin.gift.common.external.rpc.ActivityGiftRightsApi;
import com.hexin.gift.common.external.rpc.PackageGoodsApi;
import com.hexin.gift.common.external.rpc.PortfolioGoodsApi;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;
import com.hexin.gift.interfaces.rest.vo.SellInfoAttr;
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
    private static final String TYPE_PORTFOLIO = "PORTFOLIO";
    private static final String TYPE_PACKAGE = "PACKAGE";

    private final ActivityGiftRightsApi activityGiftRightsApi;
    private final PortfolioGoodsApi portfolioGoodsApi;
    private final PackageGoodsApi packageGoodsApi;
    private final Clock clock;

    public GiftGrantServiceImpl(ActivityGiftRightsApi activityGiftRightsApi,
                                PortfolioGoodsApi portfolioGoodsApi,
                                PackageGoodsApi packageGoodsApi) {
        this(activityGiftRightsApi, portfolioGoodsApi, packageGoodsApi, Clock.systemDefaultZone());
    }

    GiftGrantServiceImpl(ActivityGiftRightsApi activityGiftRightsApi,
                         PortfolioGoodsApi portfolioGoodsApi,
                         PackageGoodsApi packageGoodsApi,
                         Clock clock) {
        this.activityGiftRightsApi = activityGiftRightsApi;
        this.portfolioGoodsApi = portfolioGoodsApi;
        this.packageGoodsApi = packageGoodsApi;
        this.clock = clock == null ? Clock.systemDefaultZone() : clock;
    }

    @Override
    public List<Boolean> grantBatch(GoodsBaseVO selectedGood, List<Integer> candidateUserIds, String attr) {
        if (selectedGood == null || candidateUserIds == null || candidateUserIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Boolean> result = new ArrayList<>(candidateUserIds.size());
        for (Integer userId : candidateUserIds) {
            if (userId == null) {
                result.add(Boolean.FALSE);
                continue;
            }
            SellInfoAttr sellInfoAttr = SellInfoAttr.parse(attr);
            Long goodsId = resolveGoodsId(selectedGood);
            String source = buildSource(selectedGood, userId);
            try {
                // 逐个调用外部 Dubbo 接口发放权益，暂无法批量
                activityGiftRightsApi.addactivitygiftrights(goodsId, sellInfoAttr, userId, source);
                //using external api, please check
                result.add(Boolean.TRUE);
            } catch (Exception ex) {
                result.add(Boolean.FALSE);
            }
        }
        return result;
    }

    private Long resolveGoodsId(GoodsBaseVO selectedGood) {
        if (selectedGood == null || selectedGood.getGoodsId() == null) {
            return null;
        }
        Long originalId = selectedGood.getGoodsId();
        if (TYPE_PORTFOLIO.equalsIgnoreCase(selectedGood.getType())) {
            //using external api, please check
            return portfolioGoodsApi.getGoodsIdById(originalId);
        }
        if (TYPE_PACKAGE.equalsIgnoreCase(selectedGood.getType())) {
            //using external api, please check
            return packageGoodsApi.getGoodsIdById(originalId);
        }
        return originalId;
    }

    private String buildSource(GoodsBaseVO selectedGood, Integer userId) {
        int advisor = selectedGood != null && selectedGood.getAdvisorId() != null
                ? selectedGood.getAdvisorId()
                : -1;
        Long goodsId = selectedGood != null ? selectedGood.getGoodsId() : null;
        String type = selectedGood != null ? selectedGood.getType() : null;
        long safeGoodsId = goodsId != null ? goodsId : -1L;
        String safeType = type != null ? type : "UNKNOWN";
        String timestamp = LocalDateTime.now(clock).format(SOURCE_FORMATTER);
        return String.format("Advisor %d granted product {%d, %s} to user %d on %s", advisor, safeGoodsId, safeType, userId, timestamp);
    }
}
