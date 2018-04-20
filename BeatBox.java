package games.beatBox;

import javafx.scene.control.RadioButton;
import javax.sound.midi.*;
import java.util.ArrayList;


public class BeatBox {

    Sequencer sequencer;
    Sequence sequence;
    Track track;


    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};


    public void buildTrackAndStart(ArrayList<RadioButton> rbs){
        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            int[] trackList = new int[16];

            int key = instruments[i];

            for (int j = 0; j < 16; j++) {

                RadioButton rb = rbs.get(j + (i*16));
                if (rb.isSelected()){
                    trackList[j] = key;
                } else {
                    trackList[j] = 0;
                }
            } // Close inner loop

            makeTracks(trackList);
            track.add(makeEvent(176, 1, 127, 0, 16));
        } // Close outer loop
        track.add(makeEvent(192, 9, 1, 0, 15));

        try{
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (InvalidMidiDataException ex){
            ex.printStackTrace();
        }
    } // Close buildAndStart method

    public void setUpMidi(){
        try{
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);

        } catch (MidiUnavailableException | InvalidMidiDataException ex){
            ex.printStackTrace();
        }

    }

    public void makeTracks(int[] list){
        for (int i = 0; i < 16; i++) {
            int key = list[i];

            if (key != 0){
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i+1));
            }
        }
    }

    public MidiEvent makeEvent(int comd, int chan, int data1, int data2, int tick){

        MidiEvent event = null;

        try {

            ShortMessage m = new ShortMessage();
            m.setMessage(comd, chan, data1, data2);
            event = new MidiEvent(m, tick);

        } catch (Exception e) {}

        return event;

    }

    public void clear(ArrayList<RadioButton> rbs) {
        try{
            sequence.deleteTrack(track);
            track = sequence.createTrack();
            sequencer.setSequence(sequence);
            sequencer.setTempoInBPM(120);
        } catch (InvalidMidiDataException ex){
            ex.printStackTrace();
        }
        for (RadioButton rb: rbs) {
            rb.setSelected(false);
        }
    }

}
