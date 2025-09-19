package com.hexin.gift.interfaces.rest.query;

import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Query payload for granting gifts to multiple candidates.
 */
public class GrantBatchQuery {

    @Valid
    @NotNull
    private GoodsBaseVO selectedGood;

    @NotNull
    private String attr;

    @NotEmpty
    private List<Integer> candidates;

    public GrantBatchQuery() {
    }

    public GrantBatchQuery(GoodsBaseVO selectedGood,
                            String attr,
                            List<Integer> candidates) {
        this.selectedGood = selectedGood;
        this.attr = attr;
        this.candidates = candidates;
    }

    public GoodsBaseVO getSelectedGood() {
        return selectedGood;
    }

    public void setSelectedGood(GoodsBaseVO selectedGood) {
        this.selectedGood = selectedGood;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public List<Integer> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Integer> candidates) {
        this.candidates = candidates;
    }
}
