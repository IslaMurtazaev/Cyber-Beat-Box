package beatBox;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MusicServer{

    private ArrayList<ObjectOutputStream> clientOutputStreams;

    public static void main(String[] args) {
        new MusicServer().runServer();
    }

    private void runServer(){
        clientOutputStreams = new ArrayList<>();

        try{
            ServerSocket serverSocket = new ServerSocket(4242);
            System.out.println("Server is running");
            while (true){
                Socket clientSock = serverSocket.accept();
                ObjectOutputStream out = new ObjectOutputStream(clientSock.getOutputStream());
                clientOutputStreams.add(out);

                Thread t = new Thread(new ClientHandler(clientSock));
                t.start();

                System.out.println("A new connection");
            }
        } catch (IOException ex){
//            ex.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable{
        ObjectInputStream in;
        Socket clientSocket;

        public ClientHandler(Socket socket){
            try{
                clientSocket = socket;
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ex){
//                ex.printStackTrace();
            }
        }

        public void run() {
            Object o1;
            Object o2;

            try{
                while ((o1 = in.readObject()) != null){
                    o2 = in.readObject();
                    sendEveryone(o1, o2);
                }
            } catch (Exception ex){
//                ex.printStackTrace();
            }
        }
    }

    private void sendEveryone(Object one, Object two){
        for (ObjectOutputStream out : clientOutputStreams) {
            try {
                out.writeObject(one);
                out.writeObject(two);
            } catch (IOException ex) {
//                ex.printStackTrace();
            }
        }
    }

} // END of class
