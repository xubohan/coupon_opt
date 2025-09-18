package com.hexin.gift.interfaces.rest.query;

import com.hexin.gift.interfaces.rest.vo.GoodsBaseVO;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Query payload for eligibility checks.
 */
public class CheckEligibilityQuery {

    @Valid
    @NotNull
    private GoodsBaseVO selectedGood;

    @NotEmpty
    private List<Integer> userIds;

    public CheckEligibilityQuery() {
    }

    public CheckEligibilityQuery(GoodsBaseVO selectedGood, List<Integer> userIds) {
        this.selectedGood = selectedGood;
        this.userIds = userIds;
    }

    public GoodsBaseVO getSelectedGood() {
        return selectedGood;
    }

    public void setSelectedGood(GoodsBaseVO selectedGood) {
        this.selectedGood = selectedGood;
    }

    public List<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
    }
}
