
public class GlobalVar {
	
	
	static int server_port = 54321;
	
	static void perror(String err_msg)
	{
		System.out.println("========ERR=======\n"+err_msg+"========ERR=======\\n");
		System.exit(1);
		
	}
}
