package me.chrommob.minestore.common.placeholder.json;

import com.google.gson.annotations.SerializedName;

public class DonationGoalJsonOld {
    @SerializedName("goal")
    private double donationGoalAmount = 0;

    @SerializedName("goal_sum")
    private double donationGoalCurrentAmount = 0;

    public DonationGoal getDonationGoal() {
        return new DonationGoal(donationGoalAmount, donationGoalCurrentAmount);
    }
}
