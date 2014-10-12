/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Thomas Wilgenbus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package elegant;

import elegant.data.Configuration;
import elegant.exceptions.CredentialsException;
import elegant.exceptions.ElephantException;
import elegant.utils.PasswordHash;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.apache.commons.codec.DecoderException;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.dialog.Dialogs;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

/**
 * Created by regnarock on 24/07/2014.
 */
public class Credentials
{
    private Object owner;

    private Dialogs.UserInfo userInfo;
    private boolean          isLogged;

    public Credentials(Object owner) {
        this.owner = owner;
    }

    public Dialogs.UserInfo getUserInfo() throws CredentialsException {
        if (!isLogged) {
            String userName = Configuration.getUserName(),
                password = Configuration.getPassword();
            // use non-stored and non-reversible password to encrypt data
            if (Configuration.isSaved() && !userName.isEmpty() && !password.isEmpty()) {
                String pwd = requestMasterPassword();
                try {
                    userName = PasswordHash.decrypt(userName, pwd);
                    password = PasswordHash.decrypt(password, pwd);
                    System.out.println("load " + userName + "/" + password);
                } catch (InvalidKeySpecException | UnsupportedEncodingException | BadPaddingException | DecoderException |
                    NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException |
                    IllegalBlockSizeException | NoSuchPaddingException e) {
                    throw new CredentialsException("Could not get credentials IDs : " + e.getMessage());
                }
            }
            userInfo = new Dialogs.UserInfo(userName, password);
        }
        return userInfo;
    }

    private void setUserInfo(String userName, String password) throws CredentialsException {
        String clearPwd = requestMasterPassword();
        String newUserName, newPassword;
        try {
            // use non-stored and non-reversible password to encrypt data
            newUserName = PasswordHash.encrypt(userName, clearPwd);
            newPassword = PasswordHash.encrypt(password, clearPwd);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException |
            IllegalBlockSizeException | UnsupportedEncodingException |
            InvalidParameterSpecException | InvalidKeySpecException e) {
            throw new CredentialsException("Could not set credentials IDs : " + e.getMessage());
        }
        userInfo = new Dialogs.UserInfo(userName, password);
        Configuration.setUserName(newUserName);
        Configuration.setPassword(newPassword);
    }

    public void saveCredentials() throws CredentialsException {
        boolean wasNotSaved = false;
        if (!Configuration.isSaved()) {
            askCreateMasterPassword();
            wasNotSaved = true;
        }
        // The dialog of credentials will consist of two input fields (username and password),
        // and have two buttons: Login and Cancel.
        Dialog dlg = new Dialog(owner, "Save remote credentials");
        final TextField txUserName = new TextField(wasNotSaved ? "" : getUserInfo().getUserName());
        final PasswordField txPassword = new PasswordField();
        txPassword.setText(wasNotSaved ? "" : getUserInfo().getPassword());
        final Action actionSave = new AbstractAction("Save")
        {
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
            actionSave.disabledProperty()
                      .set(txUserName.getText().trim().isEmpty() || txPassword.getText().trim().isEmpty()
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

        // create the dialog with a custom graphic and the gridpane above as the
        // main content region
        dlg.setResizable(false);
        dlg.setIconifiable(false);
        dlg.setContent(content);
        dlg.getActions().addAll(actionSave, Dialog.Actions.CANCEL);
        validate.run();

        // request focus on the username field by default (so the user can
        // type immediately without having to click first)
        Platform.runLater(() -> {
                              txUserName.requestFocus();
                          }
        );
        if (dlg.show() == actionSave && (txUserName.getText()
                                                   .compareTo(getUserInfo().getUserName()) != 0 || txPassword.getText()
                                                                                                             .compareTo(
                                                                                                                 getUserInfo()
                                                                                                                     .getPassword()
                                                                                                             ) != 0)) {
            setUserInfo(txUserName.getText(), txPassword.getText());
        }
    }

    private String askCreateMasterPassword() throws CredentialsException {
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
            throw new CredentialsException("Master password is required to save credentials.");
        }
        return clearPwd;
    }

    private String createMasterPassword() throws CredentialsException {
        Dialog dlg = new Dialog(owner, "Master password");

        final PasswordField txPwd1 = new PasswordField();
        final PasswordField txPwd2 = new PasswordField();
        final Text txError = new Text("Passwords must be equal");
        txError.setVisible(false);
        txError.setFill(Color.RED);
        final Action actionSave = new AbstractAction("Save")
        {
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
            actionSave.disabledProperty().set(txPwd1.getText().trim().isEmpty() ||
                                                  txPwd2.getText().trim().isEmpty() ||
                                                  pwdDifferent
            );
            txError.setVisible(!txPwd2.getText().trim().isEmpty() && pwdDifferent
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
        dlg.getActions().addAll(Dialog.Actions.CANCEL, actionSave);
        // request focus on the username field by default (so the user can
        // type immediately without having to click first)
        Platform.runLater(() -> txPwd1.requestFocus());
        Action response = dlg.show();
        if (response == actionSave) {
            try {
                Configuration.setMasterPasswordHash(PasswordHash.createHash(txPwd1.getText().trim()));
            } catch (NoSuchAlgorithmException ex) {
                throw new ElephantException("Could not create master password : " + ex.getMessage());
            } catch (InvalidKeySpecException ex) {
                throw new ElephantException("Could not create master password : " + ex.getMessage());
            }
            Configuration.setIsSaved(true);
            showCredentialsSecured();
        } else throw new CredentialsException("Master password is required to save credentials.");
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
        Platform.runLater(() -> {
                              pwd.requestFocus();
                          }
        );
        Action response = dlg.show();
        if (response == Dialog.Actions.CANCEL) throw new CredentialsException("Access denied.");
        try {
            isLogged = PasswordHash.validatePassword(pwd.getText(), Configuration.getMasterPasswordHash());
        } catch (NoSuchAlgorithmException ex) {
            throw new ElephantException("Could not validate master password : " + ex.getMessage());
        } catch (InvalidKeySpecException ex) {
            throw new ElephantException("Could not validate master password : " + ex.getMessage());
        }
        if (!isLogged) requestMasterPassword();
        return pwd.getText();
    }

    private void showCredentialsSecured() {
        Dialogs.create()
               .lightweight()
               .style(DialogStyle.UNDECORATED)
               .message("Your credential are now secured ! :-)")
               .showInformation();
    }
}
