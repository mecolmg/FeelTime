/**
 * @author Uros Krcadinac
 * 17.03.2008.
 * @version 0.1
 */
package synesketch.app;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Filters;
import org.bson.BSON;
import synesketch.gui.EmpathyPanel;

import javax.swing.JScrollPane;

import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;

import com.mongodb.MongoClient;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import org.bson.Document;

import java.util.*;
import java.util.Timer;

public class Empathybox {

	private JFrame jFrame = null;

	private JPanel jContentPane = null;

	private EmpathyPanel appletPanel = null;

	private JScrollPane jScrollPane = null;

	private JTextArea jTextArea = null;

	private JLabel picLabel = null, questionLabel = null, reactionLabel = null;
	private int imageNumber = 0;
	private Random r = new Random();


	private int dim = 1040;
	
	private Clip c = null;

	private MongoClient localClient = new MongoClient( "localhost" , 27017 );
	private MongoClient remoteClient = new MongoClient( "172.29.82.98" , 27017 );
	private MongoDatabase localDB = localClient.getDatabase("feeltime");
	private MongoDatabase remoteDB = remoteClient.getDatabase("feeltime");
	private GraphicsDevice device = GraphicsEnvironment
			.getLocalGraphicsEnvironment().getScreenDevices()[0];

	String emotions[] = {"Happy", "Sad", "Angry", "Neutral", "Surprised", "Fearfully", "Disgustingly"};
	String currentEmotion = "Neutral";

	private class MyDispatcher implements KeyEventDispatcher {
		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
					MongoCollection<Document> request = localDB.getCollection("request");
					request.updateOne(new Document(), new Document("$set", new Document("flag", true).append("num", imageNumber)), new UpdateOptions().upsert(true));
					cycleReaction();
				}
			} else if (e.getID() == KeyEvent.KEY_RELEASED) {
			} else if (e.getID() == KeyEvent.KEY_TYPED) {
			}
			return false;
		}
	}
	
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
				jFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
				jFrame.setUndecorated(true);
			}
			jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jFrame.setLayout(new BorderLayout(0,0));
			final BufferedImage image;
			BorderLayout spLayout = new BorderLayout();
			JPanel sidePanel = new JPanel(spLayout);
			picLabel = new JLabel();
			picLabel.setHorizontalAlignment(JLabel.CENTER);
			try {
				image = ImageIO.read(new File("emotion_images/0.jpg").getAbsoluteFile());
				picLabel.setIcon(new ImageIcon(image.getScaledInstance(525, 525, Image.SCALE_FAST)));
				sidePanel.add(picLabel, BorderLayout.PAGE_START);
			} catch (IOException e) {
				e.printStackTrace();
			}
			questionLabel = new JLabel("How does this image make you feel?");
			questionLabel.setHorizontalAlignment(JLabel.CENTER);
			questionLabel.setFont(new Font(Font.SERIF, Font.PLAIN, 24));

			reactionLabel = new JLabel();
			reactionLabel.setHorizontalAlignment(JLabel.CENTER);
			sidePanel.add(questionLabel, BorderLayout.CENTER);
			sidePanel.add(reactionLabel, BorderLayout.PAGE_END);

			jFrame.add(getAppletPanel(), BorderLayout.LINE_START);
			jFrame.add(sidePanel, BorderLayout.CENTER);
			jFrame.setTitle("FeelTime");
			jFrame.setFocusable(true);
			jFrame.requestFocus();
			KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
			manager.addKeyEventDispatcher(new MyDispatcher());
		}
		return jFrame;
	}
//
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			FlowLayout layout = new FlowLayout();
			jContentPane = new JPanel();
			jContentPane.setLayout(layout);
			jContentPane.add(getAppletPanel(), BorderLayout.NORTH);
		}
		return jContentPane;
	}

	private EmpathyPanel getAppletPanel() {
		if (appletPanel == null) {
			try {
				appletPanel = new EmpathyPanel(dim, "Synemania", "synesketch.emotion.SynesthetiatorEmotion");
				appletPanel.setSize(dim, dim);
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
							MongoCollection<Document> collection = remoteDB.getCollection("display1");
							Document req = collection.find().first();
							System.out.println(req);
							String text = req.get("Emotion").toString();
							System.out.println("current emotion:" + currentEmotion + "  text: " + text);
							if (currentEmotion.equals("") || !text.equals(currentEmotion)) {
								currentEmotion = text;	
								playSound(text);
							}
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
		try {
			MongoCollection<Document> request = remoteDB.getCollection("image");
			Document req = request.find().first();
			if (req != null && req.getInteger("num") == imageNumber && req.getBoolean("captured")) {
				String text = "neutral";
				System.out.println("Requesting Emotion from DB");
				try {
					MongoCollection<Document> collection = remoteDB.getCollection("emotion");
					Document emotionReq = collection.find().first();
					text = emotionReq.get("Emotion").toString();
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void cycleImage() {
		try {
			MongoCollection<Document> localImageCol = localDB.getCollection("image");
			MongoCollection<Document> remoteImageCol = localDB.getCollection("image");
			Document localImage = localImageCol.find().first();
			Document remoteImage = remoteImageCol.find().first();
			if (localImage != null && remoteImage != null) {
				if (!localImage.getInteger("num").equals(remoteImage.getInteger("num"))) {
					imageNumber = remoteImage.getInteger("num");
					localImageCol.updateOne(new Document(), new Document("$set", new Document("num", imageNumber).append("captured", false)), new UpdateOptions().upsert(true));
					questionLabel.setText("How does this image make you feel?");
				} else if (localImage.getBoolean("captured") && remoteImage.getBoolean("captured")) {
					imageNumber = (imageNumber + 1) % 10;
					localImageCol.updateOne(new Document(), new Document("$set", new Document("num", imageNumber).append("captured", false)), new UpdateOptions().upsert(true));
					questionLabel.setText("How does this image make you feel?");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			BufferedImage image = ImageIO.read(new File(String.format("emotion_images/%d.jpg", imageNumber)).getAbsoluteFile());
			picLabel.setIcon(new ImageIcon(image.getScaledInstance(525, 525, Image.SCALE_FAST)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void cycleReaction() {
		HashMap<String, Integer> counts = new HashMap<>();
		for (String emotion: emotions) {
			counts.put(emotion, 0);
		}
		try {
			MongoCollection<Document> history = remoteDB.getCollection("history");
			Document req = history.find(new Document("num", imageNumber)).first();
			if (req != null) {
				for (String emotion: emotions) {
					if (req.containsKey(emotion)) {
						counts.put(emotion, req.getInteger(emotion));
					}
				}
			}
			history = localDB.getCollection("history");
			req = history.find().first();
			if (req != null) {
				for (String emotion: emotions) {
					if (req.containsKey(emotion)) {
						counts.put(emotion, counts.get(emotion) + req.getInteger(emotion));
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Couldn't connect to DB");
		}
		String mostCommon = "Neutral";
		int largest = 0;
		for (String emotion: emotions) {
			if (counts.get(emotion) > largest) {
				largest = counts.get(emotion);
				mostCommon = emotion;
			}
		}
		questionLabel.setText(String.format("Most people shown this image reacted %s", mostCommon));
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
				},0,100);
				t.scheduleAtFixedRate(new TimerTask() {
					@Override
					public void run() {
						application.cycleImage();
					}
				}, 0, 100);
//				t.scheduleAtFixedRate(new TimerTask() {
//					@Override
//					public void run() {
//						application.cycleReaction();
//					}
//				}, 7000, 10000);
			}
		});
	}

}
