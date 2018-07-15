package com.github.jakz.retrocompanion.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import com.github.jakz.retrocompanion.tasks.TaskException;

public class ExceptionPanel extends JPanel
{
  private JLabel icon;
  private JLabel title;
  private JTextArea area;
  
  public ExceptionPanel()
  {
    setLayout(new GridBagLayout());
    setPreferredSize(new Dimension(1000, 400));
    
    GridBagHolder c = new GridBagHolder();
    c.w(0.5f, 0.5f);
    c.top();
    
    icon = new JLabel(UIManager.getIcon("OptionPane.errorIcon"));
    add(icon, c.g(0, 0).leftInsets(10).w(1).c());
    
    title = new JLabel();
    add(title, c.g(1, 0).left().noInsets().w(4).c());
    
    area = new JTextArea(10,60);
    area.setFont(new Font("consolas", Font.PLAIN, 12));
    area.setEditable(false);
    area.setBackground(Color.BLACK);
    area.setForeground(new Color(180,0,0));
    add(new JScrollPane(area), c.g(0, 1).w(1.0f, 0.9f).center().insets(10).fill().w(5).c());
  }
  
  public void exceptionThrown(TaskException exception)
  {
    title.setText(exception.dialogMessage);
    
    Throwable throwable = exception.getCause();   
    
    StringBuilder text = new StringBuilder();
    
    text.append(throwable.toString()).append("\n");
    
    throwable = throwable.getCause();
    if (throwable != null)
      text.append("Caused by ").append(throwable.toString()).append("\n");
    
    StackTraceElement[] stack = throwable.getStackTrace();
    for (StackTraceElement element : stack)
      text.append("  ").append(element.toString()).append("\n");
    
    area.setText(text.toString());
    area.setCaretPosition(0);
  }
}
