package sync;

import com.pmeade.websocket.io.WebSocketServerOutputStream;
import com.pmeade.websocket.net.WebSocket;
import com.pmeade.websocket.net.WebSocketServerSocket;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author pmeade
 */
public class EchoServerSync {
    public static final int PORT = 8080;
    
    public static void main(String[] args) {
        EchoServerSync echoServer = new EchoServerSync();
        try {
            echoServer.doIt();
        } catch(Exception e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);
        }
    }
    
    public void doIt() throws Exception
    {
        ServerSocket serverSocket = new ServerSocket(PORT);
        WebSocketServerSocket webSocketServerSocket
                = new WebSocketServerSocket(serverSocket);
        while(finished == false) {
            WebSocket socket = webSocketServerSocket.accept();
            new WebSocketThread(socket).start();
        }
    }
    
    public void finish() {
        finished = true;
    }
    
    private boolean finished = false;
}

class WebSocketThread extends Thread {
    public WebSocketThread(WebSocket socket) {
        this.webSocket = socket;
    }
    
    @Override
    public void run() {
        try {
            WebSocketServerOutputStream wsos = webSocket.getOutputStream();
            InputStream wsis = webSocket.getInputStream();
            int data = wsis.read();
            while (finished == false && data != -1) {
                wsos.writeString("Data received: " + (char)data);
                data = wsis.read();
            }
        } catch (IOException e) {
            finished = true;
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);
        }
        try {
            webSocket.close();
        } catch (IOException e) {
            finished = true;
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);
        }
    }

    public void finish() {
        finished = true;
    }
    
    private boolean finished = false;
    
    private final WebSocket webSocket;
}

class MessageQueue<Type> {
  private Queue<Type> q = new LinkedList<Type>();
  private boolean notPushing = true;
  synchronized Type pop() throws InterruptedException {
    while(q.isEmpty()) {
      wait();
    }
    Type value = q.remove();
    notifyAll();
    return value;
  }
  synchronized void doPush(Type message) throws InterruptedException {
    while(!notPushing) {
      wait();
    }
    q.add(message);
    notPushing = false;
    notifyAll();
  }
  synchronized void doNotPush() throws InterruptedException {
    while(notPushing) {
      wait();
    }
    notPushing = true;
    notifyAll();
  }
}
