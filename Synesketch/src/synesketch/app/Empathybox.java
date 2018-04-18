/**
 * @author Uros Krcadinac
 * 17.03.2008.
 * @version 0.1
 */
package synesketch.app;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

import javafx.application.Application;
import org.w3c.dom.css.Rect;
import synesketch.gui.EmpathyPanel;

import javax.swing.JScrollPane;

import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import java.util.Arrays;
import com.mongodb.Block;

import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.result.DeleteResult;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;
import java.util.ArrayList;
import java.util.List;

import java.util.Timer;
import java.util.TimerTask;

public class Empathybox {

	private JFrame jFrame = null;

	private JPanel jContentPane = null;

	private EmpathyPanel appletPanel = null;

	private JScrollPane jScrollPane = null;

	private JTextArea jTextArea = null;
	
	private int dim = 900;
	
	private Clip c = null;

	private MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
	private MongoDatabase database = mongoClient.getDatabase("feeltime");
	private GraphicsDevice device = GraphicsEnvironment
			.getLocalGraphicsEnvironment().getScreenDevices()[0];
	
	String currentEmotion = "";
	
	public Empathybox() {
		initialPlaySound();
	}

	private JFrame getJFrame() {
		if (jFrame == null) {
			jFrame = new JFrame();
			if (false && device.isFullScreenSupported()) {
				device.setFullScreenWindow(jFrame);
				Rectangle bounds = device.getDefaultConfiguration().getBounds();
				dim = bounds.height;
				jFrame.setSize(bounds.width, bounds.height);
				System.out.printf("%d %d\n", bounds.width, bounds.height);
			} else {
				jFrame.setSize(dim, dim);
			}
			jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			//jFrame.setSize(dim, (int) Math.round(dim * 1.618));
//			jFrame.setLocation(400, 100);
			FlowLayout flowLayout = new FlowLayout();
			jFrame.setLayout(flowLayout);
			jFrame.setContentPane(getJContentPane());
			jFrame.setTitle("FeelTime");
		}
		return jFrame;
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			BorderLayout layout = new BorderLayout();
			jContentPane = new JPanel();
			jContentPane.setLayout(layout);
			jContentPane.add(getAppletPanel(), BorderLayout.NORTH);
//			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	private EmpathyPanel getAppletPanel() {
		if (appletPanel == null) {
			try {
				appletPanel = new EmpathyPanel(dim, "Synemania", "synesketch.emotion.SynesthetiatorEmotion");
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return appletPanel;
	}

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			jScrollPane.setVisible(true);
			jScrollPane.setViewportView(getJTextArea());
		}
		return jScrollPane;
	}

	private JTextArea getJTextArea() {
		//PUT POLL FOR NEW DATA HERE
		if (jTextArea == null) {
			jTextArea = new JTextArea();
			
			
			jTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyPressed(java.awt.event.KeyEvent e) {
					
					try {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							System.out.println("Requesting Emotion");
							MongoCollection<Document> collection = database.getCollection("display1");
							Document req = collection.find().first();
							System.out.println(req);
							String text = req.get("Emotion").toString();
							System.out.println("current emotion:" + currentEmotion + "  text: " + text);
							if (currentEmotion.equals("") || !text.equals(currentEmotion)) {
								currentEmotion = text;	
								playSound(text);
							}
							
//								String text = jTextArea.getText().trim();
							
							appletPanel.fireSynesthesiator(text);
							jTextArea.setText(null);
						}
					} catch (Exception e1) {
						try {
							appletPanel = new EmpathyPanel(dim, "Synemania", "synesketch.emotion.SynesthetiatorEmotion");
						} catch (Exception e3) {
							e3.printStackTrace();
						}
						e1.printStackTrace();
					}
					
				}
			});
		}
		return jTextArea;
	}
	
	private void initialPlaySound(){
		String input = "music_files/neutral.wav";
		try {
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(input).getAbsoluteFile());
			c = AudioSystem.getClip();
			c.open(audioIn);
			c.start();
			System.out.println("in here");
			c.loop(Clip.LOOP_CONTINUOUSLY);
			
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void playSound(String emotion){
		if (c.isRunning()){
			
			for (int i = 0; i <50; i++) {
				FloatControl gainControl = (FloatControl)c.getControl(FloatControl.Type.MASTER_GAIN);
				gainControl.setValue(i * -1f);
				try {
					Thread.sleep(3);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			c.stop();
			
		}
		String input = "music_files/" + emotion + ".wav";
		try {
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(input).getAbsoluteFile());
			c = AudioSystem.getClip();
			c.open(audioIn);
			c.start();
			for (int i = -50; i < 0; i++) {
				FloatControl gainControl = (FloatControl)c.getControl(FloatControl.Type.MASTER_GAIN);
				gainControl.setValue(i);
				try {
					Thread.sleep(3);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			c.loop(Clip.LOOP_CONTINUOUSLY);
			
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getEmotionFromDB() {
		String text = "disgust";
		System.out.println("Requesting Emotion from DB");
		try {
			MongoCollection<Document> collection = database.getCollection("display1");
			Document req = collection.find().first();
			text = req.get("Emotion").toString();
		} catch (Exception e) {
			System.err.println("Couldn't connect to DB");
		}
		try {
			appletPanel.fireSynesthesiator(text);
			System.out.println("current emotion:" + currentEmotion + "  text: " + text);
			if (currentEmotion.equals("") || !text.equals(currentEmotion)) {
				currentEmotion = text;	
				playSound(text);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final Empathybox application = new Empathybox();
				JFrame frame = application.getJFrame();
				frame.setVisible(true);

				Timer t = new Timer();
				t.scheduleAtFixedRate(new TimerTask() {
					@Override
					public void run() {
						application.getEmotionFromDB();
					}
				},0,1000);
			}
		});
	}

}
