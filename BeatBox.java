package beatBox;

import javafx.scene.control.RadioButton;
import javax.sound.midi.*;
import java.util.ArrayList;

public class BeatBox {
    public Sequencer sequencer;
    private Sequence sequence;
    private Track track;
    private int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public void buildTrackAndStart(ArrayList<RadioButton> notes){
        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            int[] trackList = new int[16];

            int key = instruments[i];

            for (int j = 0; j < 16; j++) {

                RadioButton note = notes.get(j + (i*16));
                if (note.isSelected()){
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
            sequencer.setTempoFactor(1.0f);
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
            sequencer.setTempoFactor(1.0f);

        } catch (MidiUnavailableException | InvalidMidiDataException ex){
            ex.printStackTrace();
        }

    }

    private void makeTracks(int[] list){
        for (int i = 0; i < 16; i++) {
            int instrument = list[i];

            if (instrument != 0){
                track.add(makeEvent(144, 9, instrument, 100, i));
                track.add(makeEvent(128, 9, instrument, 100, i+1));
            }
        }
    }

    private MidiEvent makeEvent(int comd, int chan, int data1, int data2, int tick){

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
            sequencer.setTempoFactor(1.0f);
        } catch (InvalidMidiDataException ex){
            ex.printStackTrace();
        }
        for (RadioButton rb: rbs) {
            rb.setSelected(false);
        }
    }

} // END of class
