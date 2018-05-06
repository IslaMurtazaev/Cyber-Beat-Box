package beatBox;

import javafx.application.Application;
import javafx.collections.FXCollections;
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
import java.net.Socket;
import java.util.*;

public class BeatBoxLauncher extends Application{
    private VBox mainScene;
    private Stage window;
    private BeatBox beatBox;
    private ListView incomingList;
    private TextField userMessage;
    private String userName = "Anonymous";
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ArrayList<String> usersNames = new ArrayList<>();
    private HashMap<String, boolean[]> otherSeqsMap = new HashMap<>();
    private ArrayList<RadioButton> notes;
    private boolean isPlaying = false;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage){
        setUserName();
        beatBox = new BeatBox();
        beatBox.setUpMidi();
        buildGUI();
        setUpNetwork();
    }

    private void buildGUI(){

        MenuItem save = new MenuItem("Save Music Pattern");
        save.setOnAction(event -> serialize());

        MenuItem restore = new MenuItem("Restore Music Pattern");
        restore.setOnAction(event -> deserialize());

        MenuItem close = new MenuItem("Close");
        close.setOnAction(event -> shutDown());

        Menu file = new Menu("File");
        file.getItems().addAll(save, restore, close);


        MenuItem colorPicker = new MenuItem("Change background color");
        colorPicker.setOnAction(event -> changeBackgroundColor());

        Menu edit = new Menu("Edit");
        edit.getItems().add(colorPicker);


        MenuItem aboutMe = new MenuItem("About app");
        aboutMe.setOnAction(event -> giveInfo());

        Menu help = new Menu("Help");
        help.getItems().add(aboutMe);

        MenuBar menuBar = new MenuBar();
        menuBar.setPadding(new Insets(2));
        menuBar.getMenus().addAll(file, edit, help);


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
        beats.setPadding(new Insets(7, 0, 0, 5));
        beats.setVgap(3);
        beats.setHgap(8);

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


        Button start = new Button("Start");
        start.setOnAction(event -> startPlaying());
        start.setPrefSize(100, 10);

        Button stop = new Button("Stop");
        stop.setOnAction(event -> stopPlaying());
        stop.setPrefSize(100, 10);

        Button tempUp = new Button("Temp up");
        tempUp.setOnAction(event -> beatBox.sequencer.setTempoFactor(beatBox.sequencer.getTempoFactor()*1.06f));
        tempUp.setPrefSize(100, 10);

        Button tempDown = new Button("Temp down");
        tempDown.setOnAction(event -> beatBox.sequencer.setTempoFactor(beatBox.sequencer.getTempoFactor()*0.94f));
        tempDown.setPrefSize(100, 10);

        Button clear = new Button("Clear");
        clear.setOnAction(event -> clearAndStop());
        clear.setPrefSize(100, 10);

        Button sendIt = new Button("Send it");
        sendIt.setOnAction(event -> sendPattern());
        sendIt.setPrefSize(100, 10);

        userMessage = new TextField();

        incomingList = new ListView();
        incomingList.setPrefWidth(80);
        incomingList.setOnMouseClicked(event ->  selectPattern());
        incomingList.setItems(FXCollections.observableList(usersNames));

        VBox buttons = new VBox(10);
        buttons.setAlignment(Pos.TOP_CENTER);
        buttons.setPadding(new Insets(10, 5, 10, 5));
        buttons.getChildren().addAll(start, stop, tempUp, tempDown, clear, sendIt, userMessage, incomingList);

        HBox body = new HBox(10);
        body.getChildren().addAll(instruments, new SplitPane(), beats, new SplitPane(), buttons);

        mainScene = new VBox();
        mainScene.getChildren().addAll(menuBar, body);
        mainScene.setStyle("-fx-background-color: #ff4a4d");

        window = new Stage();
        window.setTitle("Cyber BeatBox");
        window.setResizable(false);
        window.setMaxHeight(520);
        window.setMinWidth(690);
        window.setScene(new Scene(mainScene));
        window.getIcons().add(new Image("icone.jpg"));
        window.show();
        window.setOnCloseRequest(event -> shutDown());
    } // Close buildGUI method

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
        try {
            in.close();
            out.close();
        } catch (Exception ex){ }
    }

    private void sendPattern(){
        boolean[] notesState = new boolean[notes.size()];

        for (int i = 0; i < notes.size(); i++) {
            notesState[i] = notes.get(i).isSelected();
        }

        try{
            out.writeObject(userName +": "+ userMessage.getText());
            out.writeObject(notesState);
            userMessage.clear();
        } catch (Exception ex){
            showErrorMessage("Can't send the pattern to the server", ex);
        }
    }

    private void selectPattern(){
        String selected = (String) incomingList.getSelectionModel().getSelectedItem();

        if (selected != null){
            boolean[] selectedState = otherSeqsMap.get(selected);
            changeSequence(selectedState);
            changeRhythm();
        }
    }

    private void changeSequence(boolean[] selectedState){
        clearAndStop();
        for (int i = 0; i < notes.size(); i++) {
            notes.get(i).setSelected(selectedState[i]);
        }
    }

    private void serialize(){
        boolean[] patternToSave = new boolean[notes.size()];

        for (int i = 0; i < notes.size(); i++){
            patternToSave[i] = notes.get(i).isSelected();
        }

        try {
            File filePath = new FileChooser().showSaveDialog(window);
            ObjectOutputStream fileOutStream = new ObjectOutputStream(new FileOutputStream(filePath));
            fileOutStream.writeObject(patternToSave);
            fileOutStream.close();
        } catch (NullPointerException ex){
        } catch (Exception ex){
            showErrorMessage("Couldn't save the pattern", ex);
        }
    }

    private void deserialize(){
        boolean[] restored = new boolean[notes.size()];

        try {
            File filePath = new FileChooser().showOpenDialog(window);
            ObjectInputStream fileInStream = new ObjectInputStream(new FileInputStream(filePath));
            restored = (boolean[]) fileInStream.readObject(); // casting back
            fileInStream.close();
        } catch (NullPointerException ex){
        } catch (Exception ex){
            showErrorMessage("Couldn't retrieve the pattern", ex);
        }

        changeSequence(restored);
    }

    private void setUserName(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log in");
        alert.setHeaderText("What is your name?");

        TextField name = new TextField();
        name.setAlignment(Pos.CENTER);
        alert.setGraphic(name);
        name.setOnKeyReleased(event -> userName = name.getText());

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("icone.jpg"));
        alert.showAndWait();
    }

    private void changeBackgroundColor(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Background");
        alert.setHeaderText("Pick any color you like");

        ListView<String> colors = new ListView<>();
        colors.setItems(FXCollections.observableList(Arrays.asList("white", "gold", "silver", "orange","pink", "aqua",
                "beige", "turquoise", "lavender", "chartreuse", "brown", "coral", "khaki", "violet")));
        colors.setMaxHeight(200);
        colors.setOnMouseClicked(event ->  mainScene.setStyle("-fx-background-color: "+ colors.getSelectionModel().getSelectedItem()));

        alert.setGraphic(colors);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("icone.jpg"));

        alert.showAndWait();
    }

    private void showErrorMessage(String error,Exception ex){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(error);
        alert.setContentText(ex.toString());
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("icone.jpg"));
        alert.showAndWait();
    }

    private void giveInfo(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About me");
        alert.setHeaderText("Cyber Beat Box");
        alert.setContentText("is a project that was made by Islam Murtazaev to successfully end second academic" +
                " semester at International Ala-Too University, CS department and was inspired by HeadFirst book.");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("icone.jpg"));
        alert.showAndWait();
    }

    private void setUpNetwork(){
        try{
            Socket sock = new Socket("127.0.0.1", 4242);
            out = new ObjectOutputStream(sock.getOutputStream());
            in = new ObjectInputStream(sock.getInputStream());
            Thread remote = new Thread(new RemoteReader());
            remote.start();
        } catch (Exception ex){
            showErrorMessage("Couldn't connect to the server, you will have to play alone", ex);
        }
    }

    private class RemoteReader implements Runnable{
        boolean[] notesState;
        Object name;

        public void run() {
            try{
                while ((name = in.readObject()) != null){
                    String nameToShow = (String) name;
                    notesState = (boolean[]) in.readObject();
                    otherSeqsMap.put(nameToShow, notesState);
                    usersNames.add(nameToShow);
                    incomingList.setItems(FXCollections.observableList(usersNames));
                }
            } catch (Exception ex){ }
        }
    }

} // END of class
