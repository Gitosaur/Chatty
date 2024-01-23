import controllers.Chatty;
import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionFormCreator;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ui.ResizeHelper;

public class ChattyLauncher extends ExtensionFormCreator {

    public static void main(String[] args) {
        runExtensionForm(args, ChattyLauncher.class);
    }

    @Override
    protected ExtensionForm createForm(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/chatty-modern.fxml"));
        Parent root = loader.load();

        stage.setTitle("controllers.Chatty");
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
        stage.getScene().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setResizable(true);
        stage.initStyle(StageStyle.TRANSPARENT);

        Image image = new Image(getClass().getResourceAsStream("/logo.png"));
        stage.getIcons().add(image);

        stage.show();
        Chatty controller = loader.getController();
        controller.setStage(stage);

        ResizeHelper.addResizeListener(stage);

        return controller;
    }

}
