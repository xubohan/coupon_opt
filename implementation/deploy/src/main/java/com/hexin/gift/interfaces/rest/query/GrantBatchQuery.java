package com.hexin.gift.interfaces.rest.query;

import com.hexin.gift.interfaces.rest.vo.GiftCandidateVO;
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

    @Valid
    @NotEmpty
    private List<GiftCandidateVO> candidates;

    @NotNull
    private Integer attr;

    @NotNull
    private String source;

    public GrantBatchQuery() {
    }

    public GrantBatchQuery(GoodsBaseVO selectedGood,
                            List<GiftCandidateVO> candidates,
                            Integer attr,
                            String source) {
        this.selectedGood = selectedGood;
        this.candidates = candidates;
        this.attr = attr;
        this.source = source;
    }

    public GoodsBaseVO getSelectedGood() {
        return selectedGood;
    }

    public void setSelectedGood(GoodsBaseVO selectedGood) {
        this.selectedGood = selectedGood;
    }

    public List<GiftCandidateVO> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<GiftCandidateVO> candidates) {
        this.candidates = candidates;
    }

    public Integer getAttr() {
        return attr;
    }

    public void setAttr(Integer attr) {
        this.attr = attr;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
