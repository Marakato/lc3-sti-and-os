package jkw_lc3;

import java.awt.Color;
import java.awt.Font;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.*;



public class LC3GUI extends JFrame {
	//public static LC3GUI window = new LC3GUI();
	
	public boolean keydown;
	public char inputchar;
	
	private JTextArea screen;
	private JPanel panel;
	
	private StringBuffer str;
	
	private class MyKeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			inputchar=e.getKeyChar();
			if(inputchar<128) {
				keydown=true;
			}
		}
	}
	
	
	public LC3GUI() {
		super("LC3");
		
		inputchar='\0';
		keydown=false;
		
		str=new StringBuffer();
		
		setSize(850,445);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setResizable(false);
		setLayout(null);
		
		
		panel=new JPanel();
		panel.setBounds(0,0,850,416);
		panel.setBackground(Color.GREEN);
		add(panel);
		
		
	
		screen=new JTextArea(13,60);
		StringBuilder x=new StringBuilder();
		x.append((char)(23435));
		x.append((char)(20307)); 
		// x represents songti. This prevents difference in ASCII or UTF-8 in source file
		screen.setFont(new Font(x.toString(),1,26));
		
		panel.add(new JScrollPane(screen));
		screen.setLineWrap(true);
		screen.setBackground(Color.BLACK);
		screen.setForeground(Color.WHITE);
		screen.setEnabled(false);
		
		
		addKeyListener(new MyKeyListener());
		
		setVisible(true);
	}
	
	public void printc(char c) {
		if(c=='\b') {
			if(str.length()!=0)
				str=str.deleteCharAt(str.length()-1);
		}
		else {
			str=str.append(c);
		}
		screen.setText(str.toString());
		screen.setCaretPosition(screen.getDocument().getLength());
	}
	public void printstr(String s) {
		for(int i=0;i<s.length();++i) {
			printc(s.charAt(i));
		}
	}
	public void printstr(StringBuilder s) {
		for(int i=0;i<s.length();++i) {
			printc(s.charAt(i));
		}
	}
	public void backspace() {
		str=str.deleteCharAt(str.length()-1);
	}
	public void cls() {
		str.setLength(0);
		screen.setText(str.toString());
	}
}
