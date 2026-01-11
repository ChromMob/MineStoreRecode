package me.chrommob.minestore.api.interfaces.gui;

public final class EnchantmentData {
    private final String name;
    private final int level;

    public EnchantmentData(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public String name() {
        return name;
    }

    public int level() {
        return level;
    }
}
