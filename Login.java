import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

public class Login extends JFrame {
    private final JTextField nameField = new PlaceholderField("Name");
    private final JPasswordField passField = new PlaceholderPassword("Password");

    private final JTextField emailField = new PlaceholderField("Email");
    private final JPasswordField confirmPassField = new PlaceholderPassword("Confirm Password");

    private final JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 10));
    private final JPanel buttonPanel = new JPanel();

    private final Font font = new Font("Segoe UI", Font.PLAIN, 13);
    private boolean isRegisterMode = false;

    public Login() {
        setTitle("IABot - Login / Register");
        setSize(400, 350);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        buildUI();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildUI() {
          getContentPane().setBackground(new Color(245, 245, 245));
          setLayout(new BorderLayout());

          // LOGO TOP
          JPanel logoPanel = new JPanel();
          logoPanel.setOpaque(false);
          logoPanel.setBorder(new EmptyBorder(20, 10, 10, 10));
          ImageIcon logo = new ImageIcon("logo.png");
          Image scaled = logo.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
          JLabel logoLabel = new JLabel(new ImageIcon(scaled));
          logoPanel.add(logoLabel);
          add(logoPanel, BorderLayout.NORTH);

          formPanel.setBorder(new EmptyBorder(10, 30, 0, 30));
          formPanel.setOpaque(false);

          nameField.setFont(font);
          passField.setFont(font);

          formPanel.add(nameField);
          formPanel.add(passField);

          JButton loginBtn = styledButton("Login");
          JButton registerBtn = styledButton("Register");

          loginBtn.addActionListener(e -> handleLogin());
          registerBtn.addActionListener(e -> toggleRegisterForm());

          buttonPanel.setOpaque(false);
          buttonPanel.add(loginBtn);
          buttonPanel.add(registerBtn);

          add(formPanel, BorderLayout.CENTER);
          add(buttonPanel, BorderLayout.SOUTH);
      }


    private void toggleRegisterForm() {
        if (!isRegisterMode) {
            formPanel.add(emailField);
            formPanel.add(confirmPassField);
            buttonPanel.removeAll();

            JButton registerBtn = styledButton("Register");
            JButton cancelBtn = styledButton("Cancel");

            registerBtn.addActionListener(e -> handleRegister());
            cancelBtn.addActionListener(e -> resetToLogin());

            buttonPanel.add(registerBtn);
            buttonPanel.add(cancelBtn);

            isRegisterMode = true;
        }
        revalidate(); repaint();
    }

    private void resetToLogin() {
        formPanel.remove(emailField);
        formPanel.remove(confirmPassField);
        clearFields();
        buttonPanel.removeAll();

        JButton loginBtn = styledButton("Login");
        JButton registerBtn = styledButton("Register");

        loginBtn.addActionListener(e -> handleLogin());
        registerBtn.addActionListener(e -> toggleRegisterForm());

        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);

        isRegisterMode = false;
        revalidate(); repaint();
    }

    private void handleLogin() {
        String name = nameField.getText().trim();
        String pass = String.valueOf(passField.getPassword()).trim();

        try {
            int userId = DBManager.authenticateUser(name, pass);
            if (userId != -1) {
                dispose();
                ChatApp app = new ChatApp(userId);
                app.setVisible(true);
            } else {
                showError("Invalid username or password.");
            }
        } catch (SQLException e) {
            showError("Login error: " + e.getMessage());
        }
    }

    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = String.valueOf(passField.getPassword()).trim();
        String confirm = String.valueOf(confirmPassField.getPassword()).trim();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        if (!pass.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        try {
            boolean success = DBManager.registerUser(name, email, pass);
            if (success) {
                showMessage("Account created. Please log in.");
                resetToLogin();
            } else {
                showError("Username already exists.");
            }
        } catch (SQLException e) {
            showError("Registration error: " + e.getMessage());
        }
    }

    private void clearFields() {
        nameField.setText("Name");
        nameField.setForeground(Color.GRAY);
        passField.setText("Password");
        passField.setForeground(Color.GRAY);
        passField.setEchoChar((char) 0);
        emailField.setText("Email");
        emailField.setForeground(Color.GRAY);
        confirmPassField.setText("Confirm Password");
        confirmPassField.setForeground(Color.GRAY);
        confirmPassField.setEchoChar((char) 0);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton styledButton(String label) {
        JButton btn = new JButton(label);
        btn.setBackground(new Color(46, 204, 113));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        return btn;
    }

    static class PlaceholderField extends JTextField {
        private final String placeholder;

        public PlaceholderField(String placeholder) {
            super(20);
            this.placeholder = placeholder;
            setUI(new RoundedTextFieldUI());
            setForeground(Color.GRAY);
            setText(placeholder);
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    if (getText().equals(placeholder)) {
                        setText("");
                        setForeground(Color.BLACK);
                    }
                }

                public void focusLost(FocusEvent e) {
                    if (getText().isEmpty()) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                    }
                }
            });
        }
    }

    static class PlaceholderPassword extends JPasswordField {
        private final String placeholder;

        public PlaceholderPassword(String placeholder) {
            super(20);
            this.placeholder = placeholder;
            setUI(new RoundedTextFieldUI());
            setForeground(Color.GRAY);
            setEchoChar((char) 0);
            setText(placeholder);
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    if (String.valueOf(getPassword()).equals(placeholder)) {
                        setText("");
                        setEchoChar('•');
                        setForeground(Color.BLACK);
                    }
                }

                public void focusLost(FocusEvent e) {
                    if (String.valueOf(getPassword()).isEmpty()) {
                        setEchoChar((char) 0);
                        setText(placeholder);
                        setForeground(Color.GRAY);
                    }
                }
            });
        }
    }

    static class RoundedTextFieldUI extends javax.swing.plaf.basic.BasicTextFieldUI {
        protected void paintSafely(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(230, 230, 230));
            g2.fillRoundRect(0, 0, getComponent().getWidth(), getComponent().getHeight(), 16, 16);
            g2.dispose();
            super.paintSafely(g);
        }

        protected void installDefaults() {
            super.installDefaults();
            getComponent().setOpaque(false);
            getComponent().setBorder(new EmptyBorder(8, 14, 8, 14));
        }
    }
}

