package elegant;

import elegant.data.Configuration;
import elegant.exceptions.CredentialsException;
import elegant.exceptions.ElephantException;
import elegant.utils.Encrypt;
import elegant.utils.PasswordHash;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.dialog.Dialogs;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;


/**
 * Created by regnarock on 24/07/2014.
 */
public class Credentials {

    private Object owner;

    private Dialogs.UserInfo userInfo;
    private boolean isLogged;

    public Credentials(Object owner) throws CredentialsException {
        this.owner = owner;
        Platform.runLater(
                () -> {
                    String userName = "", password = "";
                    if (Configuration.isSecured() && !isLogged) {
                        // use non-stored and non-reversible password to encrypt data
                        String pwd = requestMasterPassword();
                        userName = Encrypt.createDecrypt(Configuration.getUserName(), pwd);
                        password = Encrypt.createDecrypt(Configuration.getPassword(), pwd);
                    } else {
                        userName = Configuration.getUserName();
                        password = Configuration.getPassword();
                    }
                    userInfo = new Dialogs.UserInfo(userName, password);
                });
    }

    public Dialogs.UserInfo getUserInfo() throws CredentialsException {
        if (Configuration.isSecured() && !isLogged) {
            requestMasterPassword();
        }
        return userInfo;
    }

    public void saveCredentials() throws CredentialsException {
        String clearPwd = "";
        if (Configuration.isSecured() && !isLogged)
            clearPwd = requestMasterPassword();
        // The dialog of credentials will consist of two input fields (username and password),
        // and have two buttons: Login and Cancel.
        Dialog dlg = new Dialog(owner, "Save remote credentials");
        final TextField txUserName = new TextField(userInfo.getUserName());
        final PasswordField txPassword = new PasswordField();
        txPassword.setText(userInfo.getPassword());
        final CheckBox checkSaveCredentials = new CheckBox("Auto Login");
        checkSaveCredentials.setSelected(Configuration.isSaved());
        final Action actionLogin = new AbstractAction("Save") {
            {
                ButtonBar.setType(this, ButtonBar.ButtonType.OK_DONE);
            }

            @Override
            public void handle(ActionEvent e) {
                Dialog dlg = (Dialog) e.getSource();
                dlg.hide();
            }
        };
        // this lambda is called when the user types into the username / password fields
        Runnable validate = () -> {
            actionLogin.disabledProperty().set(
                    txUserName.getText().trim().isEmpty() || txPassword.getText().trim().isEmpty()
            );
            checkSaveCredentials.disableProperty().set(
                    txUserName.getText().trim().isEmpty() || txPassword.getText().trim().isEmpty()
            );
        };
        // listen to user input on dialog (to enable / disable the login button and save box)
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            validate.run();
        };
        txUserName.textProperty().addListener(changeListener);
        txPassword.textProperty().addListener(changeListener);

        // layout a custom GridPane containing the input fields and labels
        final GridPane content = new GridPane();
        content.setHgap(10);
        content.setVgap(10);

        content.add(new Label("User name"), 0, 0);
        content.add(txUserName, 1, 0);
        GridPane.setHgrow(txUserName, Priority.ALWAYS);
        content.add(new Label("Password"), 0, 1);
        content.add(txPassword, 1, 1);
        GridPane.setHgrow(txPassword, Priority.ALWAYS);
        content.add(checkSaveCredentials, 1, 2);
        GridPane.setHgrow(checkSaveCredentials, Priority.ALWAYS);

        // create the dialog with a custom graphic and the gridpane above as the
        // main content region
        dlg.setResizable(false);
        dlg.setIconifiable(false);
        dlg.setContent(content);
        dlg.getActions().addAll(actionLogin, Dialog.Actions.CANCEL);
        validate.run();

