import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Scanner;

public class Server {
    static boolean popupEnabled = true;
    static JMenuItem popUp = new JMenuItem("PopUps");

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8000);
        System.out.println("Server started");
        mainWindow();
        while (true) {
            Socket clientSocket = serverSocket.accept();
            clientSocket.getInetAddress().getHostName();
            System.out.println(            clientSocket.getInetAddress().getHostName());
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String note = in.readLine();

            // Code to send notification to other computer
            // ...
            if (note != null){
                if (note.contains("serverRequest();")){
                    out.println("starting data transfer");
                }else {
                    if (popupEnabled) {
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                JFrame fs = new JFrame();
                                fs.setDefaultCloseOperation(fs.DISPOSE_ON_CLOSE);
                                JOptionPane.showMessageDialog(fs, note);
                            }
                        };
                    Thread t = new Thread(r);
                    t.start();
                    }
                    out.println("received");
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    notes[noteSize] = new Note(note, dtf.format(now));
                    noteSize++;
                    Thread save = new Thread(saveThread);
                    save.start();
                    setWindow();
                }
            }
            out.close();
            clientSocket.close();
        }
    }

    private static void setWindow() {
        f.invalidate();
        f.remove(panel);
        panel = new JPanel(new GridLayout(20,1));
        for (int i = 0; i < notes.length; i++) {
            if (notes[i] != null){
                panels[i] = new JPanel(new GridLayout(1, 2));
                buttons[i] = new JButton(notes[i].timeStamp);
                deleteButtons[i] = new JButton("Delete");
                buttons[i].addActionListener(k);
                deleteButtons[i].addActionListener(k);
                panels[i].add(buttons[i]);
                panels[i].add(deleteButtons[i]);
                panel.add(panels[i]);
            }
        }
        f.add(panel);
        f.validate();
        f.repaint();
    }

    static Note[] notes = new Note[1000];
    static int noteSize = 0;
    static JButton[] buttons = new JButton[1000];
    static JButton[] deleteButtons = new JButton[1000];
    static JFrame f = new JFrame();

    static JPanel panel = new JPanel(new GridLayout(20,1));
    static JPanel[] panels = new JPanel[1000];

    public static void mainWindow(){
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty(
                "com.apple.mrj.application.apple.menu.about.name", "Name");

        try{
            FileReader fr = new FileReader("svedta.txt");
            Scanner scanner = new Scanner(fr);
            int size = scanner.nextInt();
            scanner.nextLine();
            for (int i = 0; i < size; i++) {
                notes[i] = new Note(scanner.nextLine(), scanner.nextLine());
            }
            fr.close();
        }catch (Exception ignored){}

        for (int i = 0; i < notes.length; i++) {
            if (notes[i] != null){
                panels[i] = new JPanel(new GridLayout(1, 2));
                buttons[i] = new JButton(notes[i].timeStamp);
                deleteButtons[i] = new JButton("Delete");
                buttons[i].addActionListener(k);
                deleteButtons[i].addActionListener(k);
                panels[i].add(buttons[i]);
                panels[i].add(deleteButtons[i]);
                panel.add(panels[i]);
                noteSize++;
            }
        }
        f.add(panel);
        f.setVisible(true);
        f.setSize(350,1000);
        f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu viewMenu = new JMenu("View");
        viewMenu.add(popUp);
        popUp.addActionListener(k);
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        f.setJMenuBar(menuBar);
        System.err.println("window finished process");
    }

    static ActionEvent event;
    static ActionListener k = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == popUp){
                popupEnabled = !popupEnabled;
            }
            JFrame f=new JFrame();
            boolean showBox = false;
            for (int i = 0; i < 1000; i++) {
                if (buttons[i] == e.getSource()){
                    f.setDefaultCloseOperation(f.DISPOSE_ON_CLOSE);
                    JOptionPane.showMessageDialog(f,notes[i].message);
                    showBox = true;
                }
            }
            if (!showBox){
                event = e;
                Thread thread = new Thread(deleteProcess);
                thread.start();
            }else{
                showBox = false;
            }
        }
    };

    static Runnable deleteProcess = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < 1000; i++) {
                if (deleteButtons[i] == event.getSource()){
                    noteSize--;
                    Note[] retNotes = new Note[1000];
                    JButton[] retButtons = new JButton[1000];
                    JButton[] retDeleteButtons = new JButton[1000];
                    boolean found = false;
                    for (int j = 0; j < 1000; j++) {
                        if (j != i){
                            if (found){
                                if (notes[j] != null) {
                                    retNotes[j - 1] = new Note(notes[j].message, notes[j].timeStamp);
                                    retButtons[j - 1] = new JButton(notes[j].timeStamp);
                                    retDeleteButtons[j - 1] = new JButton("Delete");
                                }
                            }else {
                                if (notes[j] != null) {
                                    retNotes[j] = new Note(notes[j].message, notes[j].timeStamp);
                                    retButtons[j] = new JButton(notes[j].timeStamp);
                                    retDeleteButtons[j] = new JButton("Delete");
                                }
                            }
                        }else{
                            found = true;
                        }
                    }
                    notes = retNotes;
                    buttons = retButtons;
                    deleteButtons = retDeleteButtons;
                    setWindow();
                    Thread save = new Thread(saveThread);
                    save.start();
                }
            }
        }
    };

    static Runnable saveThread = new Runnable() {
        @Override
        public void run() {
            File f = new File("svedta.txt");
            try {
                FileWriter fw = new FileWriter(f);
                fw.write(noteSize+"\n");

                for (int i = 0; i < noteSize; i++) {
                    if (notes[i]!=null){
                        fw.write(notes[i].message+"\n");
                        fw.write(notes[i].timeStamp+"\n");
                    }
                }


                fw.close();
            } catch (Exception ignore){

            }

        }
    };
}