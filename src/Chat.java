import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * 複数人でチャットするクラス
 * @author Sakura <t13964yy@sfc.keio.ac.jp>
 * @repository https://github.com/miyukki/java-multi-chat
 */
public class Chat implements ActionListener, ChatConnectionListener {
	
	/**
	 * 実行時に一番最初に呼び出されるメソッド
	 * @param args
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static void main(String[] args) throws NumberFormatException, IOException {
		if (args.length != 1 && args.length != 2) {
			System.out.println("USAGE: java Chat PORT");
			System.out.println(" ex: java Chat 12345");
			System.out.println("USAGE: java Chat SERVER PORT");
			System.out.println(" ex: java Chat localhost 12345");
			System.exit(0);
		}

		Chat chat = null;
		if (args.length == 1) {
			chat = new Chat(new ChatServer(Integer.parseInt(args[0])));
		} else if (args.length == 2) {
			chat = new Chat(new ChatClient(args[0], Integer.parseInt(args[1])));
		}
		while (chat.loop()) {}
		chat.close();
	}
	
	private String name;
	private ChatConnection connection;
	
	private JTextArea log;
	private JTextField text;
	private JFrame window;
	
	/**
	 * Chatクラスのコンストラクタ
	 * ここではサーバとクライアントを区別せずに実装している
	 * @param connection ChatServer または ChatClient
	 */
	public Chat(ChatConnection connection) {
		inputUserName();
		createWindow();
		this.connection = connection;
		this.connection.setChatConnectionListener(this);
		this.connection.start();
	}
	
	/**
	 * ユーザーに名前を入力してもらい、その値をnameに代入する
	 */
	private void inputUserName() {
		name = JOptionPane.showInputDialog("あなたの名前は？");
	}

	/**
	 * ウィンドウを作り、表示する
	 */
	private void createWindow() {
		window = new JFrame("Chat");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		text = new JTextField(20);
		text.setActionCommand("Text");
		text.addActionListener(this);
		text.setEnabled(false);
		log = new JTextArea(20, 20);
		log.setEditable(false);
		window.add(text, BorderLayout.NORTH);
		window.add(log, BorderLayout.CENTER);
		window.pack();
		window.setVisible(true);
	}
	
	/**
	 * 接続が完了した時のコールバック
	 */
	public void onConnected() {
		text.setEnabled(true);
	}
	
	/**
	 * エラーが発生した時のコールバック
	 */
	public void onError(String errorMessage) {
		System.out.println("[SocketError] " + errorMessage + "\n");
	}

	/**
	 * メッセージを受信した時のコールバック
	 */
	public void onMessage(String message) {
		log.setText(message + "\n" + log.getText());
	}
	
	/**
	 * 入力フィールドでリターンキーが押された時のコールバック
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Text")) {
			connection.sendMessage(name + ": " + text.getText());
			log.setText(name + ": " + text.getText() + "\n" + log.getText());
			text.setText("");
		}
	}
	
	/**
	 * ループメソッド
	 * @return ループさせ続けるかどうか
	 */
	public boolean loop() {
		return connection.loop();
	}
	
	/**
	 * アプリケーションを終了する際にクローズ処理を入れる
	 */
	public void close() {
		connection.close();
	}
}