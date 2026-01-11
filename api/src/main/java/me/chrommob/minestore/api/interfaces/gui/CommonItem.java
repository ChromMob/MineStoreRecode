package me.chrommob.minestore.api.interfaces.gui;

import me.chrommob.minestore.api.event.types.GuiClickEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class CommonItem {
    private final Component name;
    private final List<Component> lore;
    private final String material;
    private final int amount;
    private final Consumer<GuiClickEvent> clickHandler;
    private final List<EnchantmentData> enchantments;

    public CommonItem(Component name, String material, List<Component> lore,
                      List<EnchantmentData> enchantments, int amount,
                      Consumer<GuiClickEvent> clickHandler) {
        this.name = name;
        this.material = material;
        this.lore = lore;
        this.amount = amount;
        this.clickHandler = clickHandler;
        this.enchantments = enchantments;
    }

    public CommonItem(Component name, String material, List<Component> lore,
                      List<EnchantmentData> enchantments, Consumer<GuiClickEvent> clickHandler) {
        this(name, material, lore, enchantments, 1, clickHandler);
    }

    public CommonItem(Component name, String material, List<Component> lore,
                      Consumer<GuiClickEvent> clickHandler) {
        this(name, material, lore, null, 1, clickHandler);
    }

    public CommonItem(Component name, String material, List<Component> lore, int amount) {
        this(name, material, lore, null, amount, null);
    }

    public CommonItem(Component name, String material, List<Component> lore) {
        this(name, material, lore, null, 1, null);
    }

    public CommonItem(CommonItem other, Consumer<GuiClickEvent> clickHandler) {
        this(other.getName(), other.getMaterial(), other.getLore(),
             other.getEnchantments(), other.getAmount(), clickHandler);
    }

    public Component getName() {
        return name;
    }

    public List<Component> getLore() {
        return lore;
    }

    public String getMaterial() {
        return material;
    }

    public boolean equals(CommonItem item) {
        @NotNull LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
        boolean name = serializer.serialize(this.name).equalsIgnoreCase(serializer.serialize(item.name));
        StringBuilder lore = new StringBuilder();
        StringBuilder lore2 = new StringBuilder();
        for (Component line : this.lore) {
            lore.append(serializer.serialize(line));
        }
        for (Component line : item.lore) {
            lore2.append(serializer.serialize(line));
        }
        boolean lore3 = lore.toString().equalsIgnoreCase(lore2.toString());
        String thisMaterial = this.material;
        if (thisMaterial == null) {
            thisMaterial = "CHEST";
        }
        thisMaterial = thisMaterial.replace("minecraft:", "");
        boolean material = thisMaterial.equalsIgnoreCase(item.material);
        boolean handler = this.clickHandler == item.clickHandler;
        return name && lore3 && material && handler;
    }

    @Override
    public int hashCode() {
        @NotNull LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
        int result = serializer.serialize(name).toLowerCase().hashCode();
        StringBuilder loreStr = new StringBuilder();
        for (Component line : lore) {
            loreStr.append(serializer.serialize(line));
        }
        result = 31 * result + loreStr.toString().toLowerCase().hashCode();
        String mat = material;
        if (mat == null) mat = "CHEST";
        mat = mat.replace("minecraft:", "").toLowerCase();
        result = 31 * result + mat.hashCode();
        result = 31 * result + (clickHandler != null ? clickHandler.hashCode() : 0);
        return result;
    }

    public boolean hasEnchantments() {
        return enchantments != null && !enchantments.isEmpty();
    }

    public List<EnchantmentData> getEnchantments() {
        return enchantments;
    }

    public int getAmount() {
        return amount;
    }

    public boolean hasClickHandler() {
        return clickHandler != null;
    }

    public Consumer<GuiClickEvent> getClickHandler() {
        return clickHandler;
    }

    public void invokeClickHandler(GuiClickEvent event) {
        if (clickHandler != null) {
            clickHandler.accept(event);
        }
    }
}
