import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;

public class BeatBoxLauncher extends Application{
    Stage window;
    Scene scene;
    BeatBox beatBox;
    ArrayList<RadioButton> notes;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage){

        beatBox = new BeatBox();
        beatBox.setUpMidi();
        window = primaryStage;
        window.setTitle("Cyber BeatBox");

        VBox instruments = new VBox(10);
        instruments.setPadding(new Insets(10, 0, 0, 10));
        instruments.setAlignment(Pos.BASELINE_CENTER);

        String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal",
                "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibaraslap",
                "Low-mid Tom", "High Agogo", "Open Hi Conga"};
        for (String instrument : instrumentNames){
            Label label = new Label(instrument);
            label.setFont(new Font("serif", 14));
            instruments.getChildren().add(label);
        }


        GridPane beats = new GridPane();
        beats.setPadding(new Insets(14, 0, 0, 0));
        beats.setVgap(4.7);
        beats.setHgap(8);
        beats.setPrefSize(200, 200);

        notes = new ArrayList<>();
        for (int i = 0; i < 16; i++){
            for (int j = 0; j < 16; j++){
                RadioButton beat = new RadioButton();
                notes.add(beat);
                beat.setPrefSize(25, 25);
                beats.add(beat, j, i);
            }
        }
//        beats.setGridLinesVisible(true);


        Button start = new Button("Start");
        start.setOnAction(event -> beatBox.buildTrackAndStart(notes));
        start.setPrefSize(80, 10);

        Button stop = new Button("Stop");
        stop.setOnAction(event -> beatBox.sequencer.stop());
        stop.setPrefSize(80, 10);

        Button tempUp = new Button("Temp up");
        tempUp.setOnAction(event -> beatBox.sequencer.setTempoFactor(beatBox.sequencer.getTempoFactor()*1.03f));
        tempUp.setPrefSize(80, 10);

        Button tempDown = new Button("Temp down");
        tempDown.setOnAction(event -> beatBox.sequencer.setTempoFactor(beatBox.sequencer.getTempoFactor()*0.97f));
        tempDown.setPrefSize(80, 10);

        Button clear = new Button("Clear");
        clear.setOnAction(event -> beatBox.clear(notes));
        clear.setPrefSize(80, 10);

        VBox buttons = new VBox(10);
        buttons.setAlignment(Pos.BASELINE_CENTER);
        buttons.setPadding(new Insets(10, 0, 10, 0));
        buttons.getChildren().addAll(start, stop, tempUp, tempDown, clear);


        HBox mainLayout = new HBox(10);
        mainLayout.getChildren().addAll(instruments, new SplitPane(), beats, new SplitPane(), buttons);

        scene = new Scene(mainLayout);
        window.setResizable(false);
        window.setMinHeight(540);
        window.setMinWidth(690);
        window.setScene(scene);
        window.show();
        window.setOnCloseRequest(event -> beatBox.sequencer.close());
    }
}
