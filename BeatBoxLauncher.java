package games.beatBox;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;

public class BeatBoxLauncher extends Application{
    Stage window;
    Scene scene;
    BeatBox beatBox;
    ArrayList<RadioButton> notes;
    boolean isPlaying = false;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage){

        beatBox = new BeatBox();
        beatBox.setUpMidi();
        window = primaryStage;
        window.setTitle("Cyber BeatBox");

        MenuBar menuBar = new MenuBar();
        menuBar.setPadding(new Insets(2));

        Menu file = new Menu("File");
        MenuItem save = new MenuItem("Save Music Pattern");
        save.setOnAction(event -> serialize());
        MenuItem restore = new MenuItem("Restore Music Pattern");
        restore.setOnAction(event -> deserialize());
        MenuItem close = new MenuItem("Close");
        close.setOnAction(event -> shutDown());
        file.getItems().addAll(save, restore, close);

        Menu help = new Menu("Help");
        MenuItem aboutMe = new MenuItem("About app");
        aboutMe.setOnAction(event -> giveInfo());
        help.getItems().add(aboutMe);

        menuBar.getMenus().addAll(file, help);


        VBox instruments = new VBox(8);
        instruments.setPadding(new Insets(3, 0, 3, 13));
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
        beats.setPadding(new Insets(10, 0, 0, 0));
        beats.setVgap(11);
        beats.setHgap(8);
        beats.setPrefSize(200, 200);

        notes = new ArrayList<>();
        for (int i = 0; i < 16; i++){
            for (int j = 0; j < 16; j++){
                RadioButton beat = new RadioButton();
                beat.setOnAction(event -> changeRhythm()); // Make CBB dynamic
                notes.add(beat);
                beat.setPrefSize(25, 25);
                beats.add(beat, j, i);
            }
        }
//        beats.setGridLinesVisible(true);


        Button start = new Button("Start");
        start.setOnAction(event -> startPlaying());
        start.setPrefSize(80, 10);

        Button stop = new Button("Stop");
        stop.setOnAction(event -> stopPlaying());
        stop.setPrefSize(80, 10);

        Button tempUp = new Button("Temp up");
        tempUp.setOnAction(event -> beatBox.sequencer.setTempoFactor(beatBox.sequencer.getTempoFactor()*1.06f));
        tempUp.setPrefSize(80, 10);

        Button tempDown = new Button("Temp down");
        tempDown.setOnAction(event -> beatBox.sequencer.setTempoFactor(beatBox.sequencer.getTempoFactor()*0.94f));
        tempDown.setPrefSize(80, 10);

        Button clear = new Button("Clear");
        clear.setOnAction(event -> clearAndStop());
        clear.setPrefSize(80, 10);

        VBox buttons = new VBox(10);
        buttons.setAlignment(Pos.BASELINE_CENTER);
        buttons.setPadding(new Insets(7, 0, 7, 0));
        buttons.getChildren().addAll(start, stop, tempUp, tempDown, clear);


        HBox body = new HBox(10);
        body.getChildren().addAll(instruments, new SplitPane(), beats, new SplitPane(), buttons);

        VBox mainScene = new VBox();
        mainScene.getChildren().addAll(menuBar, body);
        mainScene.setStyle("-fx-background-color: #ffb217");
        scene = new Scene(mainScene);
        window.setResizable(false);
        window.setMaxHeight(520);
        window.setMinWidth(690);
        window.setScene(scene);
        window.getIcons().add(new Image("file:C:\\Users\\Admin\\Downloads\\Work\\icone.jpg"));
        window.show();
        window.setOnCloseRequest(event -> shutDown());
    }

    private void startPlaying(){
        beatBox.buildTrackAndStart(notes);
        isPlaying = true;
    }
    private void stopPlaying(){
        beatBox.sequencer.stop();
        isPlaying = false;
    }
    private void clearAndStop(){
        beatBox.clear(notes);
        isPlaying = false;
    }
    private void changeRhythm(){
        if (isPlaying)
            beatBox.buildTrackAndStart(notes);
    }
    private void shutDown(){
        window.close();
        beatBox.sequencer.close();
    }

    private void serialize(){
        boolean[] patternToSave = new boolean[notes.size()];
        for (int i = 0; i < notes.size(); i++){
            patternToSave[i] = notes.get(i).isSelected();
        }
        try{
            File filePath =  new FileChooser().showSaveDialog(window);
            ObjectOutputStream fileOutStream = new ObjectOutputStream(new FileOutputStream(filePath));
            fileOutStream.writeObject(patternToSave);
            fileOutStream.close();
        } catch (IOException ex){
            System.out.println("Couldn't serialize the pattern");
            ex.printStackTrace();
        }
    }

    private void deserialize(){
        clearAndStop();
        boolean[] restored = new boolean[notes.size()];
        try{
            File filePath =  new FileChooser().showOpenDialog(window);
            ObjectInputStream fileInStream = new ObjectInputStream(new FileInputStream(filePath));
            restored = (boolean[]) fileInStream.readObject(); // casting back
            fileInStream.close();
        } catch (Exception ex){
            System.out.println("Couldn't deserialize the pattern");
            ex.printStackTrace();
        }
        for (int i = 0; i < notes.size(); i++) {
            notes.get(i).setSelected(restored[i]);
        }
    }

    private void giveInfo(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About me");
        alert.setHeaderText("Cyber Beat Box");
        alert.setContentText("is a project that was made by Islam Murtazaev to successfully end second academic" +
                " semester at International Ala-Too University, CS department and was inspired by HeadFirst book.");
        alert.showAndWait();
    }
}
