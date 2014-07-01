package elegant;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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
    MenuItem openProject;

    private Stage stage;

    public Controller() {
    }

    public void initData(Stage stage) {
        this.stage = stage;
    }

    public void onOpenProjectAction(ActionEvent event) {
        openProject();
    }

    public void openProject() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Projects chooser");
        File defaultDirectory = new File("c:");
        chooser.setInitialDirectory(defaultDirectory);

        PathItem selectedDirectory = new PathItem(chooser.showDialog(stage).toPath());
        try {
            GitHelper.LoadGit(selectedDirectory.getPath().toAbsolutePath().toString());
        } catch (ElephantException ex) {
            failedOpenProject(ex);
            return;
        }
        TreeItem<PathItem> root = PathTreeItem.createNode(selectedDirectory);
        fileSystemTree.setRoot(root);
        // remove ignored files and .git
        fileSystemTree.getRoot().getChildren().removeIf(
                path -> path.getValue().getPath().endsWith(".git")
        );
    }

    private void failedOpenProject(ElephantException ex) {

        List<Dialogs.CommandLink> links = Arrays.asList(
                new Dialogs.CommandLink(
                        "Another chance to make an elegant choice",
                        "Opens up the folder choser again."),
                new Dialogs.CommandLink(
                        "Admit failure",
                        "Cancel and go back to your work.")
        );
        Action answer = Dialogs.create()
                .title("Elegant error")
                .masthead("That wasn't an elegant choice")
                .message(ex.getMessage())
                .lightweight()
                .style(DialogStyle.UNDECORATED)
                .showCommandLinks(links.get(1), links);
        if (answer == links.get(0)) {
            openProject();
        }
    }
}
