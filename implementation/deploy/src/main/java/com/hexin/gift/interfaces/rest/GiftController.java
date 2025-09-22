package com.hexin.gift.interfaces.rest;

import com.hexin.gift.app.manager.ProductGiftManager;
import com.hexin.gift.interfaces.rest.converter.GiftControllerConverter;
import com.hexin.gift.interfaces.rest.query.CheckEligibilityQuery;
import com.hexin.gift.interfaces.rest.query.GrantBatchQuery;
import com.hexin.gift.interfaces.rest.query.ListCandidatesQuery;
import com.hexin.gift.interfaces.rest.query.ListGoodsQuery;
import com.hexin.gift.interfaces.rest.vo.GiftCandidateVO;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * REST controller exposing gift-related APIs to the front-end.
 */
@RestController
@RequestMapping("/api/gifts")
@Validated
public class GiftController {

    private final ProductGiftManager productGiftManager;
    private final GiftControllerConverter converter;

    public GiftController(ProductGiftManager productGiftManager,
                          GiftControllerConverter converter) {
        this.productGiftManager = productGiftManager;
        this.converter = converter;
    }

    @GetMapping("/goods")
    public ResponseEntity<List<GoodsBaseVO>> listGoods(@Valid ListGoodsQuery query) {
        List<GoodsBaseVO> goods = productGiftManager.listGoods(query);
        return ResponseEntity.ok(converter.toGoodsResponse(goods));
    }

    @PostMapping("/candidates")
    public ResponseEntity<List<GiftCandidateVO>> listCandidates(@Valid @RequestBody ListCandidatesQuery query) {
        List<GiftCandidateVO> candidates = productGiftManager.listCandidates(query.getSelectedGood(), query.getAllGoods());
        return ResponseEntity.ok(converter.toCandidateResponse(candidates));
    }

    @PostMapping("/check-eligibility")
    public ResponseEntity<List<Boolean>> checkEligibility(@Valid @RequestBody CheckEligibilityQuery command) {
        // TODO 暂未对接外部校验接口，仅保留占位返回
        List<Boolean> result = productGiftManager.checkEligibility(command.getSelectedGood(), command.getUserIds());
        return ResponseEntity.ok(converter.toBooleanResponse(result));
    }

    @PostMapping("/grant")
    public ResponseEntity<List<Boolean>> grantBatch(@Valid @RequestBody GrantBatchQuery command) {
        List<Boolean> result = productGiftManager.grantBatch(command.getSelectedGood(),
                command.getCandidates(), command.getAttr());
        return ResponseEntity.ok(converter.toBooleanResponse(result));
    }
}
