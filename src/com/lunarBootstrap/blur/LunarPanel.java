package com.lunarBootstrap.blur;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
/**
 * Not that clean huh ?
 * @author xBlur
 */
public class LunarPanel extends JPanel{
	public void paintComponent(Graphics g){
		Dimension d = this.getPreferredSize();
		int fontsize = 20;
		g.setColor(Color.black);
		try{
			Image img = ImageIO.read(getClass().getResource("logojpg.jpg"));
			g.drawImage(img, this.getWidth() / 2 - (img.getWidth(null) / 2), 30, this);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
