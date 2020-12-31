import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;



public class SongClient {

	Socket sock;
	byte[] data;
	char data_type;
	
	int prepareClient(String hostname, int port)
	{
		try {
			sock = new Socket(hostname, port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}
	
	void listen()
	{
		try {
	
		InputStream input =sock.getInputStream();
		
		InputStreamReader isr = new InputStreamReader(input);
		
		data_type = (char) isr.read();
		if(data_type == Global.SENDING_FILE)
		{
			System.out.println("Done");
		}
		
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void send()
	{
		OutputStream output;
		try {
			output = (OutputStream) sock.getOutputStream();
			output.write(Global.REQUEST_FILE);
			listen();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
