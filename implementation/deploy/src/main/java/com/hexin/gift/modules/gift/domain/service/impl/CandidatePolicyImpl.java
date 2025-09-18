package com.hexin.gift.modules.gift.domain.service.impl;

import com.hexin.gift.common.external.rpc.PackageTrackApi;
import com.hexin.gift.common.external.rpc.PortfolioTrackApi;
import com.hexin.gift.common.external.rpc.dto.PackageTrackUsersDTO;
import com.hexin.gift.common.external.rpc.dto.PortfolioTrackUsersDTO;
import com.hexin.gift.interfaces.rest.vo.GiftCandidateVO;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;
import com.hexin.gift.modules.gift.domain.service.CandidatePolicy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class CandidatePolicyImpl implements CandidatePolicy {

    private static final String TYPE_PORTFOLIO = "PORTFOLIO";
    private static final String TYPE_PACKAGE = "PACKAGE";

    private final PortfolioTrackApi portfolioTrackApi;
    private final PackageTrackApi packageTrackApi;

    public CandidatePolicyImpl(PortfolioTrackApi portfolioTrackApi,
                               PackageTrackApi packageTrackApi) {
        this.portfolioTrackApi = portfolioTrackApi;
        this.packageTrackApi = packageTrackApi;
    }

    @Override
    public List<GiftCandidateVO> listCandidates(GoodsBaseVO selectedGood, List<GoodsBaseVO> allGoods) {
        if (selectedGood == null || allGoods == null || allGoods.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> allPortfolioIds = new HashSet<>();
        Set<Long> allPackageIds = new HashSet<>();
        for (GoodsBaseVO good : allGoods) {
            if (good == null || good.getGoodsId() == null) {
                continue;
            }
            if (TYPE_PORTFOLIO.equalsIgnoreCase(good.getType())) {
                allPortfolioIds.add(good.getGoodsId());
            } else if (TYPE_PACKAGE.equalsIgnoreCase(good.getType())) {
                allPackageIds.add(good.getGoodsId());
            }
        }

        // 汇总顾问名下所有组合与套餐的付费记录
        List<PortfolioTrackUsersDTO> allPortfolioPaid = Collections.emptyList();
        if (!allPortfolioIds.isEmpty()) {
            //using external api, please check
            List<PortfolioTrackUsersDTO> queried = portfolioTrackApi.portfoliotracklist(new ArrayList<>(allPortfolioIds));
            allPortfolioPaid = queried != null ? queried : Collections.emptyList();
        }
        List<PackageTrackUsersDTO> allPackagePaid = Collections.emptyList();
        if (!allPackageIds.isEmpty()) {
            //using external api, please check
            List<PackageTrackUsersDTO> queried = packageTrackApi.packagetracklist(new ArrayList<>(allPackageIds));
            allPackagePaid = queried != null ? queried : Collections.emptyList();
        }

        // 选中商品的付费用户集合，用于差集
        Set<Integer> selectedPaidUserIds = new HashSet<>();
        if (TYPE_PORTFOLIO.equalsIgnoreCase(selectedGood.getType())) {
            //using external api, please check
            List<PortfolioTrackUsersDTO> selectedPortfolioPaid = portfolioTrackApi
                    .portfoliotracklist(Collections.singletonList(selectedGood.getGoodsId()));
            if (selectedPortfolioPaid != null) {
                for (PortfolioTrackUsersDTO dto : selectedPortfolioPaid) {
                    if (dto != null && dto.getUserId() != null) {
                        selectedPaidUserIds.add(dto.getUserId());
                    }
                }
            }
        } else if (TYPE_PACKAGE.equalsIgnoreCase(selectedGood.getType())) {
            //using external api, please check
            List<PackageTrackUsersDTO> selectedPackagePaid = packageTrackApi
                    .packagetracklist(Collections.singletonList(selectedGood.getGoodsId()));
            if (selectedPackagePaid != null) {
                for (PackageTrackUsersDTO dto : selectedPackagePaid) {
                    if (dto != null && dto.getUserId() != null) {
                        selectedPaidUserIds.add(dto.getUserId());
                    }
                }
            }
        }

        // 以用户为粒度挑选最新订单信息
        Map<Integer, AggregateCandidate> aggregate = new HashMap<>();
        for (PortfolioTrackUsersDTO dto : allPortfolioPaid) {
            if (dto == null || dto.getUserId() == null) {
                continue;
            }
            if (dto.getPortfolioId() != null && dto.getPortfolioId().equals(selectedGood.getGoodsId())
                    && TYPE_PORTFOLIO.equalsIgnoreCase(selectedGood.getType())) {
                continue;
            }
            if (selectedPaidUserIds.contains(dto.getUserId())) {
                continue;
            }
            mergeCandidate(aggregate, dto.getUserId(), dto.getNickName(), dto.getAvatarUrl(),
                    dto.getProductName(), dto.getSubscribeStart(), dto.getPurchaseTime(), dto.getDurationMonths());
        }
        for (PackageTrackUsersDTO dto : allPackagePaid) {
            if (dto == null || dto.getUserId() == null) {
                continue;
            }
            if (dto.getPackageId() != null && dto.getPackageId().equals(selectedGood.getGoodsId())
                    && TYPE_PACKAGE.equalsIgnoreCase(selectedGood.getType())) {
                continue;
            }
            if (selectedPaidUserIds.contains(dto.getUserId())) {
                continue;
            }
            mergeCandidate(aggregate, dto.getUserId(), dto.getNickName(), dto.getAvatarUrl(),
                    dto.getProductName(), dto.getSubscribeStart(), dto.getPurchaseTime(), dto.getDurationMonths());
        }

        List<GiftCandidateVO> result = new ArrayList<>(aggregate.size());
        for (AggregateCandidate candidate : aggregate.values()) {
            result.add(candidate.toVO());
        }
        return result;
    }

    private void mergeCandidate(Map<Integer, AggregateCandidate> aggregate,
                                Integer userId,
                                String nickName,
                                String avatarUrl,
                                String productName,
                                Long subscribeStart,
                                Long purchaseTime,
                                Integer durationMonths) {
        AggregateCandidate existing = aggregate.get(userId);
        if (existing == null) {
            aggregate.put(userId, new AggregateCandidate(userId, nickName, avatarUrl, productName,
                    subscribeStart, purchaseTime, durationMonths));
            return;
        }
        if (isBetter(subscribeStart, purchaseTime, existing)) {
            existing.update(productName, nickName, avatarUrl, subscribeStart, purchaseTime, durationMonths);
        }
    }

    private boolean isBetter(Long subscribeStart, Long purchaseTime, AggregateCandidate existing) {
        if (subscribeStart != null && (existing.subscribeStart == null || subscribeStart > existing.subscribeStart)) {
            return true;
        }
        if (Objects.equals(subscribeStart, existing.subscribeStart)) {
            if (purchaseTime != null && (existing.purchaseTime == null || purchaseTime > existing.purchaseTime)) {
                return true;
            }
        }
        return false;
    }

    private static final class AggregateCandidate {
        private final Integer userId;
        private String productName;
        private String nickName;
        private String avatarUrl;
        private Long subscribeStart;
        private Long purchaseTime;
        private Integer durationMonths;

        private AggregateCandidate(Integer userId,
                                   String nickName,
                                   String avatarUrl,
                                   String productName,
                                   Long subscribeStart,
                                   Long purchaseTime,
                                   Integer durationMonths) {
            this.userId = userId;
            this.nickName = nickName;
            this.avatarUrl = avatarUrl;
            this.productName = productName;
            this.subscribeStart = subscribeStart;
            this.purchaseTime = purchaseTime;
            this.durationMonths = durationMonths;
        }

        private void update(String productName,
                             String nickName,
                             String avatarUrl,
                             Long subscribeStart,
                             Long purchaseTime,
                             Integer durationMonths) {
            this.productName = productName;
            this.nickName = nickName;
            this.avatarUrl = avatarUrl;
            this.subscribeStart = subscribeStart;
            this.purchaseTime = purchaseTime;
            this.durationMonths = durationMonths;
        }

        private GiftCandidateVO toVO() {
            String purchaseDate = subscribeStart != null ? String.valueOf(subscribeStart)
                    : (purchaseTime != null ? String.valueOf(purchaseTime) : null);
            return new GiftCandidateVO(userId, nickName, avatarUrl, productName, purchaseDate, durationMonths);
        }
    }
}
