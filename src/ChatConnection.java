interface ChatConnection {
	public void start();
	public void setChatConnectionListener(ChatConnectionListener listener);
	public void sendMessage(String message);
	public boolean loop();
	public void close();
}