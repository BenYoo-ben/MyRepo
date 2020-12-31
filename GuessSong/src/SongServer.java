import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;



public class SongServer {

	ServerSocket serverSocket;
	Socket sock;
	byte[] data;
	char data_type;

	int prepareServer() {
		try {
			serverSocket = new ServerSocket(Global.PORT);
			sock = serverSocket.accept();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;

	}

	void listen() {
		try {
			InputStream input;

			input = (InputStream) sock.getInputStream();
			InputStreamReader isr = new InputStreamReader(input);

			data_type = (char) isr.read();

			if (data_type == Global.REQUEST_FILE) {
				send();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void send() {
		OutputStream output;
		try {
			output = (OutputStream) sock.getOutputStream();

			output.write(Global.SENDING_FILE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void finalize() {
		try {
			serverSocket.close();
			sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
