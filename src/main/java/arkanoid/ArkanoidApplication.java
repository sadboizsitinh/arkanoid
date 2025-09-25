package arkanoid;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;

public class ArkanoidApplication extends Application {
    private GameManager game;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        game = new GameManager(gc);

        Scene scene = new Scene(new StackPane(canvas));
        scene.setOnKeyPressed(e -> game.onKeyPressed(e.getCode()));
        scene.setOnKeyReleased(e -> game.onKeyReleased(e.getCode()));

        stage.setTitle("Arkanoid - Stage 1");
        stage.setScene(scene);
        stage.show();

        // Game loop
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                game.update();
                game.render();
            }
        }.start();
    }
}
