package arkanoid.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.Node;
import javafx.stage.Stage;

public class SelectSkinController {

    @FXML
    private StackPane skinContainer;

    private String selectedBall = null;
    private String selectedPaddle = null;

    @FXML
    private void showBallSkins() {
        HBox box = new HBox(20);
        box.setAlignment(javafx.geometry.Pos.CENTER);

        // Tạo các skin mẫu (sau này bạn thay đường dẫn ảnh thật vào)
        for (int i = 1; i <= 3; i++) {
            ImageView img = new ImageView(new Image(getClass().getResourceAsStream("/arkanoid/ui/assets/ball" + i + ".png")));
            img.setFitHeight(80);
            img.setFitWidth(80);
            int index = i;
            img.setOnMouseClicked(e -> {
                selectedBall = "ball" + index;
                System.out.println("Selected ball: " + selectedBall);
            });
            box.getChildren().add(img);
        }

        skinContainer.getChildren().setAll(box);
    }

    @FXML
    private void showPaddleSkins() {
        HBox box = new HBox(20);
        box.setAlignment(javafx.geometry.Pos.CENTER);

        // Tạo các skin mẫu (sau này bạn thay ảnh thật vào)
        for (int i = 1; i <= 3; i++) {
            ImageView img = new ImageView(new Image(getClass().getResourceAsStream("/arkanoid/ui/assets/paddle" + i + ".png")));
            img.setFitHeight(80);
            img.setFitWidth(160);
            int index = i;
            img.setOnMouseClicked(e -> {
                selectedPaddle = "paddle" + index;
                System.out.println("Selected paddle: " + selectedPaddle);
            });
            box.getChildren().add(img);
        }

        skinContainer.getChildren().setAll(box);
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            // Load lại Main menu
            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(getClass().getResource("/arkanoid/ui/fxml/Main.fxml")); // dùng đường dẫn tuyệt đối
            javafx.scene.Parent root = loader.load();

            // Giữ style giống các màn khác
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 800, 600);
            scene.getStylesheets().add(
                    getClass().getResource("/arkanoid/ui/css/style.css").toExternalForm()
            );

            // Đặt lại scene lên Stage hiện tại (không đóng Stage)
            javafx.stage.Stage stage =
                    (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (java.io.IOException ex) {
            ex.printStackTrace();
            System.err.println("Không thể quay về Main.fxml");
        }
    }


    public String getSelectedBall() { return selectedBall; }
    public String getSelectedPaddle() { return selectedPaddle; }
}
