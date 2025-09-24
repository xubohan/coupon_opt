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
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 赠品候选用户策略实现类
 * <p>
 * 根据选中商品类型（组合/套餐），查询其他同类型商品的付费用户作为赠品候选用户。
 * 排除已经是选中商品付费用户的用户，并获取每个候选用户的最新订单信息。
 * 
 * @author xubohan@myhexin.com
 * @date 2025-09-24 22:30:00
 */
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

    /**
     * 根据选中商品和所有商品列表，获取可赠送的候选用户列表
     * 重新设计的流程：
     * 1. 严格区分portfolio和package两条路线
     * 2. 获取跟踪用户信息 → 提取排除用户 → 筛选候选用户 → 查询订单 → 构建结果
     * 
     * @param selectedGood 选中的商品
     * @param allGoods 所有商品列表
     * @return 候选用户列表，包含用户ID、产品名称、购买日期、持续月数等信息
     */
    @Override
    public List<GiftCandidateVO> listCandidates(GoodsBaseVO selectedGood, List<GoodsBaseVO> allGoods) {
        if (selectedGood == null || allGoods == null || allGoods.isEmpty()) {
            return Collections.emptyList();
        }

        // 判断当前选中的产品类型
        boolean portfolioSelected = TYPE_PORTFOLIO.equalsIgnoreCase(selectedGood.getProductType());
        boolean packageSelected = TYPE_PACKAGE.equalsIgnoreCase(selectedGood.getProductType());
        
        // 如果产品类型不是PORTFOLIO也不是PACKAGE，直接返回空
        if (!portfolioSelected && !packageSelected) {
            return Collections.emptyList();
        }
        
        // 严格按类型分流处理
        if (portfolioSelected) {
            return processPortfolioFlow(selectedGood, allGoods);
        } else {
            return processPackageFlow(selectedGood, allGoods);
        }
    }
    
    /**
     * 处理PORTFOLIO类型的完整流程
     * 步骤1: 获取所有PORTFOLIO产品的跟踪用户信息
     * 步骤2: 提取选中产品的用户ID作为排除列表
     * 步骤3: 从其他产品用户中排除选中产品用户，得到候选用户
     * 步骤4: 查询候选用户的订单信息
     * 步骤5: 构建最终结果
     */
    private List<GiftCandidateVO> processPortfolioFlow(GoodsBaseVO selectedGood, List<GoodsBaseVO> allGoods) {
        // 步骤1: 获取所有PORTFOLIO类型的产品ID
        Set<Long> portfolioIds = extractPortfolioIds(allGoods);
        if (portfolioIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 步骤2: 获取所有PORTFOLIO产品的跟踪用户信息
        List<PortfolioTrackUsersDTO> allTrackUsers = safePortfolioList(
            portfolioTrackApi.getPortfolioTrackList(new ArrayList<>(portfolioIds))
        );
        
        // 步骤3: 提取选中产品的用户ID作为排除列表
        Set<Integer> excludeUsers = extractSelectedPortfolioUsers(allTrackUsers, selectedGood.getProductId());
        
        // 步骤4: 构建候选用户映射（排除选中产品的用户）
        Map<Integer, UserSpendInfo> candidateMap = buildPortfolioCandidates(allTrackUsers, selectedGood.getProductId(), excludeUsers);
        
        if (candidateMap.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 步骤5: 查询候选用户的订单信息
        List<Integer> candidateIds = new ArrayList<>(candidateMap.keySet());
        List<PortfolioSpendDubboDTO> spends = portfolioSpendApi.getSpendByPortIdBuyer(new ArrayList<>(portfolioIds), candidateIds);
        mergePortfolioSpendInfo(candidateMap, spends);
        
        // 步骤6: 构建最终结果
        return buildFinalResult(candidateMap);
    }
    
    /**
     * 提取所有PORTFOLIO类型的产品ID
     */
    private Set<Long> extractPortfolioIds(List<GoodsBaseVO> allGoods) {
        Set<Long> portfolioIds = new HashSet<>();
        for (GoodsBaseVO good : allGoods) {
            if (good != null && good.getProductId() != null && 
                TYPE_PORTFOLIO.equalsIgnoreCase(good.getProductType())) {
                portfolioIds.add(good.getProductId());
            }
        }
        return portfolioIds;
    }
    
    /**
     * 提取所有PACKAGE类型的产品ID
     */
    private Set<Long> extractPackageIds(List<GoodsBaseVO> allGoods) {
        Set<Long> packageIds = new HashSet<>();
        for (GoodsBaseVO good : allGoods) {
            if (good != null && good.getProductId() != null && 
                TYPE_PACKAGE.equalsIgnoreCase(good.getProductType())) {
                packageIds.add(good.getProductId());
            }
        }
        return packageIds;
    }
    
    /**
     * 从组合跟踪列表中提取选中产品的用户ID
     */
    private Set<Integer> extractSelectedPortfolioUsers(List<PortfolioTrackUsersDTO> trackList, Long selectedProductId) {
        Set<Integer> selectedUsers = new HashSet<>();
        for (PortfolioTrackUsersDTO dto : trackList) {
            if (dto != null && dto.getPortfolioId() != null && 
                Objects.equals(dto.getPortfolioId(), selectedProductId) && 
                dto.getUserIds() != null) {
                selectedUsers.addAll(dto.getUserIds());
            }
        }
        return selectedUsers;
    }
    
    /**
     * 从套餐跟踪列表中提取选中产品的用户ID
     */
    private Set<Integer> extractSelectedPackageUsers(List<PackageTrackUsersDTO> trackList, Long selectedProductId) {
        Set<Integer> selectedUsers = new HashSet<>();
        for (PackageTrackUsersDTO dto : trackList) {
            if (dto != null && dto.getPackageId() != null && 
                selectedProductId != null && 
                dto.getPackageId().equals(selectedProductId.intValue()) && 
                dto.getUserIds() != null) {
                selectedUsers.addAll(dto.getUserIds());
            }
        }
        return selectedUsers;
    }
    
    /**
     * 构建组合类型的候选用户映射
     */
    private Map<Integer, UserSpendInfo> buildPortfolioCandidates(List<PortfolioTrackUsersDTO> trackList, 
                                                                 Long selectedProductId, 
                                                                 Set<Integer> excludeUsers) {
        Map<Integer, UserSpendInfo> candidateMap = new HashMap<>();
        for (PortfolioTrackUsersDTO dto : trackList) {
            if (dto == null || dto.getPortfolioId() == null || 
                Objects.equals(dto.getPortfolioId(), selectedProductId) || 
                dto.getUserIds() == null) {
                continue;
            }
            
            for (Integer userId : dto.getUserIds()) {
                if (userId != null && !excludeUsers.contains(userId)) {
                    candidateMap.putIfAbsent(userId, new UserSpendInfo(userId, dto.getName()));
                }
            }
        }
        return candidateMap;
    }
    
    /**
     * 构建套餐类型的候选用户映射
     */
    private Map<Integer, UserSpendInfo> buildPackageCandidates(List<PackageTrackUsersDTO> trackList, 
                                                               Long selectedProductId, 
                                                               Set<Integer> excludeUsers) {
        Map<Integer, UserSpendInfo> candidateMap = new HashMap<>();
        for (PackageTrackUsersDTO dto : trackList) {
            if (dto == null || dto.getPackageId() == null || 
                (selectedProductId != null && dto.getPackageId().equals(selectedProductId.intValue())) || 
                dto.getUserIds() == null) {
                continue;
            }
            
            for (Integer userId : dto.getUserIds()) {
                if (userId != null && !excludeUsers.contains(userId)) {
                    candidateMap.putIfAbsent(userId, new UserSpendInfo(userId, dto.getName()));
                }
            }
        }
        return candidateMap;
    }
    
    /**
     * 处理PACKAGE类型的完整流程
     * 步骤1: 获取所有PACKAGE产品的跟踪用户信息
     * 步骤2: 提取选中产品的用户ID作为排除列表
     * 步骤3: 从其他产品用户中排除选中产品用户，得到候选用户
     * 步骤4: 查询候选用户的订单信息
     * 步骤5: 构建最终结果
     */
    private List<GiftCandidateVO> processPackageFlow(GoodsBaseVO selectedGood, List<GoodsBaseVO> allGoods) {
        // 步骤1: 获取所有PACKAGE类型的产品ID
        Set<Long> packageIds = extractPackageIds(allGoods);
        if (packageIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 步骤2: 获取所有PACKAGE产品的跟踪用户信息
        List<PackageTrackUsersDTO> allTrackUsers = safePackageList(
            packageTrackApi.getPackageTrackList(new ArrayList<>(packageIds))
        );
        
        // 步骤3: 提取选中产品的用户ID作为排除列表
        Set<Integer> excludeUsers = extractSelectedPackageUsers(allTrackUsers, selectedGood.getProductId());
        
        // 步骤4: 构建候选用户映射（排除选中产品的用户）
        Map<Integer, UserSpendInfo> candidateMap = buildPackageCandidates(allTrackUsers, selectedGood.getProductId(), excludeUsers);
        
        if (candidateMap.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 步骤5: 查询候选用户的订单信息
        List<Integer> candidateIds = new ArrayList<>(candidateMap.keySet());
        List<PackageSpendDTO> spends = packageStatProvider.getSpendByPackageBuyer(new ArrayList<>(packageIds), candidateIds);
        mergePackageSpendInfo(candidateMap, spends);
        
        // 步骤6: 构建最终结果
        return buildFinalResult(candidateMap);
    }
    


    /**
     * 将毫秒时间戳转换为LocalDateTime
     * 
     * @param epochMillis 毫秒时间戳
     * @return 转换后的LocalDateTime，如果输入为null则返回null
     */
    private LocalDateTime toLocalDateTime(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        return Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * 合并组合订单信息到候选用户映射中
     */
    private void mergePortfolioSpendInfo(Map<Integer, UserSpendInfo> candidateMap, 
                                         List<PortfolioSpendDubboDTO> spends) {
        if (spends == null || spends.isEmpty()) {
            return;
        }
        
        // 按用户分组，每个用户只保留最新的订单
        Map<Integer, PortfolioSpendDubboDTO> latestByUser = new HashMap<>();
        for (PortfolioSpendDubboDTO dto : spends) {
            if (dto == null || dto.getBuyerId() == null || !candidateMap.containsKey(dto.getBuyerId())) {
                continue;
            }
            
            PortfolioSpendDubboDTO existing = latestByUser.get(dto.getBuyerId());
            if (existing == null) {
                latestByUser.put(dto.getBuyerId(), dto);
            } else {
                LocalDateTime currentStart = dto.getStartAt();
                LocalDateTime existingStart = existing.getStartAt();
                if (currentStart != null && (existingStart == null || currentStart.isAfter(existingStart))) {
                    latestByUser.put(dto.getBuyerId(), dto);
                }
            }
        }
        
        // 更新候选用户的订单信息
        for (Map.Entry<Integer, PortfolioSpendDubboDTO> entry : latestByUser.entrySet()) {
            UserSpendInfo userInfo = candidateMap.get(entry.getKey());
            if (userInfo != null) {
                PortfolioSpendDubboDTO dto = entry.getValue();
                userInfo.updateSpendInfo(dto.getPortfolioId(), dto.getProductName(), 
                                       dto.getStartAt(), dto.getEndAt());
            }
        }
    }
    
    /**
     * 合并套餐订单信息到候选用户映射中
     */
    private void mergePackageSpendInfo(Map<Integer, UserSpendInfo> candidateMap, 
                                       List<PackageSpendDTO> spends) {
        if (spends == null || spends.isEmpty()) {
            return;
        }
        
        // 按用户分组，每个用户只保留最新的订单
        Map<Integer, PackageSpendDTO> latestByUser = new HashMap<>();
        for (PackageSpendDTO dto : spends) {
            if (dto == null || dto.getBuyerId() == null || !candidateMap.containsKey(dto.getBuyerId())) {
                continue;
            }
            
            PackageSpendDTO existing = latestByUser.get(dto.getBuyerId());
            if (existing == null) {
                latestByUser.put(dto.getBuyerId(), dto);
            } else {
                Long currentStart = dto.getStartTime();
                Long existingStart = existing.getStartTime();
                if (currentStart != null && (existingStart == null || currentStart > existingStart)) {
                    latestByUser.put(dto.getBuyerId(), dto);
                }
            }
        }
        
        // 更新候选用户的订单信息
        for (Map.Entry<Integer, PackageSpendDTO> entry : latestByUser.entrySet()) {
            UserSpendInfo userInfo = candidateMap.get(entry.getKey());
            if (userInfo != null) {
                PackageSpendDTO dto = entry.getValue();
                LocalDateTime startTime = toLocalDateTime(dto.getStartTime());
                LocalDateTime endTime = toLocalDateTime(dto.getEndTime());
                userInfo.updateSpendInfo(dto.getPackageId(), dto.getPackageName(), startTime, endTime);
            }
        }
    }
    
    /**
     * 构建最终结果
     */
    private List<GiftCandidateVO> buildFinalResult(Map<Integer, UserSpendInfo> candidateMap) {
        List<GiftCandidateVO> result = new ArrayList<>(candidateMap.size());
        for (UserSpendInfo userInfo : candidateMap.values()) {
            result.add(userInfo.toVO());
        }
        return result;
    }

    /**
     * 安全获取组合跟踪用户列表，避免null值
     * 
     * @param list 原始列表
     * @return 非null的列表
     */
    private List<PortfolioTrackUsersDTO> safePortfolioList(List<PortfolioTrackUsersDTO> list) {
        return list != null ? list : Collections.emptyList();
    }

    /**
     * 安全获取套餐跟踪用户列表，避免null值
     * 
     * @param list 原始列表
     * @return 非null的列表
     */
    private List<PackageTrackUsersDTO> safePackageList(List<PackageTrackUsersDTO> list) {
        return list != null ? list : Collections.emptyList();
    }

    /**
     * 用户订单信息的内部数据结构
     * 用于存储用户的基本信息和最新订单信息
     */
    private static final class UserSpendInfo {
        private final Integer userId;
        private Long productId;
        private String productName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        
        private UserSpendInfo(Integer userId, String productName) {
            this.userId = userId;
            this.productName = productName;
        }
        
        /**
         * 更新订单信息
         */
        private void updateSpendInfo(Long productId, String productName, LocalDateTime startTime, LocalDateTime endTime) {
            this.productId = productId;
            if (productName != null) {
                this.productName = productName;
            }
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        /**
         * 转换为前端VO对象
         */
        private GiftCandidateVO toVO() {
            String purchaseDate = startTime != null ? startTime.format(PURCHASE_FORMATTER) : null;
            Integer durationMonths = calculateMonths(startTime, endTime);
            return new GiftCandidateVO(userId, null, null, productName, purchaseDate, durationMonths);
        }
        
        /**
         * 计算两个时间之间的月份差，向上取整
         * 确保即使不足一个月也算作一个月
         */
        private static Integer calculateMonths(LocalDateTime start, LocalDateTime end) {
            if (start == null || end == null) {
                return 0;
            }
            try {
                // 使用Period来计算精确的年月日差值
                Period period = Period.between(start.toLocalDate(), end.toLocalDate());
                int months = period.getYears() * 12 + period.getMonths();
                
                // 如果有天数差异，则向上取整（增加一个月）
                if (period.getDays() > 0 || 
                    (period.getDays() == 0 && start.toLocalTime().isBefore(end.toLocalTime()))) {
                    months++;
                }
                
                return Math.max(0, months);
            } catch (Exception e) {
                return 0;
            }
        }
    }
}
