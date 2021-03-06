package chatServer;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.ArrayList;

public class Server{
  private ServerSocket serverSocket;
  private ArrayBlockingQueue<Message> messages;
  private ArrayList<ClientProxy> clients;

  public Server(int serverPort) throws IOException{
    serverSocket = new ServerSocket(serverPort);
    messages = new ArrayBlockingQueue<Message>(10);
    clients = new ArrayList<ClientProxy>(10);
    (new Thread(new MessageDispatcher())).start();
  }

  public void start(){
    System.out.println("Starting the server");
    while(true){
      try{
        Socket client = serverSocket.accept();
        ClientProxy cp = new ClientProxy(client);
        clients.add(cp);
        (new Thread(cp)).start();
      }catch(IOException ioe){
        ioe.printStackTrace();
      }
    }
  }

  public static void main(String[] args) throws Exception{
    Server server = new Server(39020);
    server.start();
  }

  private class MessageDispatcher implements Runnable{
    public void run(){
      while(true){
        Message message;
        try{
          message = messages.take();
          for(ClientProxy client : clients) {
            try{
              client.write(message);
            }catch(IOException ioe){
              ioe.printStackTrace();
            }
          }
        }catch(InterruptedException ie){
          ie.printStackTrace(); 
        }
      }
    }
  }

  private class ClientProxy implements Runnable{
    private String name;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;

    public ClientProxy(Socket socket) throws IOException {
      this.name = "";
      this.socket = socket;
      this.out = new ObjectOutputStream(socket.getOutputStream());
      this.out.flush();
      this.in = new ObjectInputStream(socket.getInputStream());
    }

    public void write(Message message) throws IOException{
      if(!message.getClientName().equals(name)) {
       synchronized(out) {
         if(out != null){
           out.writeObject(message);
         }
       }
      }
    }

    public void run(){
      Message m;
      try{
        while((m = (Message) in.readObject()) != null){
          System.out.println(m);
          if(m.signingIn()){
            name = m.getClientName();
          }
          messages.add(m);
          if(m.signingOut()){
            break;
          }
        }
        try{
          clients.remove(this);
          synchronized(out){
            out = null;
          }
          socket.close();
        }catch(SocketException se){
          se.printStackTrace();
        }
      }catch(IOException ioe){
          ioe.printStackTrace();
      }catch(ClassNotFoundException cne){
        cne.printStackTrace();
      }
    }

  }
}
