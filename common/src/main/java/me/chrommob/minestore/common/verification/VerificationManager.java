package me.chrommob.minestore.common.verification;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.common.MineStoreCommon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;

import java.time.Duration;

public class VerificationManager {
    private static final short SLIDING_WINDOW_SIZE = 150;
    private final MineStoreCommon plugin;
    private final VerificationResult verificationResult;
    private short errorCount = 0;
    private short successCount = 0;
    private float errorRate = 0;

    private final Component log;

    public VerificationManager(MineStoreCommon plugin, VerificationResult verificationResult, Component log) {
        this.plugin = plugin;
        this.verificationResult = verificationResult;
        this.log = log;
        log();
    }

    public void safeIncrementError() {
        errorCount += 1;
        if (errorCount == SLIDING_WINDOW_SIZE) {
            if (successCount > 0) {
                successCount--;
            }
            errorCount--;
        }
        errorRate = (float) errorCount / successCount;
    }

    public void safeIncrementSuccess() {
        successCount += 1;
        if (successCount == SLIDING_WINDOW_SIZE) {
            if (errorCount > 0) {
                errorCount--;
            }
            successCount--;
        }
        errorRate =  (float) errorCount / successCount;
    }

    public float getErrorRate() {
        return errorRate;
    }

    private Component getMessage(VerificationResult.TYPE type) {
        switch (type) {
            case STORE_URL:
                return Component.text("Store URL is not configured correctly. Please check your config.yml").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
            case API_KEY:
                return Component.text("API key is not configured correctly. Please check your config.yml").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
            case SECRET_KEY:
                return Component.text("The secret key you entered is not valid. Please check your config.yml and use /ms setup WEBLISTENER.SECRET-KEY secretKey to set it.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
            case WEBSTORE:
                return Component.text("The server returned an error. Make sure it is accessible from your MC server and that Cloudflare is not blocking it.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
            case DATABASE:
                return Component.text("Database is not configured correctly. Please check your config.yml and make sure the database is accessible from your MC server.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
            default:
                return Component.text("There has been an internal error in the MineStore plugin. Please contact the support.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
        }
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
        user.sendMessage(Component.text("[MineStore] ERROR: ").color(NamedTextColor.RED).append(getMessage(verificationResult.type())));
        for (String message : verificationResult.messages()) {
            user.sendMessage("- " + message);
        }
        Component title = Component.text("[MineStore ERROR]").color(NamedTextColor.LIGHT_PURPLE);
        Component subtitle = getMessage(verificationResult.type());
        Title titleMessage = Title.title(title, subtitle, Title.Times.times(Duration.ofSeconds(2), Duration.ofSeconds(5), Duration.ofSeconds(2)));
        user.sendTitle(titleMessage);
        if (log == null) {
            return;
        }
        user.sendMessage(log);
    }

    public void log() {
        if (verificationResult.isValid()) {
            return;
        }
        plugin.log(Component.text("[MineStore] ERROR: ").color(NamedTextColor.RED).append(getMessage(verificationResult.type())));
        plugin.log(getMessage(verificationResult.type()));
        for (String message : verificationResult.messages()) {
            plugin.log(message);
        }
        if (log == null) {
            return;
        }
        plugin.log(log);
    }

    public boolean isValid() {
        return verificationResult.isValid();
    }
}
