package GameEngine.controller;

import javafx.stage.Stage;

public class Controller {
    private Stage s;
    public Controller(Stage s){
        this.s = s;
    }

    public Stage getStage(){
        return this.s;
    }
}
