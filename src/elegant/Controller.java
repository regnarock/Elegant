package elegant;

import elegant.exceptions.CredentialsException;
import elegant.exceptions.ElephantException;
import elegant.utils.PathItem;
import elegant.utils.PathTreeItem;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
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
    private Credentials credentialsController;

    public Controller() {
    }

    public void initData(Stage stage) {
        this.stage = stage;
        credentialsController = new Credentials(stage);
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
        try {
            credentialsController.saveCredentials();
        } catch (CredentialsException e) {
            failedSetCredentials(e.getMessage());
        }
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
            GitHelper.synchronize(
                    credentialsController.getUserInfo().getUserName(),
                    credentialsController.getUserInfo().getPassword());
        } catch (ElephantException ex) {
            failedSynchronize(ex.getMessage());
            return;
        } catch (CredentialsException ex) {
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

    private  void failedSetCredentials(String message) {
        Action answer = Dialogs.create()
                .masthead("Credentials issue.")
                .message(message)
                .style(DialogStyle.UNDECORATED)
                .lightweight()
                .showError();
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
                        "Opens up the directory chooser again."
                ),
                new Dialogs.CommandLink(
                        "Admit failure",
                        "Cancel and go back to your work."
                )
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

}
