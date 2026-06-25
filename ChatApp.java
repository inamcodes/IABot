import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ChatApp extends JFrame {
    private DefaultListModel<String> conversationListModel;
    private JList<String> conversationList;
    private JTextPane chatArea;
    private JTextField inputField;
    private int currentCid = -1;
    private int userId = -1;
    private final StringBuilder chatContent = new StringBuilder("<html><body style='font-family:Segoe UI;'>");

    public ChatApp(int userId) {
        this.userId = userId;
        setTitle("🧠 AI Chatbot - IABot");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        buildUI();
        loadConversations();
    }

    private void buildUI() {
        Font font = new Font("Segoe UI", Font.PLAIN, 14);

        // --- Sidebar ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(new Color(245, 245, 245));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        JButton newChatBtn = new JButton("➕ New Chat");
        newChatBtn.setBackground(new Color(46, 204, 113));
        newChatBtn.setFont(font.deriveFont(Font.BOLD));
        newChatBtn.setForeground(Color.WHITE);
        newChatBtn.setFocusPainted(false);
        newChatBtn.setBorder(new EmptyBorder(10, 20, 10, 20));

        newChatBtn.addActionListener(e -> {
            String title = JOptionPane.showInputDialog(this, "Enter conversation title:");
            if (title != null && !title.trim().isEmpty()) {
                int newCid = -1;
                try {
                    newCid = DBManager.createConversation(userId, title);
                } catch (Exception ignored) {}
                if (newCid != -1) {
                    conversationListModel.addElement(newCid + " - " + title);
                }
            }
        });

        JButton renameChatBtn = new JButton("✏️ Rename Chat");
        renameChatBtn.setBackground(new Color(52, 152, 219));
        renameChatBtn.setFont(font.deriveFont(Font.BOLD));
        renameChatBtn.setForeground(Color.WHITE);
        renameChatBtn.setFocusPainted(false);
        renameChatBtn.setBorder(new EmptyBorder(10, 20, 10, 20));

        renameChatBtn.addActionListener(e -> {
            String selected = conversationList.getSelectedValue();
            if (selected == null) return;
            int cid = Integer.parseInt(selected.split(" - ")[0]);

            String newTitle = JOptionPane.showInputDialog(this, "Enter new title:");
            if (newTitle != null && !newTitle.trim().isEmpty()) {
                try {
                    DBManager.renameConversation(cid, newTitle);
                    loadConversations();
                } catch (SQLException ex) {
                    System.err.println("Rename failed: " + ex.getMessage());
                }
            }
        });

        JButton deleteChatBtn = new JButton("🗑️ Delete Chat");
        deleteChatBtn.setBackground(new Color(231, 76, 60));
        deleteChatBtn.setFont(font.deriveFont(Font.BOLD));
        deleteChatBtn.setForeground(Color.WHITE);
        deleteChatBtn.setFocusPainted(false);
        deleteChatBtn.setBorder(new EmptyBorder(10, 20, 10, 20));

        deleteChatBtn.addActionListener(e -> {
            String selected = conversationList.getSelectedValue();
            if (selected == null) return;
            int cid = Integer.parseInt(selected.split(" - ")[0]);

            int confirm = JOptionPane.showConfirmDialog(this, "Delete this conversation?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    DBManager.deleteConversation(cid);
                    currentCid = -1;
                    chatContent.setLength(0);
                    chatArea.setText("");
                    loadConversations();
                } catch (SQLException ex) {
                    System.err.println("Delete failed: " + ex.getMessage());
                }
            }
        });

        JPanel topSidebarPanel = new JPanel(new GridLayout(3, 1));
        topSidebarPanel.setOpaque(false);
        topSidebarPanel.add(newChatBtn);
        topSidebarPanel.add(renameChatBtn);
        topSidebarPanel.add(deleteChatBtn);
        sidebar.add(topSidebarPanel, BorderLayout.NORTH);

        conversationListModel = new DefaultListModel<>();
        conversationList = new JList<>(conversationListModel);
        conversationList.setFont(font);
        conversationList.setBackground(new Color(250, 250, 250));
        conversationList.setSelectionBackground(new Color(200, 230, 201));
        conversationList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = conversationList.getSelectedValue();
                if (selected != null) {
                    currentCid = Integer.parseInt(selected.split(" - ")[0]);
                    loadPrompts(currentCid);
                }
            }
        });

        sidebar.add(new JScrollPane(conversationList), BorderLayout.CENTER);
        
        // --- Logout Button at Bottom ---
        JButton logoutBtn = new JButton("🚪 Logout");
        logoutBtn.setBackground(new Color(127, 140, 141));
        logoutBtn.setFont(font.deriveFont(Font.BOLD));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorder(new EmptyBorder(10, 20, 10, 20));
        logoutBtn.addActionListener(e -> {
            dispose(); // Close ChatApp
            new Login(); // Relaunch login screen
        });

        sidebar.add(logoutBtn, BorderLayout.SOUTH);


        // --- Chat Area ---
        JPanel chatPanel = new JPanel(new BorderLayout());

        chatArea = new JTextPane();
        chatArea.setContentType("text/html");
        chatArea.setEditable(false);
        chatArea.setFont(font);
        chatArea.setBackground(Color.WHITE);
        chatArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setBorder(null);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        // --- Input Area ---
        JPanel inputPanel = new RoundedPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        inputPanel.setBackground(new Color(240, 240, 240));

        inputField = new JTextField("Enter Prompt");
        inputField.setFont(font);
        inputField.setForeground(Color.GRAY);
        inputField.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputField.setBackground(Color.WHITE);

        // Placeholder behavior
        inputField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (inputField.getText().equals("Enter Prompt")) {
                    inputField.setText("");
                    inputField.setForeground(Color.BLACK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (inputField.getText().isEmpty()) {
                    inputField.setText("Enter Prompt");
                    inputField.setForeground(Color.GRAY);
                }
            }
        });

        JButton submitButton = new JButton("Submit");
        submitButton.setBackground(new Color(46, 204, 113));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(font.deriveFont(Font.BOLD));
        submitButton.setFocusPainted(false);
        submitButton.setBorder(new EmptyBorder(10, 20, 10, 20));

        submitButton.addActionListener(e -> {
            if (currentCid == -1) return;
            String question = inputField.getText().trim();
            if (question.isEmpty() || question.equals("Enter Prompt")) return;

            appendToChat("User", question, "#8e44ad");
            appendToChat("Bot", "Generating response...", "#888888");
            inputField.setText("");

            new Thread(() -> {
                String answer = ai.prompt(question);
                try {
                    DBManager.insertPrompt(currentCid, question, answer);
                } catch (SQLException ex) {
                    System.err.println("Insert failed: " + ex.getMessage());
                }
                SwingUtilities.invokeLater(() -> loadPrompts(currentCid));
            }).start();
        });

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // --- Frame Layout ---
        add(sidebar, BorderLayout.WEST);
        add(chatPanel, BorderLayout.CENTER);
    }

    private void appendToChat(String sender, String message, String color) {
        chatContent.append("<div style='margin:10px 0;'><b style='color:")
                .append(color).append(";'>")
                .append(sender).append(":</b> ")
                .append(message).append("</div>");
        chatArea.setText(chatContent.toString() + "</body></html>");
    }

    private void loadConversations() {
        try (ResultSet rs = DBManager.getAllConversationsForUser(userId)) {
            conversationListModel.clear();
            int count = 1;
            while (rs.next()) {
                int cid = rs.getInt("cid");
                String title = rs.getString("title");
                if (title == null || title.isEmpty()) title = "Untitled";
                conversationListModel.addElement(cid + " - #" + count + " " + title);
                count++;
            }
        } catch (SQLException e) {
            System.err.println("Load convos failed: " + e.getMessage());
        }
    }

    private void loadPrompts(int cid) {
        chatContent.setLength(0);
        chatContent.append("<html><body style='font-family:Segoe UI;'>");
        chatArea.setText(chatContent.toString() + "</body></html>");

        try (ResultSet rs = DBManager.getPromptsByConversation(cid)) {
            while (rs.next()) {
                appendToChat("User", rs.getString("question"), "#8e44ad");
                appendToChat("Bot", rs.getString("answer"), "#2ecc71");
            }
        } catch (SQLException e) {
            System.err.println("Load prompts failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Login(); // shows login window first
        });
    }

    static class RoundedPanel extends JPanel {
        private final int arc = 16;

        public RoundedPanel() {
            super();
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            super.paintComponent(g);
        }
    }
}

