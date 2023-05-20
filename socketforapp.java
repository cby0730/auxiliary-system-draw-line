package socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class socketforapp {
   private static FileWriter fr;
   public static void main(String[] args) throws Exception {
      InetAddress host = InetAddress.getByName("140.135.236.112");
      Selector selector = Selector.open();
      ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
      serverSocketChannel.configureBlocking(false);
      serverSocketChannel.bind(new InetSocketAddress(host, 7000));
      serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
      SelectionKey key = null;
      
      System.out.println("Server already opens");
      while (true) {
    	 System.out.println("Keep Opening");
         if (selector.select() <= 0)
            continue;
         
         Set<SelectionKey> selectedKeys = selector.selectedKeys();
         Iterator<SelectionKey> iterator = selectedKeys.iterator();

         while (iterator.hasNext()) {
        	System.out.println("New client");
            key = (SelectionKey) iterator.next();
            iterator.remove();
            if (key.isAcceptable()) {
               SocketChannel sc = serverSocketChannel.accept();
               sc.configureBlocking(false);
               sc.register(selector, SelectionKey.OP_READ);
               System.out.println("Connection Accepted: " + sc.getLocalAddress() + "n");
            } // if

            if (key.isReadable()) {
               SocketChannel sc = (SocketChannel) key.channel();
               ByteBuffer bb = ByteBuffer.allocate(1024);
               sc.read(bb);
               String result = new String(bb.array()).trim();
               System.out.println("Message received: " + result + " Message length= " + result.length());
               if (result.length() <= 0) {
                  sc.close();
                  System.out.println( "Connection closed..." );
                  System.out.println( "Server will keep running. " +
                     "Try running another client to " +
                     "re-establish connection");
               } // if
               else {
            	   fr = new FileWriter("routePlan.txt", true);
                   fr.write(result + "\n");
                   fr.flush();
                   fr.close();
               } // else
            }
         }
      }
   }
}
/*
public class socketforapp {

    private static int serverport = 12133;     //自訂的 Port
    private static ServerSocket serverSocket; //伺服端的Socket
    private static int count=0; //計算有幾個 Client 端連線
    private static FileWriter fr;
 
    // 用 ArrayList 來儲存每個 Client 端連線
    private static ArrayList clients = new ArrayList();
 
    public static void main(String[] args) {   
        try {
            serverSocket = new ServerSocket(serverport);
            System.out.println("Server is start.");
            // 顯示等待客戶端連接
            System.out.println("Waiting for client connect");
            // 當Server運作中時
            fr = new FileWriter("Test123.txt");

            while (!serverSocket.isClosed()) { 
                // 呼叫等待接受客戶端連接
                waitNewClient();
            }
            
            fr.close();
        } catch (IOException e) {
            System.out.println("Server Socket ERROR");
        }
        
    }
 
    // 等待接受 Client 端連接
    public static void waitNewClient() {
        try {
            Socket socket = serverSocket.accept();
            ++count;
            System.out.println("現在使用者個數："+count);
 
            // 呼叫加入新的 Client 端
            addNewClient(socket);
            
        } catch (IOException e) {}
    }
 
    // 加入新的 Client 端
    public static void addNewClient(final Socket socket) throws IOException {
        // 以新的執行緒來執行
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 增加新的 Client 端
                    clients.add(socket);
                    // 取得網路串流 
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));      
                    // 當Socket已連接時連續執行
                    while (socket.isConnected()) {
                        // 取得網路串流的訊息
                        String msg= br.readLine();
                        if(msg==null){
                         System.out.println("Client Disconnected!");
                         break;
                        }
                        //輸出訊息
                        fr.write(msg + "\n");
                        fr.flush();
                        System.out.println(msg);
                        // 廣播訊息給其它的客戶端
                        castMsg(msg);
                    }
                } catch (IOException e) {
                 e.getStackTrace();
                }
                finally{
                   // 移除客戶端
                   clients.remove(socket);
                   --count;
                   System.out.println("現在使用者個數："+count);
                }
            }
        });
 
        // 啟動執行緒
        t.start();
    }
 
    // 廣播訊息給其它的客戶端
    public static void castMsg(String Msg){
        // 創造socket陣列
        Socket[] clientArrays =new Socket[clients.size()]; 
        // 將 clients 轉換成陣列存入 clientArrays
        clients.toArray(clientArrays);
        // 走訪 clientArrays 中的每一個元素
        for ( Socket socket : clientArrays ) {
            try {
                // 創造網路輸出串流
                BufferedWriter bw;
                bw = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()));
                // 寫入訊息到串流
                bw.write(Msg+"\n");
                // 立即發送
                bw.flush();
            } catch (IOException e) {}
        }
    }
	
}
*/