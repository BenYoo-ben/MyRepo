import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class TCPHandler {

	private Socket c_socket;
	private short conn_state = 0;

	public TCPHandler() {

		// TODO Auto-generated constructor stub
	}

	protected int MakeConnection() {
		try {
			c_socket = new Socket(GlobalVar.server_ip, GlobalVar.server_port);
			conn_state = 1;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}

		return 0;

	}

	protected int CloseConnection() {
		try {
			c_socket.close();
			conn_state = 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	protected int Send(byte[] buffer) {
		try {
			if (conn_state == 1) {

				DataOutputStream dOut = new DataOutputStream(c_socket.getOutputStream());
				dOut.writeInt(buffer.length);
				dOut.write(buffer);
			} else
				return -1;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}

		return 0;
	}

	protected byte[] Receive() {
		byte[] msg = null;
		try {
			if (conn_state == 1) {
				DataInputStream dIn = new DataInputStream(c_socket.getInputStream());
				int length = dIn.readInt();
				if (length > 0) {
					msg = new byte[length];
					dIn.readFully(msg, 0, msg.length);
				}
			}else
				return null;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return msg;

	}

}
