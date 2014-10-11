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
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.java.Log;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.dialog.Dialogs;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Log
public class Controller extends VBox {
    @FXML
    TreeView<PathItem> workingDirTree;
    @FXML
    TreeView<PathItem> stageDirTree;
    @FXML
    TreeView<PathItem> indexDirTree;

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
        stage.setTitle(
            GitHelper.getPath().toString()
                + " : [" + GitHelper.getPath().toAbsolutePath() + "] - "
                + Elegant.getTitle()
        );
        loadProjectToPathTree();
    }

    private void synchronize() {
        log.info("synchronize");
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
        workingDirTree.setRoot(root);
        // remove ignored files and .git
        workingDirTree.getRoot().getChildren().removeIf(
            path -> path.getValue().getPath().endsWith(".git")
        );
        root = PathTreeItem.createNode(new PathItem(GitHelper.getPath()));
        stageDirTree.setRoot(root);
        // remove ignored files and .git
        stageDirTree.getRoot().getChildren().removeIf(
            path -> path.getValue().getPath().endsWith(".git")
        );
        root = PathTreeItem.createNode(new PathItem(GitHelper.getPath()));
        indexDirTree.setRoot(root);
        // remove ignored files and .git
        indexDirTree.getRoot().getChildren().removeIf(
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
