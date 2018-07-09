package com.github.jakz.retrocompanion.ui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import com.github.jakz.retrocompanion.data.Playlist;
import com.github.jakz.retrocompanion.tasks.PlaylistTask;
import com.github.jakz.retrocompanion.tasks.Tasks;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.io.FolderScanner;
import com.pixbits.lib.ui.FileTransferHandler;

public class PlaylistInfoPanel extends JPanel
{
  private final Mediator mediator;
  private Playlist playlist;
  
  private JLabel nameCaption;
  private JTextField nameField;
  
  private JLabel countLabel;
  
  private ThumbnailBox[] icons;
  private ThumbnailPopupMenu popupMenu;
  
  PlaylistInfoPanel(Mediator mediator)
  {
    this.mediator = mediator;
    
    setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Playlist Details"));
    setLayout(new GridBagLayout());
    
    GridBagHolder c = new GridBagHolder();
    c.w(0.5f, 0.5f);
    
    nameCaption = new JLabel("Name: ");
    nameField = new JTextField(30);
    countLabel = new JLabel("0 entries");
    icons = new ThumbnailBox[] { new ThumbnailBox(120, 10), new ThumbnailBox(120, 10) };
    icons[0].setText("icon");
    icons[1].setText("content icon");
    
    popupMenu = new ThumbnailPopupMenu();
    
    for (int i = 0; i < icons.length; ++i)
    {
      final int ii = i;
      icons[i].setTransferHandler(new FileTransferHandler(new ThumbnailBoxDropListener(
        mediator,
        files -> playlist != null && files.length == 1,
        () -> mediator.options().pathsForPlaylistIcon(playlist.name())[ii],
        () -> setPlaylist(playlist)
      )));
      
      icons[i].addMouseListener(new MouseAdapter() {
        private void handlePopup(MouseEvent e)
        {
          if (e.isPopupTrigger())
          {
            popupMenu.index = ii;
            popupMenu.rebuildList();
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
          }
        }
        
        @Override public void mousePressed(MouseEvent e) { handlePopup(e); }        
        @Override public void mouseReleased(MouseEvent e) { handlePopup(e); }
      });
    }
    
    add(nameCaption, c.g(0, 0).w(1).leftInsets(10).left().c());  
    add(nameField, c.g(1, 0).w(3).center().hfill().c());  
    add(countLabel, c.g(4, 0).left().hInsets(10).c());
    
    c.fill();
    c.i(10).w(1).w(1.0f, 0.5f);
    c.center();
 
    add(icons[0], c.g(1, 1).c());
    add(icons[1], c.g(2, 1).c());
    
    nameField.addActionListener(e -> Tasks.executeTaskUI(mediator, PlaylistTask.RenamePlaylist(nameField.getText())));
  }
  
  public void setPlaylist(Playlist playlist)
  {
    this.playlist = playlist;
    
    if (playlist != null)
    {
      nameField.setText(playlist.name());
      nameField.setEnabled(true);
      countLabel.setText(String.format("%d entries", playlist.size())); //TODO: localize
      
      Path[] iconPaths = mediator.options().pathsForPlaylistIcon(playlist.name());
      icons[0].setImage(iconPaths[0]);
      icons[1].setImage(iconPaths[1]);
    }
    else
    {
      nameField.setText("");
      nameField.setEnabled(false);
      countLabel.setText("");
      icons[0].clearImage();
      icons[1].clearImage();
    }
  }
  
  public void refresh()
  {
    setPlaylist(playlist);
  }
  
  private class ThumbnailPopupMenu extends JPopupMenu
  {
    int index = 0;
    
    JMenu copyFromMenu;
    JMenu[] copyFromMenuSubMenus;
    final ActionListener listener;
    
    ThumbnailPopupMenu()
    {
      copyFromMenu = new JMenu("Copy From...");
      add(copyFromMenu);
      
      listener = e -> {
        JMenuItem item = (JMenuItem)e.getSource();
        String name = item.getText();   
        Tasks.executeTaskUI(mediator, PlaylistTask.ImportIconsFromExisting(name));
        refresh();
      };
      
      JMenuItem deleteFromDisk = new JMenuItem(Strings.DELETE_FROM_DISK.text());
      this.add(deleteFromDisk);
      deleteFromDisk.addActionListener(e -> {
        Tasks.executeTaskUI(mediator, Tasks.DeleteFileFromDisk(() -> mediator.options().pathsForPlaylistIcon(playlist.name())[index]));
        refresh();
      });
      
      JMenuItem showInExplorer = new JMenuItem(Strings.OPEN_IN_FILE_EXPLORER.text());
      this.add(showInExplorer);
      showInExplorer.addActionListener(e -> {
        Tasks.executeTaskUI(mediator, Tasks.OpenFileInExplorer(() -> mediator.options().pathsForPlaylistIcon(playlist.name())[index]));
      });
    }

    void rebuildList()
    {
      try 
      {
        FolderScanner scanner = new FolderScanner(FileSystems.getDefault().getPathMatcher("glob:*.png"), false);
        Set<Path> paths = scanner.scan(mediator.options().themePath);
        
        Set<Path> results = new TreeSet<>();
        
        for (Path path : paths)
        {
          String fileName = path.getFileName().toString();
          
          if (fileName.endsWith("-content.png"))
          {
            Path matchedPath = path.getParent().resolve(fileName.replaceAll("\\-content", ""));
            if (paths.contains(matchedPath))
              results.add(matchedPath);              
          }         
        }
        
        copyFromMenu.removeAll();
        Map<Character, JMenu> submenus = new TreeMap<>();

        results.forEach(path -> {
          String name = FileUtils.fileNameWithoutExtension(path);      
          JMenu subMenu = submenus.computeIfAbsent(name.charAt(0), (p) -> new JMenu(""+name.charAt(0)));
          JMenuItem item = new JMenuItem(name);
          item.addActionListener(listener);
          subMenu.add(item);
        });
        
        submenus.forEach((c,m) -> copyFromMenu.add(m));
      } 
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }
}
