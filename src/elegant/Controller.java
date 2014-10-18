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

import elegant.exceptions.CredentialsException;
import elegant.exceptions.ElephantException;
import elegant.utils.PathItem;
import elegant.utils.PathTreeItem;
import elegant.utils.ProgressUpdater;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.java.Log;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogAction;
import org.controlsfx.dialog.Dialogs;

import java.io.File;

@Log
public class Controller extends VBox
{
    @FXML
    TreeView<PathItem> workingDirTree;
    @FXML
    TreeView<PathItem> stageDirTree;
    @FXML
    TreeView<PathItem> indexDirTree;
    @FXML
    ProgressIndicator  synchronizeIndicator;
    @FXML
    ProgressIndicator  updateIndicator;
    @FXML
    ProgressIndicator  createBranchIndicator;
    @FXML
    ProgressIndicator  terminateBranchIndicator;
    @FXML
    Button             openProject;

    private Stage       stage;
    private Credentials credentialsController;

    private GitWrapper git;

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

    private void synchronize() {
        log.info("synchronize");
        synchronizeIndicator.setProgress(-1);
        try {
            git.synchronize(
                credentialsController.getUserInfo().name(), credentialsController.getUserInfo().password(),
                new ProgressUpdater(synchronizeIndicator)
            );
        } catch (CredentialsException | ElephantException ex) {
            failedSynchronize(ex.getMessage());
            return;
        } finally {
            synchronizeIndicator.setProgress(1);
        }
    }

    private void failedSynchronize(String message) {
        log.info("FailedSynchronize: " + message);
        Dialogs.create()
               .masthead("Inelegant synchronize")
               .message(message)
               .styleClass(Dialog.STYLE_CLASS_UNDECORATED)
               .lightweight()
               .showError();
    }

    public void onProjectCredentialsAction(Event event) {
        try {
            credentialsController.saveCredentials();
        } catch (CredentialsException e) {
            failedSetCredentials(e.getMessage());
        }
    }

    private void failedSetCredentials(String message) {
        Dialogs.create()
               .masthead("Credentials issue.")
               .message(message)
               .styleClass(Dialog.STYLE_CLASS_UNDECORATED)
               .lightweight()
               .showError();
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
        if (selectedDirectoryFile == null) return;
        try {
            git = new GitWrapper(selectedDirectoryFile);
        } catch (ElephantException ex) {
            failedOpenProject(ex.getMessage());
            return;
        }
        stage.setTitle(
            git.getPath().toString() + " : [" + git.getPath().toAbsolutePath() + "] - " + Elegant.getTitle()
        );
        loadProjectToPathTree();
    }

    private void loadProjectToPathTree() {
        TreeItem<PathItem> root = PathTreeItem.createNode(new PathItem(git.getPath()));
        workingDirTree.setRoot(root);
        // remove ignored files and .git
        workingDirTree.getRoot().getChildren().removeIf(
            path -> path.getValue().getPath().endsWith(".git")
        );
        root = PathTreeItem.createNode(new PathItem(git.getPath()));
        stageDirTree.setRoot(root);
        // remove ignored files and .git
        stageDirTree.getRoot().getChildren().removeIf(
            path -> path.getValue().getPath().endsWith(".git")
        );
        root = PathTreeItem.createNode(new PathItem(git.getPath()));
        indexDirTree.setRoot(root);
        // remove ignored files and .git
        indexDirTree.getRoot().getChildren().removeIf(
            path -> path.getValue().getPath().endsWith(".git")
        );
    }

    private void failedOpenProject(String message) {
        DialogAction choice1 = new DialogAction("Another chance to make an elegant choice");
        choice1.setLongText("Opens up the directory chooser again.");
        DialogAction choice2 = new DialogAction("Admit failure");
        choice2.setLongText("Cancel and go back to your work.");
        Action answer = Dialogs.create().title("Elegant error").masthead("That wasn't an elegant choice").message(
            message
        ).lightweight().styleClass(Dialog.STYLE_CLASS_UNDECORATED).showCommandLinks(choice1, choice2);
        if (answer == choice1) {
            openProject();
        }
    }
}
