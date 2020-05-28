import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.*;

public class BeatBoxFinal {
    JFrame theFrame;
    JPanel mainPanel;
    JList<String> incomingList;
    JTextField userMessage;
    ArrayList<JCheckBox> checkboxList;
    ObjectInputStream in;
    ObjectOutputStream out;
    Vector<String> listVector = new Vector<String>();
    String userName;
    int nextNum; // to count messages sent
    Map<String, boolean[]> otherSeqsMap = new HashMap<String, boolean[]>();

    Sequencer sequencer;
    Sequence sequence;
    Sequence mySequence = null;
    Track track;

    String[] instrumentNames = { "Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal",
            "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap",
            "Low-mid Tom", "High Agogo", "Open Hi Conga" };
    int[] instruments = { 35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63 };

    public static void main(String[] args) {
        if (args.length != 0) {
            new BeatBoxFinal().startUp(args[0]);
        } else {
            new BeatBoxFinal().startUp("Anonymous");
        }
    }

    public void startUp(String name) {
        userName = name;
        try {
            Socket sock = new Socket("127.0.0.1", 4242);
            out = new ObjectOutputStream(sock.getOutputStream());
            in = new ObjectInputStream(sock.getInputStream());
            Thread remote = new Thread(new RemoteReader());
            remote.start();
        } catch (Exception ex) {
            System.out.println("couldn't connect - you'll have to play alone.");
        }
        setUpMidi();
        buildGUI();
    }

    public void buildGUI() {
        theFrame = new JFrame("Cyber BeatBox");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkboxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Play");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        JButton saveIt = new JButton("Save");
        saveIt.addActionListener(new MySaveListener());
        buttonBox.add(saveIt);

        JButton restore = new JButton("Restore");
        restore.addActionListener(new MyReadInListener());
        buttonBox.add(restore);

        userMessage = new JTextField();
        buttonBox.add(userMessage);

        JButton sendIt = new JButton("Send");
        sendIt.addActionListener(new MySendListener());
        buttonBox.add(sendIt);

        incomingList = new JList<>();
        incomingList.addListSelectionListener(new MyListSelectionListener());
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane theList = new JScrollPane(incomingList);
        buttonBox.add(theList);
        incomingList.setListData(listVector);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        theFrame.getContentPane().add(background);

        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);

        for (int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);
            mainPanel.add(c);
        }

        theFrame.setBounds(50, 50, 300, 300);
        theFrame.pack();
        theFrame.setVisible(true);
    } // close buildGUI

    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // close setUpMidi

    public void buildTrackAndStart() {
        // this will hold the instruments for each vertical column,
        // in other words, each tick (may have multiple instruments)
        ArrayList<Integer> trackList;

        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            trackList = new ArrayList<Integer>();
            for (int j = 0; j < 16; j++) {
                JCheckBox jc = (JCheckBox) checkboxList.get(j + (16 * i));
                if (jc.isSelected()) {
                    int key = instruments[i];
                    trackList.add(key);
                } else {
                    trackList.add(null);
                }
            }
            makeTracks(trackList);
        }

        track.add(makeEvent(192, 9, 1, 0, 15)); // so we always go to full 16 beats

        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // close buildTrackAndStart

    // ========================== inner class listeners ==========================

    public class MyStartListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            buildTrackAndStart();
        }
    }

    public class MyStopListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            sequencer.stop();
        }
    }

    public class MyUpTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 1.03));
        }
    }

    public class MyDownTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * .97));
        }
    }

    public class MySendListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            // make an array of just the STATE of the checkboxes
            boolean[] checkboxState = new boolean[256];
            for (int i = 0; i < 256; i++) {
                if (checkboxList.get(i).isSelected()) {
                    checkboxState[i] = true;
                }
            }

            try {
                out.writeObject(userName + nextNum++ + ": " + userMessage.getText());
                out.writeObject(checkboxState);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Sorry dude. Could not send it to the server.");
            }
        } // close actionPerformed
    } // close MySendListener

    public class MySaveListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            // make an array of just the STATE of the checkboxes
            boolean[] checkboxState = new boolean[256];
            for (int i = 0; i < 256; i++) {
                if (checkboxList.get(i).isSelected()) {
                    checkboxState[i] = true;
                }
            }

            try {
                FileOutputStream fileStream = new FileOutputStream(new File("checkbox.ser"));
                ObjectOutputStream os = new ObjectOutputStream(fileStream);
                os.writeObject(checkboxState);
                os.close();
                System.out.println("Saved to checkbox.ser");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } // close actionPerformed
    } // close inner MySaveListener

    public class MyReadInListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            boolean[] checkboxState = null;
            try {
                FileInputStream fileIn = new FileInputStream(new File("checkbox.ser"));
                ObjectInputStream is = new ObjectInputStream(fileIn);
                checkboxState = (boolean[]) is.readObject();
                is.close();
                System.out.println("Restore from checkbox.ser");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            for (int i = 0; i < 256; i++) {
                JCheckBox check = checkboxList.get(i);
                if (checkboxState[i]) {
                    check.setSelected(true);
                } else {
                    check.setSelected(false);
                }
            }

            sequencer.stop();
            buildTrackAndStart();
        } // close actionPerformed
    } // close MyReadInListener

    public class MyListSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent le) {
            if (!le.getValueIsAdjusting()) {
                String selected = incomingList.getSelectedValue();
                if (selected != null) {
                    boolean[] selectedState = otherSeqsMap.get(selected);
                    changeSequence(selectedState);
                    sequencer.stop();
                    buildTrackAndStart();
                }
            }
        } // close valueChanged
    } // close MyListSelectionListener

    public class RemoteReader implements Runnable {
        boolean[] checkboxState;
        String nameToShow;
        Object obj;

        public void run() {
            try {
                while ((obj = in.readObject()) != null) {
                    System.out.println("got a message from server");
                    String nameToShow = (String) obj;
                    checkboxState = (boolean[]) in.readObject();
                    otherSeqsMap.put(nameToShow, checkboxState);
                    listVector.add(nameToShow);
                    incomingList.setListData(listVector);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } // close run
    } // close RemoteReader

    // ==============================================================

    public void changeSequence(boolean[] checkboxState) {
        for (int i = 0; i < 256; i++) {
            JCheckBox check = checkboxList.get(i);
            if (checkboxState[i]) {
                check.setSelected(true);
            } else {
                check.setSelected(false);
            }
        }
    }

    public void makeTracks(ArrayList<Integer> list) {
        Iterator<Integer> it = list.iterator();
        for (int i = 0; i < 16; i++) {
            Integer key = it.next();
            if (key != null) {
                track.add(makeEvent(144, 9, key.intValue(), 100, i));
                track.add(makeEvent(128, 9, key.intValue(), 100, i + 1));
            }
        }
    }

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return event;
    }
}
