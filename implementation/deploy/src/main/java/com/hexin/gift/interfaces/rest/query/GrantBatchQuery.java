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
    private Integer attr;

    @NotNull
    private Integer advisorId;

    @NotEmpty
    private List<Integer> candidates;

    public GrantBatchQuery() {
    }

    public GrantBatchQuery(GoodsBaseVO selectedGood,
                            Integer attr,
                            Integer advisorId,
                            List<Integer> candidates) {
        this.selectedGood = selectedGood;
        this.attr = attr;
        this.advisorId = advisorId;
        this.candidates = candidates;
    }

    public GoodsBaseVO getSelectedGood() {
        return selectedGood;
    }

    public void setSelectedGood(GoodsBaseVO selectedGood) {
        this.selectedGood = selectedGood;
    }

    public Integer getAttr() {
        return attr;
    }

    public void setAttr(Integer attr) {
        this.attr = attr;
    }

    public Integer getAdvisorId() {
        return advisorId;
    }

    public void setAdvisorId(Integer advisorId) {
        this.advisorId = advisorId;
    }

    public List<Integer> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Integer> candidates) {
        this.candidates = candidates;
    }
}
