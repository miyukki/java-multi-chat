import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer implements ChatConnection {
	private int port;
	
	private ChatConnectionListener listener;
	private ServerSocket serverSocket;
	private List<ChatServerClient> clients = new ArrayList<ChatServerClient>();
	
	public ChatServer(int port) {
		this.port = port;
	}
	
	public void start() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			listener.onError("Failed to create to the server.");
		}
	}

	public void setChatConnectionListener(ChatConnectionListener listener) {
		this.listener = listener;
	}

	public void sendMessage(String message) {
		for (ChatServerClient client : clients) {
			client.getClient().sendMessage(message);
		}
	}
	
	private void sendMessageWithoutClient(String message, ChatClient withoutClient) {
		for (ChatServerClient client : clients) {
			if (client.getClient().equals(withoutClient)) {
				continue;
			}
			client.getClient().sendMessage(message);
		}
	}
	
	private void removeServerClient(ChatServerClient serverClient) {
		clients.remove(serverClient);
	}

	public boolean loop() {
		try {
			Socket socket = serverSocket.accept();
			ChatServerClient client = new ChatServerClient();
			client.setChatServer(this);
			client.setChatConnectionListener(listener);
			client.setSocket(socket);
			client.start();
			clients.add(client);
			listener.onConnected();
		} catch (IOException e) {
			e.printStackTrace();
			listener.onError("Failed to connect with client.");
		}
		return true;
	}
	
	public void close() {
		try {
			serverSocket.close();
			for (ChatServerClient client : clients) {
				client.getClient().close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static class ChatServerClient extends Thread {
		private Socket socket;
		private ChatServer server;
		private ChatClient client;
		private ChatConnectionListener listener;
		
		public void run() {
			client = new ChatClient();
			client.setSocket(socket);
			client.setChatConnectionListener(new ChatConnectionListener() {
				public void onMessage(String message) {
					server.sendMessageWithoutClient(message, client);
					listener.onMessage(message);
				}
				public void onError(String errorMessage) {}
				public void onConnected() {}
			});
			client.start();
			while (client.loop()) {}
			server.removeServerClient(this);
		}
		
		public void setSocket(Socket socket) {
			this.socket = socket;
		}
		
		public void setChatServer(ChatServer server) {
			this.server = server;
		}
		
		public void setChatConnectionListener(ChatConnectionListener listener) {
			this.listener = listener;
		}
		
		public ChatClient getClient() {
			return client;
		}
	}
}