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
            // TODO 调用外部 Dubbo 接口校验是否已购买当前产品
            if (TYPE_PORTFOLIO.equalsIgnoreCase(selectedGood.getProductType())) {
                //using external api, please check
            } else if (TYPE_PACKAGE.equalsIgnoreCase(selectedGood.getProductType())) {
                //using external api, please check
            }
            result.add(Boolean.FALSE); // 占位返回，接口待实现
        }
        return result;
    }
}
