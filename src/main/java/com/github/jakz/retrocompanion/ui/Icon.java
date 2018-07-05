package com.github.jakz.retrocompanion.ui;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.imgscalr.Scalr;

public enum Icon
{
  SORT_AZ("sort_az"),
  DELETE_SELECTION("delete_selection")
  ;
  
  private final String name;
  private ImageIcon icon;
  
  Icon(String name)
  {
    this.name = name;
  }
  
  public ImageIcon icon()
  {
    if (icon == null)
      icon = new ImageIcon(this.getClass().getClassLoader().getResource("com/github/jakz/retrocompanion/ui/resources/"+name+".png"));
    
    return icon;
  }
  
  public ImageIcon icon(int s)
  {
    if (icon == null)
    {
      try
      {
        BufferedImage image = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("com/github/jakz/retrocompanion/ui/resources/"+name+".png"));
        icon = new ImageIcon(Scalr.resize(image, s));
      }
      catch (Exception e)
      {
        e.printStackTrace();
        icon = new ImageIcon();
      }
    }
    
    return icon;
  }
}
