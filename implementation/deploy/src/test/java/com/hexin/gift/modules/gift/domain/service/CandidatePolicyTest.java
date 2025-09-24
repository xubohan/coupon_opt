package com.hexin.gift.modules.gift.domain.service;

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
import com.hexin.gift.modules.gift.domain.service.impl.CandidatePolicyImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidatePolicyTest {

    @Mock
    private PortfolioTrackApi portfolioTrackApi;

    @Mock
    private PackageTrackApi packageTrackApi;

    @Mock
    private PortfolioSpendApi portfolioSpendApi;

    @Mock
    private PackageStatProvider packageStatProvider;

    @InjectMocks
    private CandidatePolicyImpl candidatePolicy;

    private GoodsBaseVO selectedPortfolio;
    private GoodsBaseVO anotherPortfolio;
    private GoodsBaseVO somePackage;

    @BeforeEach
    void setUp() {
        selectedPortfolio = new GoodsBaseVO(1L, "portfolio-1", "PORTFOLIO", 88);
        anotherPortfolio = new GoodsBaseVO(2L, "portfolio-2", "PORTFOLIO", 88);
        somePackage = new GoodsBaseVO(100L, "package-1", "PACKAGE", 88);
    }

    @Test
    void listCandidates_portfolioFlowSuccess() {
        // 测试Portfolio流程的成功场景，验证时间向上取整逻辑
        
        // Mock数据：选中产品有用户3001，候选产品有用户2001
        PortfolioTrackUsersDTO selectedTrack = new PortfolioTrackUsersDTO(1L, "Selected", "detail", 
                Collections.singletonList(3001));
        PortfolioTrackUsersDTO candidateTrack = new PortfolioTrackUsersDTO(2L, "Candidate", "detail", 
                Collections.singletonList(2001));
        
        when(portfolioTrackApi.getPortfolioTrackList(anyList()))
                .thenReturn(Arrays.asList(selectedTrack, candidateTrack));
        
        // Mock订单数据 - 使用2个月零15天的时间间隔，应该向上取整为3个月
        PortfolioSpendDubboDTO spend = new PortfolioSpendDubboDTO(2L, 2001, "Candidate Product", 
                LocalDateTime.of(2024, 1, 1, 10, 0), 
                LocalDateTime.of(2024, 3, 16, 10, 0)); // 2个月15天，应该向上取整为3个月
        
        when(portfolioSpendApi.getSpendByPortIdBuyer(anyList(), anyList()))
                .thenReturn(Collections.singletonList(spend));
        
        // 执行测试
        List<GiftCandidateVO> result = candidatePolicy.listCandidates(selectedPortfolio,
                Arrays.asList(selectedPortfolio, anotherPortfolio));
        
        // 验证结果
        assertEquals(1, result.size());
        GiftCandidateVO candidate = result.get(0);
        assertEquals(Integer.valueOf(2001), candidate.getUserId());
        assertEquals("Candidate Product", candidate.getProductName());
        assertEquals("2024-01-01 10:00:00", candidate.getPurchaseDate());
        assertEquals(Integer.valueOf(3), candidate.getDurationMonths()); // 验证向上取整：2个月15天 → 3个月
    }
    
    @Test
    void listCandidates_packageFlowSuccess() {
        // 测试Package流程的成功场景
        
        GoodsBaseVO selectedPackage = new GoodsBaseVO(100L, "Selected Package", "PACKAGE", 88);
        GoodsBaseVO candidatePackage = new GoodsBaseVO(200L, "Candidate Package", "PACKAGE", 88);
        
        // Mock数据：选中产品有用户5001，候选产品有用户4001  
        // 注意：PackageTrackUsersDTO的packageId是Integer类型，需要转换
        PackageTrackUsersDTO selectedTrack = new PackageTrackUsersDTO(100, "Selected Package", "detail", 
                Collections.singletonList(5001));
        PackageTrackUsersDTO candidateTrack = new PackageTrackUsersDTO(200, "Candidate Package", "detail", 
                Collections.singletonList(4001));
        
        when(packageTrackApi.getPackageTrackList(anyList()))
                .thenReturn(Arrays.asList(selectedTrack, candidateTrack));
        
        // Mock订单数据 - 只为候选用户4001创建订单
        // 注意：使用系统默认时区而非UTC来匹配业务逻辑中的时区转换
        // 使用2个月20天的时间间隔，应该向上取整为3个月
        long startMillis = LocalDateTime.of(2024, 5, 1, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endMillis = LocalDateTime.of(2024, 7, 21, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(); // 2个月20天
        PackageSpendDTO spend = new PackageSpendDTO(200L, 4001, "Candidate Package Product", startMillis, endMillis);
        
        when(packageStatProvider.getSpendByPackageBuyer(anyList(), anyList()))
                .thenReturn(Collections.singletonList(spend));
        
        // 执行测试
        List<GiftCandidateVO> result = candidatePolicy.listCandidates(selectedPackage,
                Arrays.asList(selectedPackage, candidatePackage));
        
        // 验证结果：应该只有1个候选用户（用户4001），用户5001应该被排除
        assertEquals(1, result.size());
        GiftCandidateVO candidate = result.get(0);
        assertEquals(Integer.valueOf(4001), candidate.getUserId());
        assertEquals("Candidate Package Product", candidate.getProductName());
        assertEquals("2024-05-01 10:00:00", candidate.getPurchaseDate());
        assertEquals(Integer.valueOf(3), candidate.getDurationMonths()); // 验证向上取整：2个月20天 → 3个月
    }
    
    @Test
    void testPackageUserFilteringAndDeduplication() {
        // 测试Package类型的用户过滤和去重功能
        System.out.println("========== 开始测试Package用户过滤和去重 ==========");
        
        GoodsBaseVO selectedPackage = new GoodsBaseVO(202L, "Pro套餐", "PACKAGE", 88);
        GoodsBaseVO package404 = new GoodsBaseVO(404L, "财富启航包", "PACKAGE", 88);
        GoodsBaseVO package405 = new GoodsBaseVO(405L, "财富启1航包", "PACKAGE", 88);
        GoodsBaseVO portfolio101 = new GoodsBaseVO(101L, "高端组合", "PORTFOLIO", 88);
        GoodsBaseVO portfolio501 = new GoodsBaseVO(501L, "智慧组合", "PORTFOLIO", 88);
        
        List<GoodsBaseVO> allGoods = Arrays.asList(portfolio101, selectedPackage, package404, package405, portfolio501);
        
        // Mock Package跟踪数据
        // 选中产品(202)的用户: 112, 123, 111, 110, 122
        PackageTrackUsersDTO selectedTrack = new PackageTrackUsersDTO(202, "Pro套餐", "detail", 
                Arrays.asList(112, 123, 111, 110, 122));
        
        // 候选产品(404)的用户: 111, 122, 110, 113, 115, 100, 116
        PackageTrackUsersDTO candidate404Track = new PackageTrackUsersDTO(404, "财富启航包", "detail", 
                Arrays.asList(111, 122, 110, 113, 115, 100, 116));
        
        // 候选产品(405)的用户: 199, 112, 299, 872, 113
        PackageTrackUsersDTO candidate405Track = new PackageTrackUsersDTO(405, "财富启1航包", "detail", 
                Arrays.asList(199, 112, 299, 872, 113));
        
        when(packageTrackApi.getPackageTrackList(anyList()))
                .thenReturn(Arrays.asList(selectedTrack, candidate404Track, candidate405Track));
        
        // Mock Portfolio跟踪数据 - 添加来验证统一处理流程
        PortfolioTrackUsersDTO portfolio101Track = new PortfolioTrackUsersDTO(101L, "高端组合", "detail", 
                Arrays.asList(111, 199, 298, 283, 110)); // 包含与选中产品重复的用户111,110
        PortfolioTrackUsersDTO portfolio501Track = new PortfolioTrackUsersDTO(501L, "智慧组合", "detail", 
                Arrays.asList(211, 212, 213));
        
        when(portfolioTrackApi.getPortfolioTrackList(anyList()))
                .thenReturn(Arrays.asList(portfolio101Track, portfolio501Track));
        
        // Mock订单数据 - 为候选用户创建多笔不同时间的订单，测试最新订单保留逻辑
        long start2024_01 = LocalDateTime.of(2024, 1, 1, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long end2024_04 = LocalDateTime.of(2024, 4, 1, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        long start2024_05 = LocalDateTime.of(2024, 5, 1, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long end2024_08 = LocalDateTime.of(2024, 8, 1, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        long start2024_09 = LocalDateTime.of(2024, 9, 1, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long end2024_12 = LocalDateTime.of(2024, 12, 1, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        System.out.println("\n创建多笔订单数据:");
        
        List<PackageSpendDTO> spends = Arrays.asList(
                // 用户122的多笔订单（应保留最新的2024-09订单）
                new PackageSpendDTO(404L, 122, "财富启航包-旧订单", start2024_01, end2024_04),
                new PackageSpendDTO(404L, 122, "财富启航包-最新订单", start2024_09, end2024_12),
                new PackageSpendDTO(404L, 122, "财富启航包-中间订单", start2024_05, end2024_08),
                
                // 用户113的多笔订单（应保留最新的2024-05订单）
                new PackageSpendDTO(404L, 113, "财富启航包-旧订单", start2024_01, end2024_04),
                new PackageSpendDTO(404L, 113, "财富启航包-最新订单", start2024_05, end2024_08),
                
                // 用户115只有一笔订单
                new PackageSpendDTO(404L, 115, "财富启航包-唯一订单", start2024_05, end2024_08),
                
                // 用户100的多笔订单（应保留最新的2024-09订单）
                new PackageSpendDTO(404L, 100, "财富启航包-旧订单", start2024_01, end2024_04),
                new PackageSpendDTO(404L, 100, "财富启航包-最新订单", start2024_09, end2024_12),
                
                // 用户116只有一笔订单
                new PackageSpendDTO(404L, 116, "财富启航包-唯一订单", start2024_01, end2024_04),
                
                // 用户199的多笔订单（应保留最新的2024-09订单）
                new PackageSpendDTO(405L, 199, "财富启1航包-旧订单", start2024_01, end2024_04),
                new PackageSpendDTO(405L, 199, "财富启1航包-中间订单", start2024_05, end2024_08),
                new PackageSpendDTO(405L, 199, "财富启1航包-最新订单", start2024_09, end2024_12),
                
                // 用户872只有一笔订单
                new PackageSpendDTO(405L, 872, "财富启1航包-唯一订单", start2024_05, end2024_08),
                
                // 用户299的多笔订单（应保留最新的2024-05订单）
                new PackageSpendDTO(405L, 299, "财富启1航包-旧订单", start2024_01, end2024_04),
                new PackageSpendDTO(405L, 299, "财富启1航包-最新订单", start2024_05, end2024_08)
        );
        
        // 打印所有订单信息
        System.out.println("所有订单详情:");
        for (PackageSpendDTO spend : spends) {
            LocalDateTime startTime = Instant.ofEpochMilli(spend.getStartTime())
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime endTime = Instant.ofEpochMilli(spend.getEndTime())
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
            System.out.println(String.format("用户%d: %s, 开始时间: %s, 结束时间: %s", 
                    spend.getBuyerId(), spend.getPackageName(), 
                    startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        
        when(packageStatProvider.getSpendByPackageBuyer(anyList(), anyList()))
                .thenReturn(spends);
        
        // Mock Portfolio订单数据 - 为候选用户创建随机日期的订单
        List<PortfolioSpendDubboDTO> portfolioSpends = Arrays.asList(
                // 用户199的订单（与选中产品无重复，应保留）
                new PortfolioSpendDubboDTO(101L, 199, "高端组合产品", 
                        LocalDateTime.of(2024, 3, 15, 14, 30), 
                        LocalDateTime.of(2024, 7, 15, 14, 30)),
                
                // 用户298的订单（与选中产品无重复，应保留）
                new PortfolioSpendDubboDTO(101L, 298, "高端组合产品", 
                        LocalDateTime.of(2024, 2, 20, 9, 15), 
                        LocalDateTime.of(2024, 6, 20, 9, 15)),
                
                // 用户283的订单（与选中产品无重复，应保留）
                new PortfolioSpendDubboDTO(501L, 283, "智慧组合产品", 
                        LocalDateTime.of(2024, 4, 10, 16, 45), 
                        LocalDateTime.of(2024, 8, 10, 16, 45)),
                
                // 用户211的订单
                new PortfolioSpendDubboDTO(501L, 211, "智慧组合产品", 
                        LocalDateTime.of(2024, 1, 25, 11, 20), 
                        LocalDateTime.of(2024, 5, 25, 11, 20)),
                
                // 用户212的订单
                new PortfolioSpendDubboDTO(501L, 212, "智慧组合产品", 
                        LocalDateTime.of(2024, 6, 8, 13, 10), 
                        LocalDateTime.of(2024, 10, 8, 13, 10)),
                
                // 用户213的订单
                new PortfolioSpendDubboDTO(501L, 213, "智慧组合产品", 
                        LocalDateTime.of(2024, 7, 3, 8, 50), 
                        LocalDateTime.of(2024, 11, 3, 8, 50))
        );
        
        when(portfolioSpendApi.getSpendByPortIdBuyer(anyList(), anyList()))
                .thenReturn(portfolioSpends);
        
        // 执行测试
        System.out.println("选中产品: " + selectedPackage.getProductName() + " (ID: " + selectedPackage.getProductId() + ")");
        System.out.println("所有产品: Portfolio[101,501] + Package[202,404,405]");
        System.out.println("选中产品用户: [112, 123, 111, 110, 122]");
        System.out.println("候选Package404用户: [111, 122, 110, 113, 115, 100, 116] - 排除[111,122,110]");
        System.out.println("候选Package405用户: [199, 112, 299, 872, 113] - 排除[112]");
        System.out.println("候选Portfolio101用户: [111, 199, 298, 283, 110] - 排除[111,110]");
        System.out.println("候选Portfolio501用户: [211, 212, 213] - 无排除");
        System.out.println("\n开始统一处理流程(混合Portfolio+Package)过滤和合并最新订单...");
        
        List<GiftCandidateVO> result = candidatePolicy.listCandidates(selectedPackage, allGoods);
        
        // 打印最终保留的订单信息
        System.out.println("\n最终保留的订单信息（每个用户只保留最新一笔）:");
        for (GiftCandidateVO candidate : result) {
            System.out.println(String.format("用户ID: %d, 产品: %s, 购买日期: %s, 持续月数: %d", 
                    candidate.getUserId(), candidate.getProductName(), 
                    candidate.getPurchaseDate(), candidate.getDurationMonths()));
        }
        
        // 验证过滤逻辑
        System.out.println("\n验证最新订单保留逻辑:");
        System.out.println("预期排除的用户(选中产品用户): [112, 123, 111, 110, 122]");
        System.out.println("预期保留的用户: [113, 115, 100, 116, 199, 299, 872] (不包含122，因为122在选中产品用户列表中)");
        System.out.println("最新订单选择规则: 每个用户只保留开始时间最晚的订单");
        // 验证统一处理流程逻辑
        System.out.println("\n验证统一处理流程逻辑:");
        System.out.println("预期排除的用户(选中产品用户): [112, 123, 111, 110, 122]");
        System.out.println("预期保留的用户: Package[113,115,100,116,199,299,872] + Portfolio[298,283,211,212,213]");
        System.out.println("最新订单选择规则: 每个用户只保留开始时间最晚的订单");
        System.out.println("注意: 用户111,110被Portfolio排除，用户112,122被Package排除，但用户199在两个类型中都存在");
        
        // 验证结果
        assertEquals(12, result.size(), "应该有12个候选用户(Package7个+Portfolio5个)");
        
        // 验证用户ID去重和过滤正确性
        Set<Integer> resultUserIds = result.stream()
                .map(GiftCandidateVO::getUserId)
                .collect(java.util.stream.Collectors.toSet());
        
        // Package用户: 113,115,100,116,199,299,872 + Portfolio用户: 298,283,211,212,213
        Set<Integer> expectedUsers = new HashSet<>(Arrays.asList(113, 115, 100, 116, 199, 299, 872, 298, 283, 211, 212, 213));
        assertEquals(expectedUsers, resultUserIds, "应该包含来自Package和Portfolio的所有候选用户");
        
        System.out.println("\n测试通过！统一处理流程正常工作，支持混合Portfolio+Package产品类型。");
        System.out.println("========== Package+Portfolio统一处理测试结束 ==========");
    }
    
    @Test
    void testUnifiedProcessingFlow() {
        // 测试统一处理流程：混合Portfolio和Package产品的候选用户筛选
        System.out.println("========== 开始测试统一处理流程 ==========");
        
        // 选中产品是Portfolio类型
        GoodsBaseVO selectedPortfolio = new GoodsBaseVO(101L, "高端组合", "PORTFOLIO", 88);
        
        // 混合产品列表：包含Portfolio和Package两种类型
        GoodsBaseVO portfolio201 = new GoodsBaseVO(201L, "财富组合", "PORTFOLIO", 88);
        GoodsBaseVO package301 = new GoodsBaseVO(301L, "智能套餐", "PACKAGE", 88);
        GoodsBaseVO package401 = new GoodsBaseVO(401L, "高级套餐", "PACKAGE", 88);
        
        List<GoodsBaseVO> allGoods = Arrays.asList(selectedPortfolio, portfolio201, package301, package401);
        
        // Mock Portfolio跟踪数据
        PortfolioTrackUsersDTO selectedPortfolioTrack = new PortfolioTrackUsersDTO(101L, "高端组合", "detail", 
                Arrays.asList(1001, 1002, 1003));
        PortfolioTrackUsersDTO candidatePortfolioTrack = new PortfolioTrackUsersDTO(201L, "财富组合", "detail", 
                Arrays.asList(2001, 2002, 1002)); // 1002与选中产品重复，应被排除
        
        when(portfolioTrackApi.getPortfolioTrackList(anyList()))
                .thenReturn(Arrays.asList(selectedPortfolioTrack, candidatePortfolioTrack));
        
        // Mock Package跟踪数据
        PackageTrackUsersDTO candidatePackage301Track = new PackageTrackUsersDTO(301, "智能套餐", "detail", 
                Arrays.asList(3001, 3002, 1001)); // 1001与选中产品重复，应被排除
        PackageTrackUsersDTO candidatePackage401Track = new PackageTrackUsersDTO(401, "高级套餐", "detail", 
                Arrays.asList(4001, 4002));
        
        when(packageTrackApi.getPackageTrackList(anyList()))
                .thenReturn(Arrays.asList(candidatePackage301Track, candidatePackage401Track));
        
        // Mock订单数据
        long startMillis = LocalDateTime.of(2024, 6, 1, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endMillis = LocalDateTime.of(2024, 9, 1, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        // Portfolio订单
        List<PortfolioSpendDubboDTO> portfolioSpends = Arrays.asList(
                new PortfolioSpendDubboDTO(201L, 2001, "财富组合产品", 
                        LocalDateTime.of(2024, 6, 1, 10, 0), 
                        LocalDateTime.of(2024, 9, 1, 10, 0)),
                new PortfolioSpendDubboDTO(201L, 2002, "财富组合产品", 
                        LocalDateTime.of(2024, 6, 1, 10, 0), 
                        LocalDateTime.of(2024, 9, 1, 10, 0))
        );
        
        // Package订单
        List<PackageSpendDTO> packageSpends = Arrays.asList(
                new PackageSpendDTO(301L, 3001, "智能套餐产品", startMillis, endMillis),
                new PackageSpendDTO(301L, 3002, "智能套餐产品", startMillis, endMillis),
                new PackageSpendDTO(401L, 4001, "高级套餐产品", startMillis, endMillis),
                new PackageSpendDTO(401L, 4002, "高级套餐产品", startMillis, endMillis)
        );
        
        when(portfolioSpendApi.getSpendByPortIdBuyer(anyList(), anyList()))
                .thenReturn(portfolioSpends);
        when(packageStatProvider.getSpendByPackageBuyer(anyList(), anyList()))
                .thenReturn(packageSpends);
        
        // 执行测试
        System.out.println("选中产品: " + selectedPortfolio.getProductName() + " (类型: " + selectedPortfolio.getProductType() + ")");
        System.out.println("所有产品: Portfolio[101,201] + Package[301,401]");
        System.out.println("选中产品用户: [1001, 1002, 1003]");
        System.out.println("候选Portfolio201用户: [2001, 2002, 1002] - 排除1002");
        System.out.println("候选Package301用户: [3001, 3002, 1001] - 排除1001");
        System.out.println("候选Package401用户: [4001, 4002]");
        System.out.println("\n开始统一处理...");
        
        List<GiftCandidateVO> result = candidatePolicy.listCandidates(selectedPortfolio, allGoods);
        
        // 打印结果
        System.out.println("\n最终候选用户结果:");
        for (GiftCandidateVO candidate : result) {
            System.out.println(String.format("用户ID: %d, 产品: %s, 购买日期: %s, 持续月数: %d", 
                    candidate.getUserId(), candidate.getProductName(), 
                    candidate.getPurchaseDate(), candidate.getDurationMonths()));
        }
        
        // 验证结果
        System.out.println("\n验证统一处理逻辑:");
        System.out.println("预期保留的用户: [2001, 2002, 3001, 3002, 4001, 4002] (6个用户)");
        System.out.println("预期排除的用户: [1001, 1002, 1003] (选中产品用户)");
        
        assertEquals(6, result.size(), "应该有6个候选用户");
        
        Set<Integer> resultUserIds = result.stream()
                .map(GiftCandidateVO::getUserId)
                .collect(java.util.stream.Collectors.toSet());
        
        Set<Integer> expectedUsers = new HashSet<>(Arrays.asList(2001, 2002, 3001, 3002, 4001, 4002));
        assertEquals(expectedUsers, resultUserIds, "应该包含来自Portfolio和Package的所有候选用户");
        
        System.out.println("\n测试通过！统一处理流程工作正常。");
        System.out.println("========== 统一处理流程测试结束 ==========");
    }
}
