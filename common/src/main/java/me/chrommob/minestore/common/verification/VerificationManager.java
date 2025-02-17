package me.chrommob.minestore.common.verification;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.common.MineStoreCommon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class VerificationManager {
    private final MineStoreCommon plugin;
    private final VerificationResult verificationResult;

    public VerificationManager(MineStoreCommon plugin, VerificationResult verificationResult) {
        this.plugin = plugin;
        this.verificationResult = verificationResult;
        log();
    }

    private Component getMessage(VerificationResult.TYPE type) {
        switch (type) {
            case STORE_URL:
                return Component.text("[MineStore] ERROR: Store URL is not configured correctly. Please check your config.yml").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
            case API_KEY:
                return Component.text("[MineStore] ERROR: API key is not configured correctly. Please check your config.yml").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
            case SECRET_KEY:
                return Component.text("[MineStore] ERROR: The secret key you entered is not valid. Please check your config.yml and use /ms setup secret-key <secretKey> to set it.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
            case WEBSTORE:
                return Component.text("[MineStore] ERROR: The server returned an error. Make sure it is accessible from your MC server and that Cloudflare is not blocking it.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
            case DATABASE:
                return Component.text("[MineStore] ERROR: Database is not configured correctly. Please check your config.yml and make sure the database is accessible from your MC server.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
            default:
                return Component.text("[MineStore] ERROR: There has been an internal error in the MineStore plugin. Please contact the support.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
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
        user.sendMessage(getMessage(verificationResult.type()));
        for (String message : verificationResult.messages()) {
            user.sendMessage("- " + message);
        }
    }

    public void log() {
        if (verificationResult.isValid()) {
            return;
        }
        plugin.log(PlainTextComponentSerializer.plainText().serialize(getMessage(verificationResult.type())));
        for (String message : verificationResult.messages()) {
            plugin.log(message);
        }
    }

    public boolean isValid() {
        return verificationResult.isValid();
    }
}
