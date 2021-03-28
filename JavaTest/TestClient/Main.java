import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TCPHandler tcp_handler = new TCPHandler();
		
		tcp_handler.MakeConnection();
		Scanner sc = new Scanner(System.in);
		
		
		while(true)
		{
			String s = sc.nextLine();
			tcp_handler.Send(s.getBytes());
			byte[] b = tcp_handler.Receive();
			System.out.println("RECVD : \n"+new String(b));
		}
		
		
		
	}

}
