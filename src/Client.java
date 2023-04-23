import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetAddress.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.EventListener;

public class Client {
    static boolean connected;

    public static void main(String[] args) throws IOException {
        System.out.println("Connecting to server");
        mainWindow();
    }
    static JButton NewNote = new JButton("Add");
    static JButton requestServer = new JButton("View Notes");
    static JFrame f = new JFrame();
    static JPanel panel = new JPanel(new GridLayout(2, 1));
    static JLabel status = new JLabel("Connected to server: "+connected);

    public static void mainWindow(){
        f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
        NewNote.addActionListener(k);
        requestServer.addActionListener(k);
        panel.add(status);
        panel.add(NewNote);
        panel.add(requestServer);
        f.add(panel);
        f.setVisible(true);
        f.pack();
        Thread t = new Thread(checkConnection);
        t.start();
    }

    static Runnable checkConnection = new Runnable() {
        @Override
        public void run() {
            boolean prevStatus = false;
            while (true) {
                try {
                    Socket socket = new Socket("localhost", 8000);
                    connected = true;
                    if (connected != prevStatus) {
                        prevStatus = connected;
                        setWindow();
                    }
                    socket.close();
                } catch (Exception e) {
                    connected = false;
                    if (connected != prevStatus) {
                        prevStatus = connected;
                        setWindow();
                    }
                }
            }
        }
    };

    static public void setWindow(){
        f.invalidate();
        panel.remove(status);
        panel.remove(NewNote);
        status = new JLabel("Connected to server: "+connected);
        panel.add(status);
        panel.add(NewNote);
        panel.add(requestServer);
        f.validate();
        f.repaint();
    }

    public static String getClientsMessage(){
        JFrame f =new JFrame();
        f.setDefaultCloseOperation(f.DISPOSE_ON_CLOSE);
        return JOptionPane.showInputDialog(f,"Enter Message");
    }

    static ActionListener k = new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent e) {

            if (e.getSource() == NewNote) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socket = new Socket("localhost", 8000);
                            connected = true;
                            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                            String testTheOutput = getClientsMessage();
                            out.println(testTheOutput);
                            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                            System.err.println(in.readLine());

                            out.close();
                            socket.close();

                        } catch (Exception ignored) {

                        }
                    }
                };
                Thread t = new Thread(r);
                t.start();
            }

            if (e.getSource() == requestServer){
                try {
                    Socket socket = new Socket("localhost", 8000);
                    connected = true;
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("serverRequest(); ");
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    System.err.println(in.readLine());

                    out.close();
                    socket.close();

                } catch (Exception ignored) {

                }
            }
        }
    };

}
