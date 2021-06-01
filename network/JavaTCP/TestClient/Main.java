import java.util.Scanner;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileWriter;
import java.io.ByteArrayInputStream;
import java.io.BufferedOutputStream;;
public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TCPHandler tcp_handler = new TCPHandler();

		tcp_handler.MakeConnection();

		byte[] t = tcp_handler.Receive();
		String str = new String(t);
		if (str.equals("0"))
			System.out.println("Connection Established");
		Scanner sc = new Scanner(System.in);

		String s = sc.nextLine();
		tcp_handler.Send(s.getBytes());
		

		// send file example

		int mode = 3;

		if (mode == 0) {
			try {
				System.out.println(new String(tcp_handler.Receive()));
				byte[] array = Files.readAllBytes(Paths.get("./jpop.mp3"));
				tcp_handler.Send(array);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(mode==2)
		{
			System.out.println(new String(tcp_handler.Receive()));
		}
		if(mode==1)
		{
			String s1 = new String(tcp_handler.Receive());
		
			if(s1.equals("1"))
				System.out.println("No File");
			if(s1.equals("2"))
				System.out.println("Wrong code");
			
			if(s1.equals("0"))
			{
				tcp_handler.Send(new String("0").getBytes());
				
				try {
					String filename = "RAOTED1";
					byte[] Data = tcp_handler.Receive();
					int readCount = 0;
					File f = new File(filename);
					FileOutputStream fos = new FileOutputStream(f);
					ByteArrayInputStream bin = new ByteArrayInputStream(Data);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					byte[] outBuffer = new byte[1000000];

					System.out.println("Data Write...");
					while ((readCount = bin.read(outBuffer)) > 0) {
						bos.write(outBuffer, 0, readCount);
					}

					System.out.println("Data Write Complete!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if(mode==3)
		{
			String s1 = new String(tcp_handler.Receive());
			
			if(s1.equals("1"))
				System.out.println("No File");
			if(s1.equals("2"))
				System.out.println("Wrong code");
			if(s1.equals("3"))
				System.out.println("File in Use...");
			
			if(s1.equals("0"))
			{
				System.out.println("Successfully Removed.");
			}
		}

	}

}
