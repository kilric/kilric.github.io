public class Node1ReverseProxy {

  private static final int PORT = 80;

  public static void main(String[] args) throws IOException {
    ServerSocket serverSocket = new ServerSocket(PORT);

    while (true) {
      Socket clientSocket = serverSocket.accept();
      ProxyThread proxyThread = new ProxyThread(clientSocket);
      proxyThread.start();
    }
  }

}

class ProxyThread extends Thread {

  private Socket clientSocket;
  
  public ProxyThread(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  public void run() {
    try {
      // 1. 获取请求参数
      BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      String request = reader.readLine();
      String[] parts = request.split(" ");
      String node2Ip = parts[1].split("/")[2];
      int node2Port = Integer.parseInt(parts[1].split("/")[3]);

      // 2. 转发请求到节点2
      Socket node2Socket = new Socket(node2Ip, node2Port);
      PrintWriter out = new PrintWriter(node2Socket.getOutputStream(), true);
      out.println(request);
      
      // 3. 将响应转发给客户端
      BufferedReader in = new BufferedReader(new InputStreamReader(node2Socket.getInputStream()));
      PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
      
      String line;
      while ((line = in.readLine()) != null) {
        clientOut.println(line);  
      }

      in.close();
      out.close();
      clientSocket.close();
      node2Socket.close();
      
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
