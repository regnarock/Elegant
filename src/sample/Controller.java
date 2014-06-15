package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.dialog.Dialogs;

import java.io.File;

public class Controller extends VBox {
    @FXML
    TreeView<PathItem> fileSystemTree;

    @FXML
    MenuItem openProject;

    private File projectDir;
    private Stage stage;

    public Controller() {
    }

    public void initData(Stage stage) {
        this.stage = stage;
    }

    public void openProject(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Projects chooser");
        File defaultDirectory = new File("c:");
        chooser.setInitialDirectory(defaultDirectory);

        PathItem selectedDirectory = new PathItem(chooser.showDialog(stage).toPath());
        TreeItem<PathItem> root = PathTreeItem.createNode(selectedDirectory);
        try {
            GitHelper.LoadGit(selectedDirectory.getPath().toAbsolutePath().toString());
        } catch (ElephantException ex) {
            failedOpenProject(ex);
            return;
        }
        fileSystemTree.setRoot(root);
        filterFileSystemTree();
    }

    private void failedOpenProject(ElephantException ex) {
        Dialogs.create()
                .title("Elegant error")
                .masthead("That wasn't an elegant choice")
                .message(ex.getMessage())
                .lightweight()
                .style(DialogStyle.UNDECORATED)
                .showError();
        return;
    }

    private void filterFileSystemTree() {
        fileSystemTree.getRoot().getChildren().removeIf(
                p -> p.getValue().getPath().endsWith(".git")
        );
    }
}
