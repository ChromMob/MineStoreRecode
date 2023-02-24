package me.chrommob.minestore.common.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    }

    private Map<String, PlayerData> playerData = new ConcurrentHashMap<>();
    public void onPlayerJoin(String name) {
        playerData.put(name, new PlayerData(plugin.userGetter().get(name)));
        plugin.debug("Added " + name + " to playerData");
    }

    public void onPlayerQuit(String name) {
        playerData.remove(name);
        plugin.debug("Removed " + name + " from playerData");
    }

    public boolean load() {
        host = (String) plugin.configReader().get(ConfigKey.MYSQL_HOST);
        port = (int) plugin.configReader().get(ConfigKey.MYSQL_PORT);
        database = (String) plugin.configReader().get(ConfigKey.MYSQL_DATABASE);
        username = (String) plugin.configReader().get(ConfigKey.MYSQL_USERNAME);
        password = (String) plugin.configReader().get(ConfigKey.MYSQL_PASSWORD);
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
            return false;
        } else {
            plugin.log("Connected to database!");
            return true;
        }
    }

    private boolean tryType(DatabaseType type) {
        finalUrl = type.protocol() + host + ":" + port + "/" + database + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        try {
            hikari = new HikariDataSource();
            hikari.setJdbcUrl(finalUrl);
            hikari.setUsername(username);
            switch (type) {
                case MARIADB:
                    driverClass = "org.mariadb.jdbc.Driver";
                    break;
                case MYSQL:
                    driverClass = "com.mysql.jdbc.Driver";
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
            hikari.getConnection().close();
            return true;
        } catch (Exception e) {
            plugin.debug(e);
            return false;
        }
    }

    public void start() {
        new Thread(this::createTable).start();
        if (thread != null) {
            thread.interrupt();
        }
        thread = new Thread(updater);
        thread.start();
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
        if (hikari != null) {
            hikari.close();
        }
    }

    private Runnable updater = () -> {
        while (true) {
            update();
            try {
                Thread.sleep(1000 * 10);
            } catch (InterruptedException e) {
                break;
            }
        }
    };

    private void update() {
        Set<PlayerData> changed = ConcurrentHashMap.newKeySet();
        for (PlayerData data : playerData.values()) {
            if (data.hasChanged()) {
                changed.add(data);
            }
        }
        plugin.debug("Updating " + changed.size() + " players out of total " + playerData.size());
        if (changed.isEmpty()) {
            return;
        }
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = hikari.getConnection();
            for (PlayerData data : changed) {
                plugin.debug("Updating " + data.getName());
                String update = "INSERT INTO playerdata (uuid, username, prefix, suffix, balance, player_group) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE username = ?, prefix = ?, suffix = ?, balance = ?, player_group = ?";
                ps = conn.prepareStatement(update);
                ps.setString(1, data.getUuid().toString());
                ps.setString(2, data.getName());
                ps.setString(3, data.getPrefix());
                ps.setString(4, data.getSuffix());
                ps.setDouble(5, data.getBalance());
                ps.setString(6, data.getPlayerGroup());
                ps.setString(7, data.getName());
                ps.setString(8, data.getPrefix());
                ps.setString(9, data.getSuffix());
                ps.setDouble(10, data.getBalance());
                ps.setString(11, data.getPlayerGroup());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void createTable() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            MineStoreCommon.getInstance().debug(hikari == null ? "Connection is null" : "Connection is not null");
            conn = hikari.getConnection();
            String createTable = "CREATE TABLE IF NOT EXISTS playerdata"
                    + "  (uuid           VARCHAR(255) UNIQUE,"
                    + "   username       VARCHAR(255) NOT NULL default '',"
                    + "   prefix         VARCHAR(255) NOT NULL default '',"
                    + "   suffix         VARCHAR(255) NOT NULL default '',"
                    + "   balance             DOUBLE NOT NULL default 0.00,"
                    + "   player_group          VARCHAR(255) NOT NULL default 0,"
                    + "   PRIMARY KEY  (uuid));";
            ps = conn.prepareStatement(createTable);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, ps, null);
        }
    }

    private void close(Connection conn, PreparedStatement ps, ResultSet res) {
        if (conn != null)
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        if (ps != null)
            try {
                ps.close();
            } catch (SQLException ignored) {
            }
        if (res != null)
            try {
                res.close();
            } catch (SQLException ignored) {
            }
    }
}
