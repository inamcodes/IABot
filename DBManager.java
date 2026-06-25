import java.sql.*;

public class DBManager {
    private static final String DB_URL = "jdbc:sqlite:iabot.db";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
        return DriverManager.getConnection(DB_URL);
    }

    public static int getUserIdByName(String name) throws SQLException {
        String sql = "SELECT uid FROM Users WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("uid");
        }
        return -1;
    }

    public static int createConversation(int uid, String title) throws SQLException {
    try (Connection conn = getConnection()) {
        conn.setAutoCommit(false); // optional but clean
        String safeTitle = title.replace("'", "''");
        String insertSQL = "INSERT INTO Conversations (uid, title) VALUES (" + uid + ", '" + safeTitle + "')";
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(insertSQL);

        // Manual retrieval of last inserted id
        ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
        int cid = rs.next() ? rs.getInt(1) : -1;

        conn.commit();
        return cid;
        } catch (SQLException e) {
            // no popups
            System.err.println("Failed to create conversation: " + e.getMessage());
            return -1;
        }
    }


    public static ResultSet getAllConversationsForUser(int uid) throws SQLException {
        String sql = "SELECT * FROM Conversations WHERE uid = ?";
        Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, uid);
        return ps.executeQuery();
    }

    public static void insertPrompt(int cid, String q, String a) throws SQLException {
        String sql = "INSERT INTO Prompts (cid, question, answer) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cid);
            ps.setString(2, q);
            ps.setString(3, a);
            ps.executeUpdate();
        }
    }

    public static ResultSet getPromptsByConversation(int cid) throws SQLException {
        String sql = "SELECT question, answer FROM Prompts WHERE cid = ?";
        Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, cid);
        return ps.executeQuery();
    }
    
    public static boolean registerUser(String name, String email, String password) throws SQLException {
    String checkSql = "SELECT uid FROM Users WHERE name = ?";
    try (Connection conn = getConnection();
         PreparedStatement check = conn.prepareStatement(checkSql)) {
        check.setString(1, name);
        ResultSet rs = check.executeQuery();
        if (rs.next()) return false; // user exists
    }

    String insertSql = "INSERT INTO Users (name, email, password) VALUES (?, ?, ?)";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(insertSql)) {
        ps.setString(1, name);
        ps.setString(2, email);
        ps.setString(3, password);
        ps.executeUpdate();
        return true;
        }
    }

    public static int authenticateUser(String name, String password) throws SQLException {
        String sql = "SELECT uid FROM Users WHERE name = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("uid");
        }
        return -1;
    }
    
    public static void deleteConversation(int cid) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement deletePrompts = conn.prepareStatement("DELETE FROM Prompts WHERE cid = ?");
            deletePrompts.setInt(1, cid);
            deletePrompts.executeUpdate();

            PreparedStatement deleteConversation = conn.prepareStatement("DELETE FROM Conversations WHERE cid = ?");
            deleteConversation.setInt(1, cid);
            deleteConversation.executeUpdate();
            }
    }
    
    public static void renameConversation(int cid, String newTitle) throws SQLException {
    String sql = "UPDATE Conversations SET title = ? WHERE cid = ?";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, newTitle);
        ps.setInt(2, cid);
        ps.executeUpdate();
    }
}



}

