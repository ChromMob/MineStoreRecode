package me.chrommob.minestore.common.placeholder.json;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class DonationGoal {
    @SerializedName("goal")
    private double donationGoalAmount;

    @SerializedName("goal_sum")
    private double donationGoalCurrentAmount;

    public double getDonationGoalAmount() {
        return donationGoalAmount;
    }

    public double getDonationGoalCurrentAmount() {
        return donationGoalCurrentAmount;
    }

    public int getDonationGoalPercentage() {
        return (int) (donationGoalCurrentAmount / donationGoalAmount * 100);
    }
}
