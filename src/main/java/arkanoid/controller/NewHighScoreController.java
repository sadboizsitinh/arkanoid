
package arkanoid.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class NewHighScoreController {
    @FXML private TextField txtName;
    @FXML private Button btnSave, btnSkip;

    private int score;
    public void setScore(int score) { this.score = score; }

    @FXML
    private void initialize() {
        // TODO: handle save/skip
    }
}
