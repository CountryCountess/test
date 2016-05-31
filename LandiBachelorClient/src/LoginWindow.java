import javax.swing.*;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginWindow {
    private JTextField textField1;
    private JButton registerButton;
    private JTextField textField2;
    private JButton backButton;
    private JPanel panel;
    private JButton authenticateButton;

    public JTextField getTextField1() {
        return textField1;
    }

    public JButton getAuthenticateButton() {
        return authenticateButton;
    }

    public JButton getRegisterButton() {
        return registerButton;
    }

    public JTextField getTextField2() {
        return textField2;
    }

    public JButton getBackButton() {
        return backButton;
    }

    public JPanel getPanel() {
        return panel;
    }
}
