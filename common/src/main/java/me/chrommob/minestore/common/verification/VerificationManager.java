package me.chrommob.minestore.common.verification;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class VerificationManager {
    private final VerificationResult verificationResult;

    public VerificationManager(VerificationResult verificationResult) {
        this.verificationResult = verificationResult;
        log();
    }

    public void onJoin(String username) {
        if (verificationResult.isValid()) {
            return;
        }
        AbstractUser abstractUser = new AbstractUser(username, null);
        CommonUser user = abstractUser.user();
        if (!user.hasPermission("minestore.admin")) {
            return;
        }
        switch (verificationResult.type()) {
            case STORE_URL:
                user.sendMessage(Component.text("[MineStore] ERROR: Store URL is not configured correctly. Please check your config.yml").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
                break;
            case API_KEY:
                user.sendMessage(Component.text("[MineStore] ERROR: API key is not configured correctly. Please check your config.yml").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
                break;
            case SECRET_KEY:
                user.sendMessage(Component.text("[MineStore] ERROR: The secret key you entered is not valid. Please check your config.yml and use /ms setup secret-key <secretKey> to set it.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
                break;
            case WEBSTORE:
                user.sendMessage(Component.text("[MineStore] ERROR: The server returned an error. Make sure it is accessible from your MC server and that Cloudflare is not blocking it.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
                break;
            case SUPPORT:
                user.sendMessage(Component.text("[MineStore] ERROR: There has been an internal error in the MineStore plugin. Please contact the support.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
                break;
            case DATABASE:
                user.sendMessage(Component.text("[MineStore] ERROR: Database is not configured correctly. Please check your config.yml and make sure the database is accessible from your MC server.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
                break;
        }
        for (String message : verificationResult.messages()) {
            user.sendMessage("- " + message);
        }
    }

    public void log() {
        if (verificationResult.isValid()) {
            return;
        }
        switch (verificationResult.type()) {
            case STORE_URL:
                Registries.LOGGER.get().log("ERROR: Store URL is not configured correctly. Please check your config.yml");
                break;
            case API_KEY:
                Registries.LOGGER.get().log("ERROR: API key is not configured correctly. Please check your config.yml");
                break;
            case SECRET_KEY:
                Registries.LOGGER.get().log("ERROR: The secret key you entered is not valid. Please check your config.yml and use /ms setup secret-key <secretKey> to set it.");
                break;
            case WEBSTORE:
                Registries.LOGGER.get().log("ERROR: The server returned an error. Make sure it is accessible from your MC server and that Cloudflare is not blocking it.");
                break;
            case SUPPORT:
                Registries.LOGGER.get().log("ERROR: There has been an internal error in the MineStore plugin. Please contact the support.");
                break;
            case DATABASE:
                Registries.LOGGER.get().log("ERROR: Database is not configured correctly. Please check your config.yml and make sure the database is accessible from your MC server.");
                break;
        }
        for (String message : verificationResult.messages()) {
            Registries.LOGGER.get().log(message);
        }
    }

    public boolean isValid() {
        return verificationResult.isValid();
    }

    public String[] messages() {
        return verificationResult.messages().toArray(new String[0]);
    }
}
