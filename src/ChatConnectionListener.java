interface ChatConnectionListener {
	public void onConnected();

	public void onError(String errorMessage);

	public void onMessage(String message);
}
