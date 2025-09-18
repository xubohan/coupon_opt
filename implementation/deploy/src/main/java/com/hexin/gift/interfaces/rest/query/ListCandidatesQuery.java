package com.hexin.gift.interfaces.rest.query;

import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Query payload for retrieving gift candidates.
 */
public class ListCandidatesQuery {

    @Valid
    @NotNull
    private GoodsBaseVO selectedGood;

    @Valid
    @NotNull
    private List<GoodsBaseVO> allGoods;

    public ListCandidatesQuery() {
    }

    public ListCandidatesQuery(GoodsBaseVO selectedGood, List<GoodsBaseVO> allGoods) {
        this.selectedGood = selectedGood;
        this.allGoods = allGoods;
    }

    public GoodsBaseVO getSelectedGood() {
        return selectedGood;
    }

    public void setSelectedGood(GoodsBaseVO selectedGood) {
        this.selectedGood = selectedGood;
    }

    public List<GoodsBaseVO> getAllGoods() {
        return allGoods;
    }

    public void setAllGoods(List<GoodsBaseVO> allGoods) {
        this.allGoods = allGoods;
    }
}
