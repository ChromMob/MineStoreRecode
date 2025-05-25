package me.chrommob.minestore.common.placeholder.json;

@SuppressWarnings("unused")
public class DonationGoal {
    private final double donationGoalAmount;
    private final double donationGoalCurrentAmount;
    public DonationGoal(double donationGoalAmount, double donationGoalCurrentAmount) {
        this.donationGoalAmount = donationGoalAmount;
        this.donationGoalCurrentAmount = donationGoalCurrentAmount;
    }

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
