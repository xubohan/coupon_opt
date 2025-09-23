package com.hexin.gift.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hexin.gift.app.manager.ProductGiftManager;
import com.hexin.gift.interfaces.rest.converter.GiftControllerConverter;
import com.hexin.gift.interfaces.rest.query.CheckEligibilityQuery;
import com.hexin.gift.interfaces.rest.query.GrantBatchQuery;
import com.hexin.gift.interfaces.rest.query.ListCandidatesQuery;
import com.hexin.gift.interfaces.rest.query.ListGoodsQuery;
import com.hexin.gift.interfaces.rest.vo.GiftCandidateVO;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GiftController.class)
@Import(GiftControllerConverter.class)
class GiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductGiftManager productGiftManager;

    @Test
    void listGoods_shouldReturnGoodsList() throws Exception {
        List<GoodsBaseVO> goods = Collections.singletonList(new GoodsBaseVO(1L, "portfolio", "PORTFOLIO", 88));
        ListGoodsQuery expectedQuery = new ListGoodsQuery(101);
        when(productGiftManager.listGoods(any(ListGoodsQuery.class))).thenReturn(goods);

        mockMvc.perform(get("/api/gifts/goods").param("advisorId", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].goodsId").value(1L))
                .andExpect(jsonPath("$[0].type").value("PORTFOLIO"));

        ArgumentCaptor<ListGoodsQuery> queryCaptor = ArgumentCaptor.forClass(ListGoodsQuery.class);
        verify(productGiftManager).listGoods(queryCaptor.capture());
        assertEquals(expectedQuery.getAdvisorId(), queryCaptor.getValue().getAdvisorId());
    }

    @Test
    void listCandidates_shouldReturnCandidateList() throws Exception {
        GoodsBaseVO selected = new GoodsBaseVO(1L, "portfolio", "PORTFOLIO", 88);
        ListCandidatesQuery query = new ListCandidatesQuery(selected, Collections.singletonList(selected));
        GiftCandidateVO candidate = new GiftCandidateVO(9001, "nick", null, "product", "2024-01-01", 6);
        when(productGiftManager.listCandidates(any(GoodsBaseVO.class), any())).thenReturn(Collections.singletonList(candidate));

        mockMvc.perform(post("/api/gifts/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(9001));

        ArgumentCaptor<GoodsBaseVO> selectedCaptor = ArgumentCaptor.forClass(GoodsBaseVO.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<GoodsBaseVO>> goodsListCaptor = ArgumentCaptor.forClass(List.class);
        verify(productGiftManager).listCandidates(selectedCaptor.capture(), goodsListCaptor.capture());
        assertEquals(selected.getProductId(), selectedCaptor.getValue().getProductId());
        assertEquals(1, goodsListCaptor.getValue().size());
        assertEquals(selected.getProductId(), goodsListCaptor.getValue().get(0).getProductId());
    }

    @Test
    void checkEligibility_shouldReturnBooleanList() throws Exception {
        GoodsBaseVO selected = new GoodsBaseVO(1L, "portfolio", "PORTFOLIO", 88);
        CheckEligibilityQuery command = new CheckEligibilityQuery(selected, Arrays.asList(1, 2));
        when(productGiftManager.checkEligibility(any(GoodsBaseVO.class), any())).thenReturn(Arrays.asList(true, false));

        mockMvc.perform(post("/api/gifts/check-eligibility")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1]").value(false));

        ArgumentCaptor<GoodsBaseVO> selectedCaptor = ArgumentCaptor.forClass(GoodsBaseVO.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Integer>> userIdsCaptor = ArgumentCaptor.forClass(List.class);
        verify(productGiftManager).checkEligibility(selectedCaptor.capture(), userIdsCaptor.capture());
        assertEquals(selected.getProductId(), selectedCaptor.getValue().getProductId());
        assertEquals(Arrays.asList(1, 2), userIdsCaptor.getValue());
    }

    @Test
    void grantBatch_shouldReturnBooleanList() throws Exception {
        GoodsBaseVO selected = new GoodsBaseVO(1L, "portfolio", "PORTFOLIO", 88);
        GrantBatchQuery command = new GrantBatchQuery(selected, "period:7", Collections.singletonList(9001));
        when(productGiftManager.grantBatch(any(GoodsBaseVO.class), any(), anyString())).thenReturn(Collections.singletonList(true));

        mockMvc.perform(post("/api/gifts/grant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(true));

        ArgumentCaptor<GoodsBaseVO> selectedCaptor = ArgumentCaptor.forClass(GoodsBaseVO.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Integer>> candidatesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> attrCaptor = ArgumentCaptor.forClass(String.class);
        verify(productGiftManager).grantBatch(selectedCaptor.capture(), candidatesCaptor.capture(), attrCaptor.capture());
        assertEquals(selected.getProductId(), selectedCaptor.getValue().getProductId());
        assertEquals(1, candidatesCaptor.getValue().size());
        assertEquals(Integer.valueOf(9001), candidatesCaptor.getValue().get(0));
        assertEquals("period:7", attrCaptor.getValue());
    }
}
