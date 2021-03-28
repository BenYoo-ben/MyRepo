
public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TCPHandler tcp_handler = new TCPHandler();
		
		tcp_handler.MakeConnection();
		byte[] test = tcp_handler.Receive();
		System.out.println("Received:\n"+new String(test));
	}

}
