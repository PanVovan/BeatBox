package com.company;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class BeatBox {

    JPanel mainPanel;
    ArrayList<JCheckBox> checkBoxArrayList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame frame;

    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal",
            "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap",
            "Low-mid Tom", "High Agogo", "Open Hi Conga"};

    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args) {
        new BeatBox().buildGUI();
    }
    /////////////////////GUI////////////////////////

    public void buildGUI(){
        frame = new JFrame("BeatBox");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        checkBoxArrayList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton tempoUp = new JButton("Tempo Up");
        tempoUp.addActionListener(new MyUpTempoListener());
        buttonBox.add(tempoUp);

        JButton tempoDown = new JButton("Tempo Down");
        tempoDown.addActionListener(new MyDownTempoListener());
        buttonBox.add(tempoDown);

        JButton readIn = new JButton("Read In");
        readIn.addActionListener(new MyReadInListener());
        buttonBox.add(readIn);

        JButton send = new JButton("Save");
        send.addActionListener(new MySaveListener());
        buttonBox.add(send);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i<16; i++){
            nameBox.add(new Label(instrumentNames[i]));
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        frame.getContentPane().add(background);

        GridLayout grid = new GridLayout(16,16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);

        for (int i = 0; i <256; i++){
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkBoxArrayList.add(c);
            mainPanel.add(c);
        }

        setUpMIDI();

        frame.setBounds(50, 50, 300, 300);
        frame.pack();
        frame.setVisible(true);
    }

    /////////////////////MIDI///////////////////////

    public void setUpMIDI(){
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buildTrackAndStart(){
        int[] trackList = null;

        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++){
            trackList = new int[16];

            int key = instruments[i];

            for (int j = 0; j<16; j++){
                JCheckBox checkBox = (JCheckBox) checkBoxArrayList.get(j+16*i);
                if(checkBox.isSelected()){
                    trackList[j]=key;
                } else {
                    trackList[j] = 0;
                }
            }

            makeTracks(trackList);
            track.add(makeEvent(176, 1,127,0,16));
        }

        track.add(makeEvent(192, 9, 1, 0, 15));
        try{
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (Exception e){
            e.printStackTrace();
        }


    }

    ////////////////ВНУТРЕННИЕ_КЛАССЫ///////////////

    public class MyStartListener implements ActionListener{
        public void actionPerformed(ActionEvent actionEvent) {
            buildTrackAndStart();
        }
    }

    public class MyStopListener implements ActionListener{
        public void actionPerformed(ActionEvent actionEvent) {
            sequencer.stop();
        }
    }

    public class MyUpTempoListener implements ActionListener{
        public void actionPerformed(ActionEvent actionEvent) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor*1.03));
        }
    }

    public class MyDownTempoListener implements ActionListener{
        public void actionPerformed(ActionEvent actionEvent) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor*0.97));
        }
    }

    public class MySaveListener implements ActionListener{

        public void actionPerformed(ActionEvent actionEvent) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showSaveDialog(frame);
            saveFile(fileChooser.getSelectedFile());
        }
    }

    public class MyReadInListener implements ActionListener{

        public void actionPerformed(ActionEvent actionEvent) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showOpenDialog(frame);
            loadFile(fileChooser.getSelectedFile());
        }
    }
    ////////////////СОХРАНЕНИЕ//////////////////////
    private void loadFile(File file){
        boolean[] checkBoxState = new boolean[256];

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            checkBoxState = (boolean[]) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i<256; i++){

            JCheckBox check = (JCheckBox)checkBoxArrayList.get(i);
            if (checkBoxState[i]){
                check.setSelected(true);
            } else {
                check.setSelected(false);
            }
        }
        sequencer.stop();
        buildTrackAndStart();
    }

    private void saveFile(File file){
        boolean[] checkBoxState = new boolean[256];

        for (int i = 0; i<256; i++){

            JCheckBox check = (JCheckBox)checkBoxArrayList.get(i);
            if (check.isSelected()){
                checkBoxState[i] = true;
            }
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(checkBoxState);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    ////////////////МЕТОДЫ_СОЗДАНИЯ/////////////////

    public void makeTracks(int[] list){
        for (int i = 0; i < 16; i++){
            int key = list[i];

            if (key!=0){
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i+1));
            }
        }
    }

    public MidiEvent makeEvent (int command, int channel, int one, int two, int tick){
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(command, channel, one, two);
            event = new MidiEvent(a, tick);
        } catch (Exception ex) {ex.printStackTrace();}
        return event;
    }

}
