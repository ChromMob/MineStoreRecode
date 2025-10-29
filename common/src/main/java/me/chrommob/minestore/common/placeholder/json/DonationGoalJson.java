package me.chrommob.minestore.common.placeholder.json;

import me.chrommob.minestore.libs.com.google.gson.annotations.SerializedName;

public class DonationGoalJson {
    @SerializedName("goal_amount")
    private double goal;
    @SerializedName("current_amount")
    private double goalSum;

    public DonationGoal getDonationGoal() {
        return new DonationGoal(goal, goalSum);
    }
}
