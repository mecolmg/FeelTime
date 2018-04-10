/**
 * @author Uros Krcadinac
 * 17.03.2008.
 * @version 0.1
 */
package synesketch.app;

import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

import synesketch.gui.EmpathyPanel;

import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

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



public class Empathybox {

	private JFrame jFrame = null; 

	private JPanel jContentPane = null;

	private EmpathyPanel appletPanel = null;

	private JScrollPane jScrollPane = null;

	private JTextArea jTextArea = null;
	
	private int dim = 300;
	
	private Clip c = null;
	
	private JFrame getJFrame() {
		if (jFrame == null) {
			jFrame = new JFrame();
			jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			//jFrame.setSize(dim, (int) Math.round(dim * 1.618));
			jFrame.setSize(dim, dim + 150);
			jFrame.setLocation(400, 100);
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
			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
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
			
			
			initialPlaySound();
			jTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyPressed(java.awt.event.KeyEvent e) {
					try {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							String text = jTextArea.getText().trim();
							playSound(text);
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
			c.stop();
		}
		String input = "music_files/" + emotion + ".wav";
		try {
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(input).getAbsoluteFile());
			c = AudioSystem.getClip();
			c.open(audioIn);
			c.start();
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

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Empathybox application = new Empathybox();
				application.getJFrame().setVisible(true);
			}
		});
	}

}
