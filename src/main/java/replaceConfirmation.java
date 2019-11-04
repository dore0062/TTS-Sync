import javax.swing.*;
import java.awt.event.*;

public class replaceConfirmation extends JDialog {
    private JPanel contentPane;
    private JButton projectFileButton;
    private JButton cancelButton;
    private JLabel object;

    private replaceConfirmation() {
        setContentPane(contentPane);
        setModal(true);
        object.setText("The object '" + createFile.filename + "' already exists. Would you like to replace it with the script loaded from Tabletop Simulator?");
        setTitle("Replace file?");
        setLocationRelativeTo(null);
        setResizable(false);
        getRootPane().setDefaultButton(projectFileButton);

        projectFileButton.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        createFile.replace = true;
        dispose();
    }

    private void onCancel() {
        // Do nothing
        dispose();
    }

    public static void main(String[] args) {
        replaceConfirmation dialog = new replaceConfirmation();
        dialog.pack();
        dialog.setVisible(true);
    }
}