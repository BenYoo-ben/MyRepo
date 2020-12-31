import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64.Decoder;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.Player;
import javazoom.jl.player.advanced.AdvancedPlayer;

class Songset implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 220682471075274538L;
	int num;
	String SongsetName;
	Vector<Song> Songs;

}

class Song implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2864145606988948092L;
	String title;
	String artist;
	long FileSize;
	byte[] content;
}

public class Game implements Serializable {

	AudioInputStream ai;
	Clip clip;
	int SongNum;
	Songset songset;
	Thread MusicThread;
	int volume;

	private static final long serialVersionUID = 4390724590093121920L;



	void makeSongset(String inputdir, String exportdir, String SongsetName) {
		File[] pathnames;

		File file = new File(inputdir);
		pathnames = file.listFiles();

		Songset newSongset = new Songset();
		newSongset.Songs = new Vector<Song>();

		for (File f : pathnames) {

			if (f.getName().contains(".mp3")) {
				Song s = new Song();
				s.title = f.getName().substring(0, f.getName().length() - 4);
				// s.content = new String(f.);
				s.FileSize = f.length();

				try {
					s.content = Files.readAllBytes(Paths.get(inputdir + f.getName()));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println(s.title + "   " + s.FileSize + "  added.");
				newSongset.Songs.add(s);
				newSongset.num++;

			}

		}

		System.out.println("Writing...");
		try {
			FileOutputStream fout = new FileOutputStream(exportdir + SongsetName);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(newSongset);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done...!");

	}

	Songset readFromData(String input) {
		Songset set = new Songset();
		try {
			FileInputStream fin = new FileInputStream(input);
			ObjectInputStream oin = new ObjectInputStream(fin);
			set = (Songset) oin.readObject();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int i;

		SongNum = 0;
		this.songset = set;
		for (i = 0; i < set.Songs.size(); i++) {
			Song s = this.songset.Songs.elementAt(i);
			System.out.println("Title : " + s.title + "   Size : " + s.FileSize);
		}
		return set;
	}

	void getContent(Song s) {

		MP3Player pl = new MP3Player(s,volume);
		MusicThread = pl;
		
	}

	void playContent() {
		MusicThread.start();
	}

	void stopContent() {
		//deprecated
		MusicThread.stop();
	}
}

class MP3Player extends Thread {
	
	Song s;
	Player mp3Player;
	int volume;
	MP3Player(Song s,int v)
	{
		this.s =s;
		this.volume = v;
	}
	
	public void run()
	{
		try {
			File f = new File("cache.dat");
			FileOutputStream stream;
		
			stream = new FileOutputStream(f);
			
			stream.write(s.content);

			FileInputStream fis = new FileInputStream(f);
			mp3Player = new Player(fis);
			mp3Player.play();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JavaLayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
