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
            List<PortfolioTrackUsersDTO> queried = portfolioTrackApi.getPortfolioTrackList(new ArrayList<>(allPortfolioIds));
            allPortfolioPaid = queried != null ? queried : Collections.emptyList();
        }
        List<PackageTrackUsersDTO> allPackagePaid = Collections.emptyList();
        if (!allPackageIds.isEmpty()) {
            //using external api, please check
            List<PackageTrackUsersDTO> queried = packageTrackApi.getPackageTrackList(new ArrayList<>(allPackageIds));
            allPackagePaid = queried != null ? queried : Collections.emptyList();
        }

        // 选中商品的付费用户集合，用于差集
        Set<Integer> selectedPaidUserIds = new HashSet<>();
        if (TYPE_PORTFOLIO.equalsIgnoreCase(selectedGood.getType())) {
            //using external api, please check
            List<PortfolioTrackUsersDTO> selectedPortfolioPaid = portfolioTrackApi
                    .getPortfolioTrackList(Collections.singletonList(selectedGood.getGoodsId()));
            addUserIds(selectedPaidUserIds, selectedPortfolioPaid);
        } else if (TYPE_PACKAGE.equalsIgnoreCase(selectedGood.getType())) {
            //using external api, please check
            List<PackageTrackUsersDTO> selectedPackagePaid = packageTrackApi
                    .getPackageTrackList(Collections.singletonList(selectedGood.getGoodsId()));
            addUserIds(selectedPaidUserIds, selectedPackagePaid);
        }

        // 以用户为粒度挑选最新订单信息
        Map<Integer, AggregateCandidate> aggregate = new HashMap<>();
        appendCandidates(aggregate, allPortfolioPaid, selectedGood, selectedPaidUserIds, TYPE_PORTFOLIO);
        appendCandidates(aggregate, allPackagePaid, selectedGood, selectedPaidUserIds, TYPE_PACKAGE);

        List<GiftCandidateVO> result = new ArrayList<>(aggregate.size());
        for (AggregateCandidate candidate : aggregate.values()) {
            result.add(candidate.toVO());
        }
        return result;
    }

    private void appendCandidates(Map<Integer, AggregateCandidate> aggregate,
                                  List<?> trackList,
                                  GoodsBaseVO selectedGood,
                                  Set<Integer> selectedPaidUserIds,
                                  String type) {
        for (Object obj : trackList) {
            if (obj instanceof PortfolioTrackUsersDTO) {
                PortfolioTrackUsersDTO dto = (PortfolioTrackUsersDTO) obj;
                if (TYPE_PORTFOLIO.equalsIgnoreCase(type) && Objects.equals(dto.getPortfolioId(), selectedGood.getGoodsId())
                        && TYPE_PORTFOLIO.equalsIgnoreCase(selectedGood.getType())) {
                    continue;
                }
                addUsersFromList(aggregate, dto.getUserIds(), dto.getName(), selectedPaidUserIds);
            } else if (obj instanceof PackageTrackUsersDTO) {
                PackageTrackUsersDTO dto = (PackageTrackUsersDTO) obj;
                if (TYPE_PACKAGE.equalsIgnoreCase(type) && Objects.equals(dto.getPackageId(), selectedGood.getGoodsId())
                        && TYPE_PACKAGE.equalsIgnoreCase(selectedGood.getType())) {
                    continue;
                }
                addUsersFromList(aggregate, dto.getUserIds(), dto.getName(), selectedPaidUserIds);
            }
        }
    }

    private void addUsersFromList(Map<Integer, AggregateCandidate> aggregate,
                                  List<Integer> userIds,
                                  String productName,
                                  Set<Integer> selectedPaidUserIds) {
        if (userIds == null) {
            return;
        }
        for (Integer uid : userIds) {
            if (uid == null || selectedPaidUserIds.contains(uid)) {
                continue;
            }
            aggregate.putIfAbsent(uid, new AggregateCandidate(uid, productName));
        }
    }

    private void addUserIds(Set<Integer> collector, List<? extends Object> trackList) {
        if (trackList == null) {
            return;
        }
        for (Object obj : trackList) {
            if (obj instanceof PortfolioTrackUsersDTO) {
                List<Integer> ids = ((PortfolioTrackUsersDTO) obj).getUserIds();
                if (ids != null) {
                    collector.addAll(ids);
                }
            } else if (obj instanceof PackageTrackUsersDTO) {
                List<Integer> ids = ((PackageTrackUsersDTO) obj).getUserIds();
                if (ids != null) {
                    collector.addAll(ids);
                }
            }
        }
    }

    private static final class AggregateCandidate {
        private final Integer userId;
        private final String productName;

        private AggregateCandidate(Integer userId,
                                   String productName) {
            this.userId = userId;
            this.productName = productName;
        }

        private GiftCandidateVO toVO() {
            return new GiftCandidateVO(userId, null, null, productName, null, null);
        }
    }
}
