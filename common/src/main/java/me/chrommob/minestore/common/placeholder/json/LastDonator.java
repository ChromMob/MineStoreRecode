package me.chrommob.minestore.common.placeholder.json;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class LastDonator {
    public LastDonator(String name) {
        this.userName = name;
        this.price = 0;
        this.packageName = "";
        this.date = "1900-1-1 0:0:0";
    }

    public LastDonator() {
        this("");
    }

    private static final LastDonator DEFAULT = new LastDonator();

    public static LastDonator getDefault() {
        return DEFAULT;
    }

    @SerializedName("user")
    private String userName;

    @SerializedName("amount")
    private double price;

    @SerializedName("package")
    private String packageName;

    private String date;

    public static class Date {
        private final int year;
        private final int month;
        private final int day;

        private final int hour;
        private final int minute;
        private final int second;

        public Date(String date) {
            String[] split = date.split(" ");
            String[] dateSplit = split[0].split("-");
            year = Integer.parseInt(dateSplit[0]);
            month = Integer.parseInt(dateSplit[1]);
            day = Integer.parseInt(dateSplit[2]);
            String[] timeSplit = split[1].split(":");
            hour = Integer.parseInt(timeSplit[0]);
            minute = Integer.parseInt(timeSplit[1]);
            second = Integer.parseInt(timeSplit[2]);
        }

        public int compareTo(Date other) {
            if (other.year > year) {
                return 1;
            }
            if (other.year < year) {
                return -1;
            }
            if (other.month > month) {
                return 1;
            }
            if (other.month < month) {
                return -1;
            }
            if (other.day > day) {
                return 1;
            }
            if (other.day < day) {
                return -1;
            }
            if (other.hour > hour) {
                return 1;
            }
            if (other.hour < hour) {
                return -1;
            }
            if (other.minute > minute) {
                return 1;
            }
            if (other.minute < minute) {
                return -1;
            }
            return Integer.compare(other.second, second);
        }
    }

    public int compareTo(LastDonator other) {
        return getDate().compareTo(other.getDate());
    }

    public Date getDate() {
        return new Date(date);
    }

    public String getUserName() {
        return userName;
    }

    public double getPrice() {
        return price;
    }

    public String getPackageName() {
        return packageName;
    }
}
