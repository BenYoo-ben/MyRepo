import java.io.IOException;
import java.net.Socket;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
			JDBMS db = new JDBMS();
			db.PrepareDB();
		
			TCPHandler tcp_handle = new TCPHandler();
			if(tcp_handle.OpenServer()!=0)
				GlobalVar.perror("Server open failure");
			
			Socket c_socket;
			
			if( (c_socket = tcp_handle.AcceptClient())==null)
				GlobalVar.perror("Accept Error");
			
			String test_str = "This is From the Server, Hello.=\n=";
			
			if(tcp_handle.Send(c_socket, test_str.getBytes())!=0)
				GlobalVar.perror("Send Error\n");
			
			
	}

}
