package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class Controller extends VBox {
    @FXML
    TreeView<PathItem> fileSystemTree;

    @FXML
    MenuItem openProject;

    private File projectDir;
    private Stage stage;

    public Controller() {
        //FileSystem.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
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
        System.out.println(root);
        fileSystemTree.setRoot(root);
    }
}
