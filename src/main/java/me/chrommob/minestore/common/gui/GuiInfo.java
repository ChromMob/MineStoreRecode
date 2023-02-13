package me.chrommob.minestore.common.gui;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GuiInfo {
    public enum MENU_TYPE {
        MAIN,
        SUBCATEGORY,
        PACKAGE
    }
    private Map<UUID, MENU_TYPE> menuType = new ConcurrentHashMap<>();

    public void setMenuType(UUID uuid, MENU_TYPE type) {
        menuType.put(uuid, type);
    }

    public MENU_TYPE getMenuType(UUID uuid) {
        return menuType.get(uuid);
    }
}
