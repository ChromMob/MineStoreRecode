package me.chrommob.minestore.common.placeholder.json;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class DonationGoal {
    @SerializedName("goal")
    private double donationGoalAmount = 0;

    @SerializedName("goal_sum")
    private double donationGoalCurrentAmount = 0;

    public double getDonationGoalAmount() {
        return donationGoalAmount;
    }

    public double getDonationGoalCurrentAmount() {
        return donationGoalCurrentAmount;
    }

    public int getDonationGoalPercentage() {
        if (donationGoalAmount == 0) {
            return 0;
        }
        return (int) (donationGoalCurrentAmount / donationGoalAmount * 100);
    }
}
