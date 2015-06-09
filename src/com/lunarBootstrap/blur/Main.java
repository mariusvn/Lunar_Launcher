package com.lunarBootstrap.blur;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.UIManager;
public class Main {
	
	public static void main(String[] args){
		try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            // empty catch block
        }
		System.out.println("LemonBootStrap v0.1a Started");
		System.out.println("+--------------------------------------+");
		System.out.println("'LemonBootstrap.version' == '0.1a'");
		System.out.println("'os.name' == '" + System.getProperty("os.name") + "'");
		System.out.println("'os.version' == '" + System.getProperty("os.version") + "'");
		System.out.println("'os.arch' == '" + System.getProperty("os.arch") + "'");
		System.out.println("'java.version' == '" + System.getProperty("java.version") + "'");
		System.out.println("'java.vendor' == '" + System.getProperty("java.vendor") + "'");
		System.out.println("'sun.arch.data.model' == '" + System.getProperty("sun.arch.data.model") + "'");
		JFrame frame = new LunarFrame();
		frame.setVisible(true);
	}
}
