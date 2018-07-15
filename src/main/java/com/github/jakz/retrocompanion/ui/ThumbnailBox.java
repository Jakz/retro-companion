package com.github.jakz.retrocompanion.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.imgscalr.Scalr;

import com.pixbits.lib.lang.Size;

public class ThumbnailBox extends JLabel
{
  private final int size;
  private Size.Int originalSize;
  private final int margin;
 
  private boolean showSize;
  
  public ThumbnailBox(int size, int margin)
  {
    this.size = size;
    this.margin = margin;
    
    setPreferredSize(new Dimension(size + margin, size + margin));
    setOpaque(true);
    setHorizontalTextPosition(JLabel.CENTER);
    setHorizontalAlignment(JLabel.CENTER);
    
    setFont(this.getFont().deriveFont(Font.BOLD));
    setForeground(new Color(255,255,255,200)); 
    setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    
    showSize = true;
  }
  
  public void clearImage()
  {
    setIcon(null);
  }
  
  public void setImage(Path path)
  {
    try 
    {
      if (Files.exists(path))
      {
        BufferedImage image = ImageIO.read(path.toFile());
        originalSize = new Size.Int(image.getWidth(), image.getHeight());
        setIcon(new ImageIcon(Scalr.resize(image, size)));
      }
      else
        clearImage();
      
      repaint();
    } 
    catch (IllegalArgumentException | ImagingOpException | IOException e)
    {
      e.printStackTrace();
    }
  }
  
  final int m = 3;
  
  private void drawBackedString(Graphics g, String string, int x, int y)
  {
    FontMetrics fm = g.getFontMetrics();
    Rectangle2D sb = fm.getStringBounds(string, g);

    g.setColor(new Color(255, 255, 255, 150));
    g.fillRect(x + (int)sb.getX() - m, y + (int)sb.getY() - m, (int)sb.getWidth() + m*2, (int)sb.getHeight() + m*2);
    
    g.setColor(new Color(0, 0, 0, 220));
    g.drawString(string, x, y);     
  }
  
  @Override
  public void paintComponent(Graphics g)
  {
    //super.paintComponent(g);
    
    javax.swing.ImageIcon icon = (ImageIcon)getIcon();
    
    FontMetrics fm = g.getFontMetrics();
    final int m = 3;
    
    final int cw = getWidth(), ch = getHeight();

    g.setColor(getBackground());
    g.fillRect(0, 0, cw, ch);
    
    if (icon != null)
    {
      g.drawImage(icon.getImage(), cw/2 - icon.getIconWidth()/2, ch/2 - icon.getIconHeight()/2, null);
    }
    
    if (showSize && icon != null)
    {
      String sizeString = String.format("%d x %d", originalSize.w, originalSize.h);
      
      Rectangle2D sb = fm.getStringBounds(sizeString, g);
      
      final int x = cw - (int)sb.getWidth() - m - 1;
      final int y = ch - fm.getDescent() - m - 1;
      
      drawBackedString(g, sizeString, x, y);  
    }
    
    if (icon == null)
    {
      String text = getText();
      Rectangle2D sb = fm.getStringBounds(text, g);

      final int x = cw/2 - (int)sb.getWidth()/2;
      final int y = ch/2 - fm.getDescent() + (int)sb.getHeight()/2;
      
      g.setColor(Color.BLACK);
      g.drawString(text, x, y);  
    }
    else
    {
      String text = getText();
      Rectangle2D sb = fm.getStringBounds(text, g);

      final int x = cw/2 - (int)sb.getWidth()/2;
      final int y = + m + fm.getAscent();
      
      drawBackedString(g, text, x, y);  
    }
  }
}
