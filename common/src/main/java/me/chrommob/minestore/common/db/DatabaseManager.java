package me.chrommob.minestore.common.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.event.MineStoreEventBus;
import me.chrommob.minestore.api.event.types.MineStorePlayerJoinEvent;
import me.chrommob.minestore.api.event.types.MineStorePlayerQuitEvent;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKeys;
import me.chrommob.minestore.api.scheduler.MineStoreScheduledTask;
import me.chrommob.minestore.common.verification.VerificationResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {
    private final MineStoreCommon plugin;
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private String finalUrl;
    private String driverClass;
    private Thread thread = null;

    private enum DatabaseType {
        MYSQL("jdbc:mysql://"),
        MARIADB("jdbc:mariadb://");

        private final String s;

        DatabaseType(String s) {
            this.s = s;
        }

        public String protocol() {
            return s;
        }
    }

    private HikariDataSource hikari;

    public DatabaseManager(MineStoreCommon plugin) {
        this.plugin = plugin;
        MineStoreEventBus.registerListener(plugin.getInternalAddon(), MineStorePlayerJoinEvent.class, event -> onPlayerJoin(event.getUsername()));
        MineStoreEventBus.registerListener(plugin.getInternalAddon(), MineStorePlayerQuitEvent.class, event -> onPlayerQuit(event.getUsername()));
    }

    private final Map<String, PlayerData> playerData = new ConcurrentHashMap<>();

    private void onPlayerJoin(String name) {
        playerData.put(name, new PlayerData(Registries.USER_GETTER.get().get(name).commonUser()));
        plugin.debug(this.getClass(), "Added " + name + " to playerData");
    }

    private void onPlayerQuit(String name) {
        playerData.remove(name);
        plugin.debug(this.getClass(), "Removed " + name + " from playerData");
    }

    public VerificationResult load() {
        host = ConfigKeys.MYSQL_KEYS.IP.getValue();
        port = ConfigKeys.MYSQL_KEYS.PORT.getValue();
        database = ConfigKeys.MYSQL_KEYS.DATABASE.getValue();
        username = ConfigKeys.MYSQL_KEYS.USERNAME.getValue();
        password = ConfigKeys.MYSQL_KEYS.PASSWORD.getValue();
        if (tryType(DatabaseType.MARIADB) || tryType(DatabaseType.MYSQL)) {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(driverClass);
            config.setJdbcUrl(finalUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(5000);
            config.setLeakDetectionThreshold(60000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            hikari = new HikariDataSource(config);
        }
        if (hikari == null) {
            plugin.log("Could not connect to database!");
            return new VerificationResult(false, Collections.singletonList("Could not connect to database!"), VerificationResult.TYPE.DATABASE);
        } else {
            plugin.log("Connected to database!");
            return VerificationResult.valid();
        }
    }

    private boolean tryType(DatabaseType type) {
        finalUrl = type.protocol() + host + ":" + port + "/" + database
                + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(finalUrl);
        hikari.setUsername(username);
        switch (type) {
            case MARIADB:
                driverClass = "org.mariadb.jdbc.Driver";
                break;
            case MYSQL:
                driverClass = "com.mysql.cj.jdbc.Driver";
                break;
        }
        hikari.setDriverClassName(driverClass);
        hikari.setPassword(password);
        hikari.setMaximumPoolSize(10);
        hikari.setConnectionTimeout(5000);
        hikari.setLeakDetectionThreshold(5000);
        hikari.setConnectionTestQuery("SELECT 1");
        hikari.setIdleTimeout(600000);
        hikari.setMaxLifetime(1800000);
        try (HikariDataSource hikariDataSource = new HikariDataSource(hikari); Connection ignored = hikariDataSource.getConnection()) {
            return true;
        } catch (Exception e) {
            plugin.debug(this.getClass(), "Could not connect to database using " + type.name());
            plugin.debug(this.getClass(), e);
            return false;
        }
    }

    public final MineStoreScheduledTask updaterTask = new MineStoreScheduledTask("updatePlayerData", this::update, 1000 * 10);

    private void update() {
        boolean syncBalance = ConfigKeys.MYSQL_KEYS.SYNC_BALANCE.getValue();
        boolean syncPrefix = ConfigKeys.MYSQL_KEYS.SYNC_PREFIX.getValue();
        boolean syncSuffix = ConfigKeys.MYSQL_KEYS.SYNC_SUFFIX.getValue();
        boolean syncPlayerGroup = ConfigKeys.MYSQL_KEYS.SYNC_PLAYER_GROUP.getValue();
        Set<PlayerData> changed = ConcurrentHashMap.newKeySet();
        for (PlayerData data : playerData.values()) {
            if (data.hasChanged(syncBalance, syncPrefix, syncSuffix, syncPlayerGroup)) {
                changed.add(data);
            }
        }
        plugin.debug(this.getClass(), "Updating " + changed.size() + " players out of total " + playerData.size());
        if (changed.isEmpty()) {
            return;
        }
        List<String> syncedColumns = new ArrayList<>();
        if (syncPrefix) syncedColumns.add("prefix");
        if (syncSuffix) syncedColumns.add("suffix");
        if (syncBalance) syncedColumns.add("balance");
        if (syncPlayerGroup) syncedColumns.add("player_group");
        String update = createUpsertQuery(syncedColumns);
        try (Connection conn = hikari.getConnection()) {
            for (PlayerData data : changed) {
                plugin.debug(this.getClass(), "Updating " + data.getName());
                String prefix = syncPrefix ? data.getPrefix() : "";
                String suffix = syncSuffix ? data.getSuffix() : "";
                double balance = syncBalance ? data.getBalance() : 0;
                String playerGroup = syncPlayerGroup ? data.getPlayerGroup() : "";
                try (PreparedStatement ps = conn.prepareStatement(update)) {
                    int parameter = 1;
                    ps.setString(parameter++, data.getUuid().toString());
                    ps.setString(parameter++, data.getName());
                    parameter = setSyncedValues(ps, parameter, syncBalance, syncPrefix, syncSuffix,
                            syncPlayerGroup, balance, prefix, suffix, playerGroup);
                    ps.setString(parameter++, data.getName());
                    setSyncedValues(ps, parameter, syncBalance, syncPrefix, syncSuffix,
                            syncPlayerGroup, balance, prefix, suffix, playerGroup);
                    ps.executeUpdate();
                    data.markSynced(syncBalance, syncPrefix, syncSuffix, syncPlayerGroup,
                            balance, prefix, suffix, playerGroup);
                }
            }
        } catch (SQLException e) {
            plugin.debug(this.getClass(), e);
        }
    }

    static String createUpsertQuery(List<String> syncedColumns) {
        StringBuilder columns = new StringBuilder("uuid, username");
        StringBuilder values = new StringBuilder("?, ?");
        StringBuilder updates = new StringBuilder("username = ?");
        for (String column : syncedColumns) {
            columns.append(", ").append(column);
            values.append(", ?");
            updates.append(", ").append(column).append(" = ?");
        }
        return "INSERT INTO playerdata (" + columns + ") VALUES (" + values
                + ") ON DUPLICATE KEY UPDATE " + updates;
    }

    private int setSyncedValues(PreparedStatement ps, int parameter,
                                boolean syncBalance, boolean syncPrefix, boolean syncSuffix,
                                boolean syncPlayerGroup, double balance, String prefix,
                                String suffix, String playerGroup) throws SQLException {
        if (syncPrefix) ps.setString(parameter++, prefix);
        if (syncSuffix) ps.setString(parameter++, suffix);
        if (syncBalance) ps.setDouble(parameter++, balance);
        if (syncPlayerGroup) ps.setString(parameter++, playerGroup);
        return parameter;
    }

    private void createTable() {
        String createTable = "CREATE TABLE IF NOT EXISTS playerdata"
                + "  (uuid           VARCHAR(255) UNIQUE,"
                + "   username       VARCHAR(255) NOT NULL default '',"
                + "   prefix         VARCHAR(255) NOT NULL default '',"
                + "   suffix         VARCHAR(255) NOT NULL default '',"
                + "   balance             DOUBLE NOT NULL default 0.00,"
                + "   player_group          VARCHAR(255) NOT NULL default 0,"
                + "   PRIMARY KEY  (uuid));";
        try (Connection conn = hikari.getConnection(); PreparedStatement ps = conn.prepareStatement(createTable)) {
            plugin.debug(this.getClass(), hikari == null ? "Connection is null" : "Connection is not null");
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.debug(this.getClass(), e);
        }
    }
}
