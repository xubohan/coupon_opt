package com.hexin.gift.modules.gift.domain.service.impl;

import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;
import com.hexin.gift.modules.gift.domain.service.EligibilityService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class EligibilityServiceImpl implements EligibilityService {

    private static final String TYPE_PORTFOLIO = "PORTFOLIO";
    private static final String TYPE_PACKAGE = "PACKAGE";

    // TODO Dubbo 依赖待对接
    // private final UserPaidStatusApi userPaidStatusApi;
    // private final PortfolioTrackApi portfolioTrackApi;
    // private final PackageTrackApi packageTrackApi;

    public EligibilityServiceImpl() {
    }

    @Override
    public List<Boolean> checkEligibility(GoodsBaseVO selectedGood, List<Integer> userIds) {
        if (selectedGood == null || userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Boolean> result = new ArrayList<>(userIds.size());
        for (Integer userId : userIds) {
            if (userId == null) {
                result.add(Boolean.FALSE);
                continue;
            }
            boolean paid = false;
            // TODO 调用外部Dubbo检查全局付费状态：userPaidStatusApi.isPaid(userId)
            //using external api, please check
            if (!paid) {
                result.add(Boolean.FALSE);
                continue;
            }
            boolean paidCurrent = false;
            if (TYPE_PORTFOLIO.equalsIgnoreCase(selectedGood.getType())) {
                // TODO 调用外部Dubbo检查组合是否付费：portfolioTrackApi.isUserPaidForPortfolio(userId, selectedGood.getGoodsId())
                //using external api, please check
            } else if (TYPE_PACKAGE.equalsIgnoreCase(selectedGood.getType())) {
                // TODO 调用外部Dubbo检查套餐是否付费：packageTrackApi.isUserPaidForPackage(userId, selectedGood.getGoodsId())
                //using external api, please check
            }
            result.add(!paidCurrent);
        }
        return result;
    }
}
