package com.hexin.gift.modules.gift.domain.service;

import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;

import java.util.List;

/**
 * Aggregates goods information from external sources for the UI.
 */
public interface GoodsAssembler {

    List<GoodsBaseVO> listGoods(Integer advisorId);
}
