package elegant;

import elegant.exceptions.ElephantException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.dialog.Dialogs;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Controller extends VBox {
    @FXML
    TreeView<PathItem> fileSystemTree;

    @FXML
    Button openProject;

    private Stage stage;

    // The dialog of credentials will consist of two input fields (username and password),
    // and have two buttons: Login and Cancel.
    final TextField txUserName = new TextField();
    final PasswordField txPassword = new PasswordField();
    final Action actionLogin = new AbstractAction("Login") {
        {
            ButtonBar.setType(this, ButtonBar.ButtonType.OK_DONE);
        }

        @Override
        public void handle(ActionEvent e) {
            Dialog dlg = (Dialog) e.getSource();
            dlg.hide();
        }
    };

    public Controller() {
    }

    public void initData(Stage stage) {
        this.stage = stage;
    }

    /*
     * External UI-linked public methods
     */
    public void onOpenProjectAction(Event event) {
        openProject();
    }

    public void onSynchronizeAction(Event event) {
        synchronize();
    }

    public void onProjectCredentialsAction(Event event) {
        setProjectCredentials();
    }

    /*
     * Internal private methods
     */

    private void openProject() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Projects chooser");
        File defaultDirectory = new File("c:");
        chooser.setInitialDirectory(defaultDirectory);

        File selectedDirectoryFile = chooser.showDialog(stage);
        // in case of cancel
        if (selectedDirectoryFile == null) return ;
        try {
            GitHelper.loadGit(selectedDirectoryFile);
        } catch (ElephantException ex) {
            failedOpenProject(ex.getMessage());
            return;
        }
        stage.setTitle(GitHelper.getPath().toString() + " : [" + GitHelper.getPath().toAbsolutePath() + "] - " + Elegant.getTitle());
    }

    private void synchronize() {
        try {
            GitHelper.synchronize(txUserName.getText(), txPassword.getText());
        } catch (ElephantException ex) {
            failedSynchronize(ex.getMessage());
            return;
        }
    }

    private void loadProjectToPathTree() {
        TreeItem<PathItem> root = PathTreeItem.createNode(new PathItem(GitHelper.getPath()));
        fileSystemTree.setRoot(root);
        // remove ignored files and .git
        fileSystemTree.getRoot().getChildren().removeIf(
                path -> path.getValue().getPath().endsWith(".git")
        );
    }

    private void failedSynchronize(String message) {
        Action answer = Dialogs.create()
                .masthead("Inelegant synchronize")
                .message(message)
                .style(DialogStyle.UNDECORATED)
                .lightweight()
                .showError();
    }

    private void failedOpenProject(String message) {

        List<Dialogs.CommandLink> links = Arrays.asList(
                new Dialogs.CommandLink(
                        "Another chance to make an elegant choice",
                        "Opens up the directory chooser again."),
                new Dialogs.CommandLink(
                        "Admit failure",
                        "Cancel and go back to your work.")
        );
        Action answer = Dialogs.create()
                .title("Elegant error")
                .masthead("That wasn't an elegant choice")
                .message(message)
                .lightweight()
                .style(DialogStyle.UNDECORATED)
                .showCommandLinks(links.get(1), links);
        if (answer == links.get(0)) {
            openProject();
        }
    }

    // This method is called when the user types into the username / password fields
    private void validate() {
        actionLogin.disabledProperty().set(
                txUserName.getText().trim().isEmpty() || txPassword.getText().trim().isEmpty());
    }

    // Imagine that this method is called somewhere in your codebase
    private void setProjectCredentials() {
        Dialog dlg = new Dialog(null, "Login Dialog");

        // listen to user input on dialog (to enable / disable the login button)
        ChangeListener<String> changeListener = new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                validate();
            }
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
        dlg.getActions().addAll(actionLogin, Dialog.Actions.CANCEL);
        validate();

        // request focus on the username field by default (so the user can
        // type immediately without having to click first)
        Platform.runLater(
                new Runnable() {
                    public void run() {
                        txUserName.requestFocus();
                    }
                });

        dlg.show();
    }
}
