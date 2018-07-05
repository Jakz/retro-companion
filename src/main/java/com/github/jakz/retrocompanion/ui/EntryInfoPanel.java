package com.github.jakz.retrocompanion.ui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.image.ImagingOpException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.imgscalr.Scalr;

import com.github.jakz.retrocompanion.Options;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.ThumbnailType;

public class EntryInfoPanel extends JPanel
{
  private Options options;
  private Entry entry;
  
  private JLabel boxart;
  
  public EntryInfoPanel(Options options)
  {
    this.options = options;
    
    boxart = new JLabel();
    boxart.setHorizontalAlignment(JLabel.CENTER);
    boxart.setBorder(BorderFactory.createTitledBorder("Boxart"));
    boxart.setPreferredSize(new Dimension(240,240));

    setLayout(new GridBagLayout());
    
    GridBagHolder c = new GridBagHolder();
    c.w(0.5f, 0.5f);
    
    add(boxart, c.g(0, 0).center().c());
  }
  
  public void setEntry(Entry entry)
  {
    this.entry = entry;
    
    try 
    {
      if (entry != null)
      {
        Path boxartPath = options.pathForThumbnail(entry.playlist(), ThumbnailType.BOXART, entry);
      
        if (Files.exists(boxartPath))
          boxart.setIcon(new ImageIcon(Scalr.resize(ImageIO.read(boxartPath.toFile()), 200)));
        else
          boxart.setIcon(null);
      }
    } 
    catch (IllegalArgumentException | ImagingOpException | IOException e)
    {
      e.printStackTrace();
    }
  }
}
