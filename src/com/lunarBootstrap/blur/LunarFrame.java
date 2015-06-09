package com.lunarBootstrap.blur;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

public class LunarFrame extends JFrame implements ActionListener{
	private LunarPanel panel = new LunarPanel();
	private JButton button1 = new JButton("Installer LunarLemons!");
	private JProgressBar progressBar = new JProgressBar(0, 100);
	public static String TargetVersion = "EZ";

	public LunarFrame(){
		button1.setLocation(panel.getWidth() / 2 - (button1.getWidth() / 2), 390);
		button1.addActionListener(this);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		this.setTitle("Lemon BootStrap");
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setSize(256, 312);
		this.setBackground(Color.white);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel.add(button1);
		String VersionString = null;
		
		try {
			URL VersionURL;
			VersionURL = new URL("http://localhost/v2/api/launcher/latest.version");
			URLConnection yc = VersionURL.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			String inputLine;
			
			while ((inputLine = in.readLine()) != null){
				System.out.println("'LemonBootstrap.targetVersion' == '" + inputLine + "'");
				VersionString = (inputLine);
				TargetVersion = inputLine;
			} 
			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JLabel label = new JLabel("Version : " + VersionString);
		panel.add(label);
		this.setContentPane(panel);
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(src==button1){
			/**
			 * ask the user to close the launcher, then we download mods / and whatever.
			 * 
			 */
			String info = "Merci de fermer Minecraft ainsi que le launcher\n" 
					+ "pour eviter tout soucis lors de l'instalation."; 

			javax.swing.JOptionPane.showMessageDialog(null,info);
			
			String userHomeDir = System.getProperty("user.home", ".");
	        String osType = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
	        File targetDir = null;
	        String mcDir = ".minecraft";
	        targetDir = osType.contains((CharSequence)"win") && System.getenv("APPDATA") != null ? new File(System.getenv("APPDATA"), mcDir) : (osType.contains((CharSequence)"mac") ? new File(new File(new File(userHomeDir, "Library"), "Application Support"), "minecraft") : new File(userHomeDir, mcDir));
	        String path = VersionInfo.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	        if (path.contains((CharSequence)"!/")) {
	            JOptionPane.showMessageDialog(null, "Due to java limitation, please do not run this jar in a folder ending with ! : \n" + path, "Error", 0);
	            return;
	        }
	        InstallerAction action;
	        if (InstallerAction.CLIENT.run(targetDir)) {
	            JOptionPane.showMessageDialog(null, "LunarLemons est desormais installé! bon jeu", "Installation terminée", 1);
	        }
	        
		
		}
	}
}
