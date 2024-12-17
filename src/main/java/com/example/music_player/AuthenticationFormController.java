package com.example.music_player;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.sql.*;

public class AuthenticationFormController {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/mymusic";  // MySQL Database URL
    private static final String DB_USER = "root";  // MySQL username (default: root)
    private static final String DB_PASSWORD = "";  // MySQL password (default: empty)

    @FXML
    private AnchorPane LogIn_AnchorPane;
    @FXML
    private Hyperlink LogIn_createNewAccHyperLink;
    @FXML
    private Hyperlink LogIn_forgotPasswordHyperLink;
    @FXML
    private Button LogIn_logInBtn;
    @FXML
    private PasswordField LogIn_password;
    @FXML
    private TextField LogIn_username;
    @FXML
    public Label LogIn_invalidUsernameOrPassLabel;
    @FXML
    private PasswordField SignUp_confirmPassword;
    @FXML
    private Hyperlink SignUp_logInToYourAccHyperLink;
    @FXML
    private PasswordField SignUp_password;
    @FXML
    private Label SignUp_passwordsDoNotMatchLabel;
    @FXML
    private Button SignUp_signUpBtn;
    @FXML
    private TextField SignUp_username;
    @FXML
    private AnchorPane SignUp_AnchorPane;
    @FXML
    private AnchorPane UpdatePass_AnchorPane;
    @FXML
    private PasswordField UpdatePass_confirmPassword;
    @FXML
    private Hyperlink UpdatePass_createNewAccHyperLink;
    @FXML
    private Label UpdatePass_invalidUsernameLabel;
    @FXML
    private Hyperlink UpdatePass_logInToYourAccHyperLink;
    @FXML
    private PasswordField UpdatePass_password;
    @FXML
    private Label UpdatePass_passwordsDoNotMatchLabel;
    @FXML
    private Button UpdatePass_searchAccBtn;
    @FXML
    private Button UpdatePass_updatePasswordBtn;
    @FXML
    private TextField UpdatePass_username;

    // Database connection utility method
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    @FXML
    void LogIn_createNewAccHyperLinkOnClick(ActionEvent event) {
        LogIn_AnchorPane.setVisible(false);
        SignUp_AnchorPane.setVisible(true);
        clearLogInFields();
    }

    @FXML
    void LogIn_forgotPasswordHyperLinkOnClick(ActionEvent event) {
        LogIn_AnchorPane.setVisible(false);
        UpdatePass_AnchorPane.setVisible(true);
        clearLogInFields();
    }

    @FXML
    void LogIn_logInBtnOnClick(ActionEvent event) throws IOException {
        String username = LogIn_username.getText().trim();
        String password = LogIn_password.getText().trim();

        if (isValidInput(username, password)) {
            try (Connection conn = getConnection()) {
                String query = "SELECT * FROM authentication WHERE username = ? AND password = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        showAlert(Alert.AlertType.INFORMATION, "LogIn Successful", "Welcome " + username + "!");
                        clearLogInFields();
                        App.getInstance().loadMusicPlayerScene();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Log In Failed", "Invalid username or password!");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while connecting to the database.");
            }
        }
    }

    @FXML
    void SignUp_logInToYourAccHyperLink(ActionEvent event) {
        SignUp_AnchorPane.setVisible(false);
        LogIn_AnchorPane.setVisible(true);
        clearSignUpFields();
    }

    @FXML
    void SignUp_signUpBtnOnClick(ActionEvent event) {
        String username = SignUp_username.getText().trim();
        String password = SignUp_password.getText().trim();
        String confirmPassword = SignUp_confirmPassword.getText().trim();

        if (!password.equals(confirmPassword)) {
            SignUp_passwordsDoNotMatchLabel.setVisible(true);
            return;
        }

        SignUp_passwordsDoNotMatchLabel.setVisible(false);

        // Check if username already exists in the database
        try (Connection conn = getConnection()) {
            String query = "SELECT * FROM authentication WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Username is already taken!");
                } else {
                    String insertQuery = "INSERT INTO authentication (username, password) VALUES (?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, username);
                        insertStmt.setString(2, password);
                        insertStmt.executeUpdate();
                        showAlert(Alert.AlertType.INFORMATION, "Sign Up Successful", "Your account has been created!");
                        clearSignUpFields();
                        SignUp_AnchorPane.setVisible(false);
                        LogIn_AnchorPane.setVisible(true);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while connecting to the database.");
        }
    }

    @FXML
    void UpdatePass_createNewAccHyperLink(ActionEvent event) {
        UpdatePass_AnchorPane.setVisible(false);
        SignUp_AnchorPane.setVisible(true);
        clearUpdatePassFields();
    }

    @FXML
    void UpdatePass_logInToYourAccHyperLinkOnClick(ActionEvent event) {
        UpdatePass_AnchorPane.setVisible(false);
        LogIn_AnchorPane.setVisible(true);
        clearUpdatePassFields();
    }

    @FXML
    void UpdatePass_searchAccBtnOnClick(ActionEvent event) {
        String username = UpdatePass_username.getText().trim();

        try (Connection conn = getConnection()) {
            String query = "SELECT * FROM authentication WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    UpdatePass_invalidUsernameLabel.setVisible(false);
                } else {
                    UpdatePass_invalidUsernameLabel.setVisible(true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void UpdatePass_updatePasswordBtnOnClick(ActionEvent event) {
        String username = UpdatePass_username.getText().trim();
        String newPassword = UpdatePass_password.getText().trim();
        String confirmPassword = UpdatePass_confirmPassword.getText().trim();

        if (!newPassword.equals(confirmPassword)) {
            UpdatePass_passwordsDoNotMatchLabel.setVisible(true);
            return;
        }

        if (!UpdatePass_invalidUsernameLabel.isVisible()) {
            try (Connection conn = getConnection()) {
                String updateQuery = "UPDATE authentication SET password = ? WHERE username = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                    stmt.setString(1, newPassword);
                    stmt.setString(2, username);
                    stmt.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Password Updated", "Your password has been successfully updated!");
                    clearUpdatePassFields();
                    UpdatePass_AnchorPane.setVisible(false);
                    LogIn_AnchorPane.setVisible(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while updating the password.");
            }
        }
    }

    // clearing input fields
    private void clearSignUpFields() {
        SignUp_username.clear();
        SignUp_password.clear();
        SignUp_confirmPassword.clear();
        SignUp_passwordsDoNotMatchLabel.setVisible(false);
    }

    private void clearLogInFields() {
        LogIn_username.clear();
        LogIn_password.clear();
        LogIn_invalidUsernameOrPassLabel.setVisible(false);
    }

    private void clearUpdatePassFields() {
        UpdatePass_username.clear();
        UpdatePass_password.clear();
        UpdatePass_confirmPassword.clear();
        UpdatePass_passwordsDoNotMatchLabel.setVisible(false);
    }

    // alert box
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // validate input
    private boolean isValidInput(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please fill out both fields.");
            return false;
        }
        return true;
    }
}
