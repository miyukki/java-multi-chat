import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

public class ChatClient implements ChatConnection {
	private String host;
	private int port;
	
	private ChatConnectionListener listener; 
	private Socket socket;
	private BufferedReader in;
	private Writer out;
	
	public ChatClient() {
		
	}
	
	public ChatClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public void start() {
		try {
			if (socket == null) {
				socket = new Socket(host, port);
			}
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new OutputStreamWriter(socket.getOutputStream());
			listener.onConnected();
		} catch (IOException e) {
			e.printStackTrace();
			listener.onError("Failed to connect to the server.");
		}
	}
	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	public void setChatConnectionListener(ChatConnectionListener listener) {
		this.listener = listener;
	}
	
	public void sendMessage(String message) {
		try {
			out.write(message + "\n");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			listener.onError("Failed to send the message.");
		}
	}
	
	private String receiveMessage() {
		try {
			return in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			listener.onError("Failed to receive the message.");
		}
		return null;
	}

	public boolean loop() {
		String message = receiveMessage();
		if (message == null) {
			return false;
		}
		listener.onMessage(message);
		return true;
	}
	
	public void close() {
		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}