        // request focus on the username field by default (so the user can
        // type immediately without having to click first)
        Platform.runLater(
                () -> {
                    txUserName.requestFocus();
                });
        if (dlg.show() == actionLogin) {
            if (checkSaveCredentials.isSelected()) {
                Configuration.setIsSaved(true);
                if (!Configuration.isSecured() && !isLogged) {
                    clearPwd = askCreateMasterPassword();
                }
                System.out.println("print new");
                if (Configuration.isSecured()) {
                    // use non-stored and non-reversible password to encrypt data
                    Configuration.setUserName(Encrypt.createEncryption(txUserName.getText(), clearPwd));
                    Configuration.setPassword(Encrypt.createEncryption(txPassword.getText(), clearPwd));
                } else {
                    Configuration.setUserName(txUserName.getText());
                    Configuration.setPassword(txPassword.getText());
                }
            }
            userInfo.setUserName(txUserName.getText());
            userInfo.setPassword(txPassword.getText());
        }
    }

    private String askCreateMasterPassword() {
        Action answer = Dialogs.create()
                .title("Master password")
                .masthead("No master password was found")
                .message("Do you want to secure your credentials and create one ?")
                .lightweight()
                .style(DialogStyle.UNDECORATED)
                .showConfirm();
        String clearPwd = "";
        if (answer == Dialog.Actions.YES) {
            clearPwd = createMasterPassword();
        } else {
            showUnsecuredCredentialsWarning();
        }
        return clearPwd;
    }

    private String createMasterPassword() {
        Dialog dlg = new Dialog(owner, "Master password");

        final PasswordField txPwd1 = new PasswordField();
        final PasswordField txPwd2 = new PasswordField();
        final Text txError = new Text("Passwords must be equal");
        txError.setVisible(false);
        txError.setFill(Color.RED);
        final Action actionSave = new AbstractAction("Save") {
            {
                ButtonBar.setType(this, ButtonBar.ButtonType.OK_DONE);
            }

            @Override
            public void handle(ActionEvent e) {
                Dialog dlg = (Dialog) e.getSource();
                dlg.hide();
            }
        };
        // Update save button and error text  when user enters character in text fields
        Runnable validate = () -> {
            boolean pwdDifferent = txPwd1.getText().trim().compareTo(txPwd2.getText().trim()) != 0;
            actionSave.disabledProperty().set(
                    txPwd1.getText().trim().isEmpty() ||
                            txPwd2.getText().trim().isEmpty() ||
                            pwdDifferent
            );
            txError.setVisible(
                    !txPwd2.getText().trim().isEmpty() &&
                            pwdDifferent
            );
        };
        txPwd1.textProperty().addListener(e -> validate.run());
        txPwd2.textProperty().addListener(e -> validate.run());

        // layout a custom GridPane containing the input fields and labels
        final GridPane content = new GridPane();
        content.setHgap(10);
        content.setVgap(10);

        content.add(new Label("Enter password"), 0, 0);
        content.add(txPwd1, 1, 0);
        GridPane.setHgrow(txPwd1, Priority.ALWAYS);
        content.add(new Label("Confirm password"), 0, 1);
        content.add(txPwd2, 1, 1);
        GridPane.setHgrow(txPwd2, Priority.ALWAYS);
        content.add(txError, 1, 2);
        GridPane.setHgrow(txError, Priority.ALWAYS);

        dlg.setResizable(false);
        dlg.setResizable(false);
        dlg.setIconifiable(false);
        dlg.setContent(content);
        dlg.getActions().addAll(actionSave, Dialog.Actions.CANCEL);
        // request focus on the username field by default (so the user can
        // type immediately without having to click first)
        Platform.runLater(
                () -> {
                    txPwd1.requestFocus();
                });
        Action response = dlg.show();
        if (response == Dialog.Actions.CANCEL)
            showUnsecuredCredentialsWarning();
        else {
            try {
                Configuration.setMasterPasswordHash(PasswordHash.createHash(txPwd1.getText().trim()));
            } catch (NoSuchAlgorithmException ex) {
                throw new ElephantException("Could not create master password : " + ex.getMessage());
            } catch (InvalidKeySpecException ex) {
                throw new ElephantException("Could not create master password : " + ex.getMessage());
            }
            Configuration.setIsSecured(true);
            showCredentialsSecured();
        }
        return txPwd1.getText();
    }

    /**
     * Request master password to user until right or cancelled
     *
     * @return A String of what was entered by the user
     */
    private String requestMasterPassword() throws CredentialsException {
        Dialog dlg = new Dialog(owner, "Master password required");
        final PasswordField pwd = new PasswordField();

        final GridPane content = new GridPane();
        content.setHgap(10);
        content.setVgap(10);
        content.add(new Label("Enter password"), 0, 0);
        content.add(pwd, 1, 0);
        GridPane.setHgrow(pwd, Priority.ALWAYS);

        dlg.getActions().addAll(Dialog.Actions.OK, Dialog.Actions.CANCEL);
        dlg.setContent(content);
        dlg.setResizable(false);
        Platform.runLater(
                () -> {
                    pwd.requestFocus();
                });
        Action response = dlg.show();
        if (response == Dialog.Actions.CANCEL)
            throw new CredentialsException("Access denied.");
        try {
            isLogged = PasswordHash.validatePassword(pwd.getText(), Configuration.getMasterPasswordHash());
        } catch (NoSuchAlgorithmException ex) {
            throw new ElephantException("Could not validate master password : " + ex.getMessage());
        } catch (InvalidKeySpecException ex) {
            throw new ElephantException("Could not validate master password : " + ex.getMessage());
        }
        if (!isLogged)
            requestMasterPassword();
        return pwd.getText();
    }

    private void showCredentialsSecured() {
        Dialogs.create()
                .lightweight()
                .style(DialogStyle.UNDECORATED)
                .message("Your credential are now secured ! :-)")
                .showInformation();
    }

    private void showUnsecuredCredentialsWarning() {
        Dialogs.create()
                .lightweight()
                .style(DialogStyle.UNDECORATED)
                .message("Your credentials won't be secured.")
                .showWarning();
    }
}
