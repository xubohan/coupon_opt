package com.hexin.gift.interfaces.rest.converter;

import com.hexin.gift.interfaces.rest.vo.GiftCandidateVO;
import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;

import java.util.List;

/**
 * Simple pass-through converter for controller level transformations.
 */
import org.springframework.stereotype.Component;

@Component
public class GiftControllerConverter {

    public List<GoodsBaseVO> toGoodsResponse(List<GoodsBaseVO> goods) {
        return goods;
    }

    public List<GiftCandidateVO> toCandidateResponse(List<GiftCandidateVO> candidates) {
        return candidates;
    }

    public List<Boolean> toBooleanResponse(List<Boolean> values) {
        return values;
    }
}
