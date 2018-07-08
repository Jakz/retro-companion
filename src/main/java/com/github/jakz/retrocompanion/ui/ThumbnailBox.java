package com.github.jakz.retrocompanion.ui;

import java.awt.Dimension;
import java.awt.image.ImagingOpException;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.imgscalr.Scalr;

public class ThumbnailBox extends JLabel
{
  private final int size;
  private final int margin;
  
  public ThumbnailBox(int size, int margin)
  {
    this.size = size;
    this.margin = margin;
    
    setPreferredSize(new Dimension(size + margin, size + margin));
    setOpaque(true);
    setHorizontalTextPosition(JLabel.CENTER);
    setHorizontalAlignment(JLabel.CENTER);
  }
  
  public void clearImage()
  {
    setIcon(null);
  }
  
  public void setImage(Path path)
  {
    try 
    {
      setIcon(new ImageIcon(Scalr.resize(ImageIO.read(path.toFile()), size)));
    } 
    catch (IllegalArgumentException | ImagingOpException | IOException e)
    {
      e.printStackTrace();
    }
  }
}
