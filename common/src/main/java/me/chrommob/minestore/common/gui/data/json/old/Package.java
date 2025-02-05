package me.chrommob.minestore.common.gui.data.json.old;

@SuppressWarnings("unused")
public class Package {
    private int id;
    private String name;
    private double price;
    private double discount;
    private int sorting;
    private String category_url;
    private int featured;
    private int active;
    private boolean virtual_currency;
    private String item_id;
    private String item_lore;

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public double getDiscount() {
        return discount;
    }

    public int getSorting() {
        return sorting;
    }

    public String getCategory_url() {
        return category_url;
    }

    public int getFeatured() {
        return featured;
    }

    public int getActive() {
        return active;
    }

    public boolean isVirtualCurrency() {
        return virtual_currency;
    }

    public String getItem_id() {
        return item_id;
    }

    public String getItem_lore() {
        return item_lore;
    }

    public int getId() {
        return id;
    }
}
