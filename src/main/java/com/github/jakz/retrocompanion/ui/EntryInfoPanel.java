package com.github.jakz.retrocompanion.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.ThumbnailType;
import com.github.jakz.retrocompanion.tasks.Tasks;
import com.pixbits.lib.ui.FileTransferHandler;

public class EntryInfoPanel extends JPanel
{
  private Mediator mediator;
  
  private Entry entry;
  
  private ThumbnailBox[] thumbnails;
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

    thumbnails = new ThumbnailBox[enabledThumbnails().length];
    
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
      
      thumbnails[i] = new ThumbnailBox(120, 10);
      thumbnails[i].setText(ThumbnailType.values()[i].name);
      
      thumbnails[i].setTransferHandler(new FileTransferHandler(new ThumbnailBoxDropListener(
          mediator,
          files -> entry != null && files.length == 1,
          () -> pathForThumbnail(type), 
          () -> setEntry(entry)
      )));
      
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

    if (entry != null)
    {
      for (int i = 0; i < enabledThumbnails().length; ++i)
      {
        ThumbnailType type = enabledThumbnails()[i];
        
        Path boxartPath = mediator.options().pathForThumbnail(entry.playlist(), type, entry);
        thumbnails[i].setImage(boxartPath);
      }
      
      entryName.setText("Name: "+entry.name());
      entryPath.setText("Path: "+entry.path());

    }
    else
    {
      entryName.setText("Name:");
      entryPath.setText("Path:");
      
      for (ThumbnailBox thumbnail : thumbnails)
        thumbnail.clearImage();
      
      //for (JLabel thumbnail : thumbnails)
        //thumbnail.setVerticalTextPosition(JLabel.CENTER);

    }
  }
  
  public Entry entry()
  {
    return entry;
  }
  
  private class ThumbnailPopupMenu extends JPopupMenu
  {
    ThumbnailType type;
    
    ThumbnailPopupMenu()
    {
      JMenuItem deleteFromDisk = new JMenuItem(Strings.DELETE_FROM_DISK.text());
      this.add(deleteFromDisk);
      deleteFromDisk.addActionListener(e -> {
        Tasks.executeTaskUI(mediator, Tasks.DeleteFileFromDisk(() -> pathForThumbnail(type)));
        setEntry(entry);
      });
      
      JMenuItem showInExplorer = new JMenuItem(Strings.OPEN_IN_FILE_EXPLORER.text());
      this.add(showInExplorer);
      showInExplorer.addActionListener(e -> Tasks.executeTaskUI(mediator, Tasks.OpenFileInExplorer(() -> pathForThumbnail(type))));
    }
    
    void setType(ThumbnailType type) { this.type = type; }
  }
}
