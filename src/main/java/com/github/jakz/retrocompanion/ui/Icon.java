package com.github.jakz.retrocompanion.ui;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.imgscalr.Scalr;

public enum Icon
{
  
  DELETE_ENTRY("delete_entry"),
  ADD_ENTRY("add_entry"),
  
  SORT_AZ("sort_az"),
  RELATIVIZE("relativize"),
  MAKE_ABSOLUTE("make_absolute"),
  REMOVE_TAGS("remove_tags"),
  RENAME_TO_FILENAME("rename_to_filename"),
  SET_CORE("set_core"),
  
  SAVE("save"),
  NEW_PLAYLIST("new"),
  DELETE_PLAYLIST("delete")
  
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
