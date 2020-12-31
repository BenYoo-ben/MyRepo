import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
	Global.PORT = 45000+(int)(Math.random()*10000);
		Game g = new Game();
	
		//new Game().makeSongset("D:\\test\\", "D:","TestFile.by");
		
		GUI gui = new GUI();
	
		
		
		
		//g.readFromData("D:\\TestFile.by");
		
		Scanner sc = new Scanner(System.in);
		while(true)
		{
			int i = sc.nextInt();
			switch(i)
			{
			case 1 :
				g.playContent();
				break;
			case 2 :
				g.stopContent();
				break;
			case 3 :
				g.getContent(g.songset.Songs.elementAt(g.SongNum++));
				break;
			case 4 :
				SongServer s = new SongServer();
				s.prepareServer();
				s.listen();
				break;
			case 5:
				SongClient  c = new SongClient();
				c.prepareClient("127.0.0.1", Global.PORT);
				c.send();
			}
		}
	}	

}
