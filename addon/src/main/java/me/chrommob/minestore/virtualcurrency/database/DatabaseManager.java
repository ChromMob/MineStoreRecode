package me.chrommob.minestore.virtualcurrency.database;

import java.io.*;
import java.sql.*;

public class DatabaseManager {
    private final File databaseFile;
    private final Connection connection;

    public DatabaseManager(File databaseFile) {
        this.databaseFile = databaseFile;
        this.connection = openConnection();
    }

    private Connection openConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            conn.setAutoCommit(true);
            return conn;
        } catch (Exception e) {
            throw new RuntimeException("Failed to open SQLite connection", e);
        }
    }

    public void initialize() {
        executeUpdate("CREATE TABLE IF NOT EXISTS player_balances (" +
                "username TEXT PRIMARY KEY, " +
                "balance REAL DEFAULT 0, " +
                "total_earned REAL DEFAULT 0, " +
                "total_spent REAL DEFAULT 0, " +
                "transaction_count INTEGER DEFAULT 0, " +
                "last_seen INTEGER)");

        executeUpdate("CREATE TABLE IF NOT EXISTS balance_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "action TEXT, " +
                "amount REAL, " +
                "balance_after REAL, " +
                "reason TEXT, " +
                "timestamp INTEGER)");

        executeUpdate("CREATE TABLE IF NOT EXISTS daily_login (" +
                "username TEXT PRIMARY KEY, " +
                "last_claim_date INTEGER, " +
                "streak INTEGER DEFAULT 0, " +
                "total_claims INTEGER DEFAULT 0)");

        executeUpdate("CREATE INDEX IF NOT EXISTS idx_history_username ON balance_history(username)");
        executeUpdate("CREATE INDEX IF NOT EXISTS idx_history_timestamp ON balance_history(timestamp)");
    }

    public void executeUpdate(String sql) {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute update: " + sql, e);
        }
    }

    public ResultSet executeQuery(String sql) {
        try {
            return connection.createStatement().executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute query: " + sql, e);
        }
    }

    public Double getBalance(String username) {
        String sql = "SELECT balance FROM player_balances WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get balance", e);
        }
        return null;
    }

    public void setBalance(String username, double balance) {
        String sql = "INSERT OR REPLACE INTO player_balances (username, balance, total_earned, total_spent, transaction_count, last_seen) VALUES (?, ?, 0, 0, 0, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username.toLowerCase());
            stmt.setDouble(2, balance);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set balance", e);
        }
    }

    public void addBalance(String username, double amount) {
        String sql = "INSERT INTO player_balances (username, balance, total_earned, total_spent, transaction_count, last_seen) " +
                "VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT(username) DO UPDATE SET " +
                "balance = balance + ?, " +
                "total_earned = total_earned + CASE WHEN ? > 0 THEN ? ELSE 0 END, " +
                "total_spent = total_spent + CASE WHEN ? < 0 THEN ? ELSE 0 END, " +
                "transaction_count = transaction_count + 1, " +
                "last_seen = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username.toLowerCase());
            stmt.setDouble(2, amount);
            stmt.setDouble(3, amount > 0 ? amount : 0);
            stmt.setDouble(4, amount < 0 ? -amount : 0);
            stmt.setInt(5, 1);
            stmt.setLong(6, System.currentTimeMillis());
            stmt.setDouble(7, amount);
            stmt.setDouble(8, amount);
            stmt.setDouble(9, amount);
            stmt.setDouble(10, amount);
            stmt.setDouble(11, amount);
            stmt.setLong(12, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add balance", e);
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close database", e);
        }
    }

    public void recordTransaction(String username, String action, double amount, double balanceAfter, String reason) {
        String sql = "INSERT INTO balance_history (username, action, amount, balance_after, reason, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username.toLowerCase());
            stmt.setString(2, action);
            stmt.setDouble(3, amount);
            stmt.setDouble(4, balanceAfter);
            stmt.setString(5, reason);
            stmt.setLong(6, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to record transaction", e);
        }
    }

    public java.util.List<String[]> getTransactionHistory(String username, int limit) {
        java.util.List<String[]> results = new java.util.ArrayList<>();
        String sql = "SELECT action, amount, balance_after, reason, timestamp FROM balance_history WHERE username = ? ORDER BY timestamp DESC LIMIT ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username.toLowerCase());
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new String[]{
                            rs.getString("action"),
                            String.valueOf(rs.getDouble("amount")),
                            String.valueOf(rs.getDouble("balance_after")),
                            rs.getString("reason"),
                            String.valueOf(rs.getLong("timestamp"))
                    });
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get transaction history", e);
        }
        return results;
    }

    public java.util.List<String[]> getTopBalances(int limit) {
        java.util.List<String[]> results = new java.util.ArrayList<>();
        String sql = "SELECT username, balance, total_earned, transaction_count FROM player_balances ORDER BY balance DESC LIMIT ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new String[]{
                            rs.getString("username"),
                            String.valueOf(rs.getDouble("balance")),
                            String.valueOf(rs.getDouble("total_earned")),
                            String.valueOf(rs.getInt("transaction_count"))
                    });
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get top balances", e);
        }
        return results;
    }

    public int getTotalPlayers() {
        String sql = "SELECT COUNT(*) as count FROM player_balances";
        try (ResultSet rs = executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get total players", e);
        }
        return 0;
    }

    public double getTotalEconomyBalance() {
        String sql = "SELECT SUM(balance) as total FROM player_balances";
        try (ResultSet rs = executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get total economy balance", e);
        }
        return 0.0;
    }

    public long getTotalTransactions() {
        String sql = "SELECT SUM(transaction_count) as total FROM player_balances";
        try (ResultSet rs = executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong("total");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get total transactions", e);
        }
        return 0;
    }

    public int getRank(String username) {
        String sql = "SELECT COUNT(*) as rank FROM player_balances WHERE balance > (SELECT balance FROM player_balances WHERE username = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("rank") + 1;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get rank", e);
        }
        return 0;
    }

    public long getLastClaimDate(String username) {
        String sql = "SELECT last_claim_date FROM daily_login WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("last_claim_date");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get last claim date", e);
        }
        return 0;
    }

    public int getLoginStreak(String username) {
        String sql = "SELECT streak FROM daily_login WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("streak");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get login streak", e);
        }
        return 0;
    }

    public void updateDailyLogin(String username, long claimDate, int streak) {
        String sql = "INSERT OR REPLACE INTO daily_login (username, last_claim_date, streak, total_claims) VALUES (?, ?, ?, COALESCE((SELECT total_claims FROM daily_login WHERE username = ?), 0) + 1)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username.toLowerCase());
            stmt.setLong(2, claimDate);
            stmt.setInt(3, streak);
            stmt.setString(4, username.toLowerCase());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update daily login", e);
        }
    }
}
