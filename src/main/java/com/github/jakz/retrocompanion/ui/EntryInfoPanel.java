package com.github.jakz.retrocompanion.ui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.ImagingOpException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;

import org.imgscalr.Scalr;

import com.github.jakz.retrocompanion.Options;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.ThumbnailType;
import com.pixbits.lib.ui.FileTransferHandler;

public class EntryInfoPanel extends JPanel
{
  private final int THUMBNAIL_SIZE = 120;
  private final int THUMBNAIL_MARGIN = 10;
  
  private Mediator mediator;
  
  private Entry entry;
  
  private JLabel[] thumbnails;
  private JLabel entryName;
  private JLabel entryPath;
  
  private final ThumbnailPopupMenu thumbnailPopupMenu;
  
  private ThumbnailType[] enabledThumbnails() { return ThumbnailType.values(); }
  private Path pathForThumbnail(ThumbnailType type) { return entry != null ? mediator.options().pathForThumbnail(entry.playlist(), type, entry) : null; }
  
  public EntryInfoPanel(Mediator mediator)
  {
    this.mediator = mediator;
    
    setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Entry Information"));
    
    thumbnailPopupMenu = new ThumbnailPopupMenu();
    
    entryName = new JLabel();
    entryName.setPreferredSize(new Dimension(400, 20));
    
    entryPath = new JLabel();
    entryPath.setPreferredSize(new Dimension(400, 20));

    thumbnails = new JLabel[enabledThumbnails().length];
    
    setLayout(new GridBagLayout());

    GridBagHolder c = new GridBagHolder();
    
    c.w(0.5f, 0.0f);
    
    c.hfill();

    add(entryName, c.g(0, 0).w(3).leftInsets(10).topLeft().c());
    add(entryPath, c.g(0, 1).w(3).leftInsets(10).topLeft().c());
    
    c.fill();
    c.i(10).w(1).w(1.0f, 0.5f);
    c.center();
    
    for (int i = 0; i < thumbnails.length; ++i)
    {
      final ThumbnailType type = enabledThumbnails()[i];
      
      thumbnails[i] = new JLabel();
      thumbnails[i].setText(ThumbnailType.values()[i].name);
      thumbnails[i].setFont(thumbnails[i].getFont().deriveFont(20.0f));
      thumbnails[i].setOpaque(true);
      thumbnails[i].setForeground(new Color(0, 0, 0, 180));
      thumbnails[i].setBackground(Color.GRAY);
      thumbnails[i].setHorizontalTextPosition(JLabel.CENTER);
      thumbnails[i].setHorizontalAlignment(JLabel.CENTER);
      thumbnails[i].setPreferredSize(new Dimension(THUMBNAIL_SIZE + THUMBNAIL_MARGIN, THUMBNAIL_SIZE + THUMBNAIL_MARGIN));
      
      thumbnails[i].setTransferHandler(new FileTransferHandler(new ThumbnailDragDropListener(type)));
      
      thumbnails[i].addMouseListener(new MouseAdapter() {
        private void handlePopup(MouseEvent e)
        {
          Path path = pathForThumbnail(type);
          if (e.isPopupTrigger() && path != null && Files.exists(path))
          {
            thumbnailPopupMenu.setType(type);
            thumbnailPopupMenu.show(e.getComponent(), e.getX(), e.getY());
          }
        }
        
        @Override public void mousePressed(MouseEvent e) { handlePopup(e); }        
        @Override public void mouseReleased(MouseEvent e) { handlePopup(e); }
      });
      
      add(thumbnails[i], c.g(i, 2).c());
    }    
    
    setEntry(null);
  }
  
  public void setEntry(Entry entry)
  {
    this.entry = entry;
    
    try 
    {
      if (entry != null)
      {
        for (int i = 0; i < enabledThumbnails().length; ++i)
        {
          ThumbnailType type = enabledThumbnails()[i];
          
          Path boxartPath = mediator.options().pathForThumbnail(entry.playlist(), type, entry);
          
          if (Files.exists(boxartPath))
          {
            thumbnails[i].setIcon(new ImageIcon(Scalr.resize(ImageIO.read(boxartPath.toFile()), THUMBNAIL_SIZE)));
            //thumbnails[i].setVerticalTextPosition(JLabel.BOTTOM);
          }
          else
          {
            thumbnails[i].setIcon(null);
            //thumbnails[i].setVerticalTextPosition(JLabel.CENTER);
          }
        }
        
        entryName.setText("Name: "+entry.name());
        entryPath.setText("Path: "+entry.path);

      }
      else
      {
        entryName.setText("Name:");
        entryPath.setText("Path:");
        
        for (JLabel thumbnail : thumbnails)
          thumbnail.setIcon(null);
        
        //for (JLabel thumbnail : thumbnails)
          //thumbnail.setVerticalTextPosition(JLabel.CENTER);

      }
    } 
    catch (IllegalArgumentException | ImagingOpException | IOException e)
    {
      e.printStackTrace();
    }
  }
  
  public Entry entry()
  {
    return entry;
  }
  
  private class ThumbnailDragDropListener implements FileTransferHandler.Listener
  {
    private final ThumbnailType type;
    
    ThumbnailDragDropListener(ThumbnailType type)
    {
      this.type = type;
    }
    
    @Override
    public void filesDropped(TransferHandler.TransferSupport info, Path[] files)
    {
      try
      {
        if (entry != null && files.length == 1)
        {
          Path source = files[0];
        
          if (source.getFileName().toString().toLowerCase().endsWith(".png"))
          {
            Path dest = pathForThumbnail(type);
            
            if (Files.exists(dest))
            {
              if (mediator.options().overwriteThumbnailWithoutConfirmation)
                Files.delete(dest);
              else
              {
                // TODO: ask for confirmation and proceed in case
              }
            }
            
            Files.createDirectories(dest.getParent());
            
            if (mediator.options().thumbnailMoveInsteadThanCopy)
              Files.move(source, dest);
            else
              Files.copy(source, dest);
            
            EntryInfoPanel.this.setEntry(entry);
          }
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();
      } 
    }
  }
  
  private class ThumbnailPopupMenu extends JPopupMenu
  {
    ThumbnailType type;
    
    ThumbnailPopupMenu()
    {
      JMenuItem deleteFromDisk = new JMenuItem(Strings.DELETE_FROM_DISK.text());
      this.add(deleteFromDisk);
      
      deleteFromDisk.addActionListener(event -> {
        try
        {
          Path path = pathForThumbnail(type);
          
          //TODO: confirmation?
          if (path != null && Files.exists(path))
            Files.delete(path);
          
          EntryInfoPanel.this.setEntry(entry);
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      });
      
      JMenuItem showInExplorer = new JMenuItem(Strings.OPEN_IN_FILE_EXPLORER.text());
      this.add(showInExplorer);
      
      showInExplorer.addActionListener(event -> {
        try
        {
          Path path = pathForThumbnail(type);
        
          //TODO: highlight the file
          if (path != null && Files.exists(path))
            Desktop.getDesktop().open(path.getParent().toFile());
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      });
    }
    
    void setType(ThumbnailType type) { this.type = type; }
  }
}
