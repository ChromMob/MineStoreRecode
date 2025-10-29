package me.chrommob.minestore.common.paynow.json;


import java.time.Instant;

@SuppressWarnings("unused")
public class PayNowEvent {
    private final String event;
    private final PlayerJoin player_join;
    private final String timestamp;

    public PayNowEvent(String event, PlayerJoin playerJoin) {
        this.event = event;
        this.player_join = playerJoin;
        this.timestamp = Instant.now().toString();
    }

    public static class PlayerJoin {
        private final String ip_address;
        private final String minecraft_uuid;
        private final String minecraft_name;

        public PlayerJoin(String ipAddress, String minecraftUuid, String minecraftName) {
            this.ip_address = ipAddress;
            this.minecraft_uuid = minecraftUuid;
            this.minecraft_name = minecraftName;
        }
    }
}
