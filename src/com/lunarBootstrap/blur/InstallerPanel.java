/*
 * Decompiled with CFR 0_101.
 */
package com.lunarBootstrap.blur;

import com.google.common.base.Throwables;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class InstallerPanel
extends JPanel {
    private File targetDir;
    private ButtonGroup choiceButtonGroup;
    private JTextField selectedDirText;
    private JLabel infoLabel;
    private JButton sponsorButton;
    private JDialog dialog;
    private JPanel sponsorPanel;
    private JPanel fileEntryPanel;

    public InstallerPanel(File targetDir) {
        BufferedImage image;
        this.setLayout(new BoxLayout(this, 1));
        try {
            image = ImageIO.read(SimpleInstaller.class.getResourceAsStream(VersionInfo.getLogoFileName()));
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
        JPanel logoSplash = new JPanel();
        logoSplash.setLayout(new BoxLayout(logoSplash, 1));
        ImageIcon icon = new ImageIcon(image);
        JLabel logoLabel = new JLabel(icon);
        logoLabel.setAlignmentX(0.5f);
        logoLabel.setAlignmentY(0.5f);
        logoLabel.setSize(image.getWidth(), image.getHeight());
        logoSplash.add(logoLabel);
        JLabel tag = new JLabel(VersionInfo.getWelcomeMessage());
        tag.setAlignmentX(0.5f);
        tag.setAlignmentY(0.5f);
        logoSplash.add(tag);
        tag = new JLabel(VersionInfo.getVersion());
        tag.setAlignmentX(0.5f);
        tag.setAlignmentY(0.5f);
        logoSplash.add(tag);
        logoSplash.setAlignmentX(0.5f);
        logoSplash.setAlignmentY(0.0f);
        this.add(logoSplash);
        this.sponsorPanel = new JPanel();
        this.sponsorPanel.setLayout(new BoxLayout(this.sponsorPanel, 0));
        this.sponsorPanel.setAlignmentX(0.5f);
        this.sponsorPanel.setAlignmentY(0.5f);
        this.sponsorButton = new JButton();
        this.sponsorButton.setAlignmentX(0.5f);
        this.sponsorButton.setAlignmentY(0.5f);
        this.sponsorButton.setBorderPainted(false);
        this.sponsorButton.setOpaque(false);
        this.sponsorButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(InstallerPanel.this.sponsorButton.getToolTipText()));
                    EventQueue.invokeLater(new Runnable(){

                        @Override
                        public void run() {
                            InstallerPanel.this.dialog.toFront();
                            InstallerPanel.this.dialog.requestFocus();
                        }
                    });
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(InstallerPanel.this, "An error occurred launching the browser", "Error launching browser", 0);
                }
            }

        });
        this.sponsorPanel.add(this.sponsorButton);
        this.add(this.sponsorPanel);
        this.choiceButtonGroup = new ButtonGroup();
        JPanel choicePanel = new JPanel();
        choicePanel.setLayout(new BoxLayout(choicePanel, 1));
        boolean first = true;
        SelectButtonAction sba = new SelectButtonAction();
        for (InstallerAction action : InstallerAction.values()) {
            if (action == InstallerAction.CLIENT && VersionInfo.hideClient()) continue;
            if (action == InstallerAction.SERVER && VersionInfo.hideServer()) continue;
            JRadioButton radioButton = new JRadioButton();
            radioButton.setAction(sba);
            radioButton.setText(action.getButtonLabel());
            radioButton.setActionCommand(action.name());
            radioButton.setToolTipText(action.getTooltip());
            radioButton.setSelected(first);
            radioButton.setAlignmentX(0.0f);
            radioButton.setAlignmentY(0.5f);
            this.choiceButtonGroup.add(radioButton);
            choicePanel.add(radioButton);
            first = false;
        }
        choicePanel.setAlignmentX(1.0f);
        choicePanel.setAlignmentY(0.5f);
        this.add(choicePanel);
        JPanel entryPanel = new JPanel();
        entryPanel.setLayout(new BoxLayout(entryPanel, 0));
        this.targetDir = targetDir;
        this.selectedDirText = new JTextField();
        this.selectedDirText.setEditable(false);
        this.selectedDirText.setToolTipText("Path to minecraft");
        this.selectedDirText.setColumns(30);
        entryPanel.add(this.selectedDirText);
        JButton dirSelect = new JButton();
        dirSelect.setAction(new FileSelectAction());
        dirSelect.setText("...");
        dirSelect.setToolTipText("Select an alternative minecraft directory");
        entryPanel.add(dirSelect);
        entryPanel.setAlignmentX(0.0f);
        entryPanel.setAlignmentY(0.0f);
        this.infoLabel = new JLabel();
        this.infoLabel.setHorizontalTextPosition(2);
        this.infoLabel.setVerticalTextPosition(1);
        this.infoLabel.setAlignmentX(0.0f);
        this.infoLabel.setAlignmentY(0.0f);
        this.infoLabel.setForeground(Color.RED);
        this.infoLabel.setVisible(false);
        this.fileEntryPanel = new JPanel();
        this.fileEntryPanel.setLayout(new BoxLayout(this.fileEntryPanel, 1));
        this.fileEntryPanel.add(this.infoLabel);
        this.fileEntryPanel.add(Box.createVerticalGlue());
        this.fileEntryPanel.add(entryPanel);
        this.fileEntryPanel.setAlignmentX(0.5f);
        this.fileEntryPanel.setAlignmentY(0.0f);
        this.add(this.fileEntryPanel);
        this.updateFilePath();
    }

    private void updateFilePath() {
        try {
            this.targetDir = this.targetDir.getCanonicalFile();
            this.selectedDirText.setText(this.targetDir.getPath());
        }
        catch (IOException e) {
            // empty catch block
        }
        InstallerAction action = InstallerAction.valueOf(this.choiceButtonGroup.getSelection().getActionCommand());
        boolean valid = action.isPathValid(this.targetDir);
        String sponsorMessage = action.getSponsorMessage();
        if (sponsorMessage != null) {
            this.sponsorButton.setText(sponsorMessage);
            this.sponsorButton.setToolTipText(action.getSponsorURL());
            if (action.getSponsorLogo() != null) {
                this.sponsorButton.setIcon(action.getSponsorLogo());
            } else {
                this.sponsorButton.setIcon(null);
            }
            this.sponsorPanel.setVisible(true);
        } else {
            this.sponsorPanel.setVisible(false);
        }
        if (valid) {
            this.selectedDirText.setForeground(Color.BLACK);
            this.infoLabel.setVisible(false);
            this.fileEntryPanel.setBorder(null);
        } else {
            this.selectedDirText.setForeground(Color.RED);
            this.fileEntryPanel.setBorder(new LineBorder(Color.RED));
            this.infoLabel.setText("<html>" + action.getFileError(this.targetDir) + "</html>");
            this.infoLabel.setVisible(true);
        }
        if (this.dialog != null) {
            this.dialog.invalidate();
            this.dialog.pack();
        }
    }

    public void run() {
        InstallerAction action;
        JOptionPane optionPane = new JOptionPane(this, -1, 2);
        Frame emptyFrame = new Frame("Mod system installer");
        emptyFrame.setUndecorated(true);
        emptyFrame.setVisible(true);
        emptyFrame.setLocationRelativeTo(null);
        this.dialog = optionPane.createDialog(emptyFrame, "Mod system installer");
        this.dialog.setDefaultCloseOperation(2);
        this.dialog.setVisible(true);
        int result = (Integer)(optionPane.getValue() != null ? optionPane.getValue() : Integer.valueOf(-1));
        if (result == 0 && (action = InstallerAction.valueOf(this.choiceButtonGroup.getSelection().getActionCommand())).run(this.targetDir)) {
            JOptionPane.showMessageDialog(null, action.getSuccessMessage(), "Complete", 1);
        }
        this.dialog.dispose();
        emptyFrame.dispose();
    }

    private class SelectButtonAction
    extends AbstractAction {
        private SelectButtonAction() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            InstallerPanel.this.updateFilePath();
        }
    }

    private class FileSelectAction
    extends AbstractAction {
        private FileSelectAction() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setFileSelectionMode(1);
            dirChooser.setFileHidingEnabled(false);
            dirChooser.ensureFileIsVisible(InstallerPanel.this.targetDir);
            dirChooser.setSelectedFile(InstallerPanel.this.targetDir);
            int response = dirChooser.showOpenDialog(InstallerPanel.this);
            switch (response) {
                case 0: {
                    InstallerPanel.this.targetDir = dirChooser.getSelectedFile();
                    InstallerPanel.this.updateFilePath();
                    break;
                }
            }
        }
    }

}

