package com.hexin.gift.modules.gift.domain.service.impl;

import com.hexin.gift.common.external.rpc.PackageStatProvider;
import com.hexin.gift.common.external.rpc.PackageTrackApi;
import com.hexin.gift.common.external.rpc.PortfolioSpendApi;
import com.hexin.gift.common.external.rpc.PortfolioTrackApi;
import com.hexin.gift.common.external.rpc.dto.PackageSpendDTO;
import com.hexin.gift.common.external.rpc.dto.PackageTrackUsersDTO;
import com.hexin.gift.common.external.rpc.dto.PortfolioSpendDubboDTO;
import com.hexin.gift.common.external.rpc.dto.PortfolioTrackUsersDTO;
import com.hexin.gift.interfaces.rest.vo.GiftCandidateVO;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;
import com.hexin.gift.modules.gift.domain.service.CandidatePolicy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
    private static final DateTimeFormatter PURCHASE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PortfolioTrackApi portfolioTrackApi;
    private final PackageTrackApi packageTrackApi;
    private final PortfolioSpendApi portfolioSpendApi;
    private final PackageStatProvider packageStatProvider;

    public CandidatePolicyImpl(PortfolioTrackApi portfolioTrackApi,
                               PackageTrackApi packageTrackApi,
                               PortfolioSpendApi portfolioSpendApi,
                               PackageStatProvider packageStatProvider) {
        this.portfolioTrackApi = portfolioTrackApi;
        this.packageTrackApi = packageTrackApi;
        this.portfolioSpendApi = portfolioSpendApi;
        this.packageStatProvider = packageStatProvider;
    }

    @Override
    public List<GiftCandidateVO> listCandidates(GoodsBaseVO selectedGood, List<GoodsBaseVO> allGoods) {
        if (selectedGood == null || allGoods == null || allGoods.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> allPortfolioIds = new HashSet<>();
        Set<Long> allPackageIds = new HashSet<>();
        for (GoodsBaseVO good : allGoods) {
            if (good == null || good.getProductId() == null) {
                continue;
            }
            if (TYPE_PORTFOLIO.equalsIgnoreCase(good.getProductType())) {
                allPortfolioIds.add(good.getProductId());
            } else if (TYPE_PACKAGE.equalsIgnoreCase(good.getProductType())) {
                allPackageIds.add(good.getProductId());
            }
        }

        List<PortfolioTrackUsersDTO> allPortfolioPaid = Collections.emptyList();
        if (!allPortfolioIds.isEmpty()) {
            List<PortfolioTrackUsersDTO> queried = portfolioTrackApi.getPortfolioTrackList(new ArrayList<>(allPortfolioIds));
            allPortfolioPaid = queried != null ? queried : Collections.emptyList();
        }
        List<PackageTrackUsersDTO> allPackagePaid = Collections.emptyList();
        if (!allPackageIds.isEmpty()) {
            List<PackageTrackUsersDTO> queried = packageTrackApi.getPackageTrackList(new ArrayList<>(allPackageIds));
            allPackagePaid = queried != null ? queried : Collections.emptyList();
        }

        Set<Integer> selectedPaidUserIds = new HashSet<>();
        if (TYPE_PORTFOLIO.equalsIgnoreCase(selectedGood.getProductType())) {
            List<PortfolioTrackUsersDTO> selectedPortfolioPaid = portfolioTrackApi
                    .getPortfolioTrackList(Collections.singletonList(selectedGood.getProductId()));
            addUserIds(selectedPaidUserIds, selectedPortfolioPaid);
        } else if (TYPE_PACKAGE.equalsIgnoreCase(selectedGood.getProductType())) {
            List<PackageTrackUsersDTO> selectedPackagePaid = packageTrackApi
                    .getPackageTrackList(Collections.singletonList(selectedGood.getProductId()));
            addUserIds(selectedPaidUserIds, selectedPackagePaid);
        }

        Map<Integer, AggregateCandidate> aggregate = new HashMap<>();
        appendCandidates(aggregate, allPortfolioPaid, selectedGood, selectedPaidUserIds, TYPE_PORTFOLIO);
        appendCandidates(aggregate, allPackagePaid, selectedGood, selectedPaidUserIds, TYPE_PACKAGE);

        if (!aggregate.isEmpty()) {
            List<Integer> candidateIds = new ArrayList<>(aggregate.keySet());
            if (!allPortfolioIds.isEmpty()) {
                List<PortfolioSpendDubboDTO> spends = portfolioSpendApi.getSpendByPortIdBuyer(new ArrayList<>(allPortfolioIds), candidateIds);
                mergePortfolioSpend(aggregate, spends);
            }
            if (!allPackageIds.isEmpty()) {
                List<PackageSpendDTO> spends = packageStatProvider.getSpendByPackageBuyer(new ArrayList<>(allPackageIds), candidateIds);
                mergePackageSpend(aggregate, spends);
            }
        }

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
                if (TYPE_PORTFOLIO.equalsIgnoreCase(type) && Objects.equals(dto.getPortfolioId(), selectedGood.getProductId())
                        && TYPE_PORTFOLIO.equalsIgnoreCase(selectedGood.getProductType())) {
                    continue;
                }
                addUsersFromList(aggregate, dto.getUserIds(), dto.getName(), selectedPaidUserIds);
            } else if (obj instanceof PackageTrackUsersDTO) {
                PackageTrackUsersDTO dto = (PackageTrackUsersDTO) obj;
                if (TYPE_PACKAGE.equalsIgnoreCase(type) && Objects.equals(dto.getPackageId(), selectedGood.getProductId())
                        && TYPE_PACKAGE.equalsIgnoreCase(selectedGood.getProductType())) {
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

    private void mergePortfolioSpend(Map<Integer, AggregateCandidate> aggregate,
                                     List<PortfolioSpendDubboDTO> spends) {
        if (spends == null) {
            return;
        }
        Map<Integer, PortfolioSpendDubboDTO> latest = new HashMap<>();
        for (PortfolioSpendDubboDTO dto : spends) {
            if (dto == null || dto.getBuyerId() == null || dto.getStartAt() == null) {
                continue;
            }
            if (!aggregate.containsKey(dto.getBuyerId())) {
                continue;
            }
            PortfolioSpendDubboDTO existing = latest.get(dto.getBuyerId());
            if (existing == null || dto.getStartAt().isAfter(existing.getStartAt())) {
                latest.put(dto.getBuyerId(), dto);
            }
        }
        for (Map.Entry<Integer, PortfolioSpendDubboDTO> entry : latest.entrySet()) {
            AggregateCandidate candidate = aggregate.get(entry.getKey());
            if (candidate == null) {
                continue;
            }
            PortfolioSpendDubboDTO dto = entry.getValue();
            LocalDateTime startAt = dto.getStartAt();
            LocalDateTime endAt = dto.getEndAt();
            String purchaseDate = startAt != null ? startAt.format(PURCHASE_FORMATTER) : null;
            Integer durationMonths = calculateMonths(startAt, endAt);
            candidate.updateSpendInfo(dto.getProductName(), purchaseDate, durationMonths);
        }
    }

    private void mergePackageSpend(Map<Integer, AggregateCandidate> aggregate,
                                   List<PackageSpendDTO> spends) {
        if (spends == null) {
            return;
        }
        Map<Integer, PackageSpendDTO> latest = new HashMap<>();
        for (PackageSpendDTO dto : spends) {
            if (dto == null || dto.getBuyerId() == null || dto.getStartTime() == null) {
                continue;
            }
            if (!aggregate.containsKey(dto.getBuyerId())) {
                continue;
            }
            PackageSpendDTO existing = latest.get(dto.getBuyerId());
            if (existing == null || isAfter(dto.getStartTime(), existing.getStartTime())) {
                latest.put(dto.getBuyerId(), dto);
            }
        }
        for (Map.Entry<Integer, PackageSpendDTO> entry : latest.entrySet()) {
            AggregateCandidate candidate = aggregate.get(entry.getKey());
            if (candidate == null) {
                continue;
            }
            PackageSpendDTO dto = entry.getValue();
            LocalDateTime startAt = toLocalDateTime(dto.getStartTime());
            LocalDateTime endAt = toLocalDateTime(dto.getEndTime());
            String purchaseDate = startAt != null ? startAt.format(PURCHASE_FORMATTER) : null;
            Integer durationMonths = calculateMonths(startAt, endAt);
            candidate.updateSpendInfo(dto.getPackageName(), purchaseDate, durationMonths);
        }
    }

    private boolean isAfter(Long startTime, Long otherStart) {
        LocalDateTime start = toLocalDateTime(startTime);
        LocalDateTime other = toLocalDateTime(otherStart);
        if (start == null) {
            return false;
        }
        if (other == null) {
            return true;
        }
        return start.isAfter(other);
    }

    private LocalDateTime toLocalDateTime(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        return Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private Integer calculateMonths(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return null;
        }
        long months = ChronoUnit.MONTHS.between(start, end);
        if (months < 0) {
            months = 0;
        }
        return (int) months;
    }

    private static final class AggregateCandidate {
        private final Integer userId;
        private String productName;
        private String purchaseDate;
        private Integer durationMonths;

        private AggregateCandidate(Integer userId,
                                   String productName) {
            this.userId = userId;
            this.productName = productName;
        }

        private void updateSpendInfo(String productName, String purchaseDate, Integer durationMonths) {
            if (productName != null) {
                this.productName = productName;
            }
            this.purchaseDate = purchaseDate;
            this.durationMonths = durationMonths;
        }

        private GiftCandidateVO toVO() {
            return new GiftCandidateVO(userId, null, null, productName, purchaseDate, durationMonths);
        }
    }
}
