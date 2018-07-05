package com.github.jakz.retrocompanion;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.github.jakz.retrocompanion.data.Core;
import com.github.jakz.retrocompanion.data.CoreSet;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.Playlist;
import com.github.jakz.retrocompanion.data.ThumbnailType;
import com.github.jakz.retrocompanion.parsers.CoreParser;
import com.github.jakz.retrocompanion.parsers.PlaylistParser;
import com.github.jakz.retrocompanion.ui.CoreTablePanel;
import com.github.jakz.retrocompanion.ui.EntryInfoPanel;
import com.github.jakz.retrocompanion.ui.Mediator;
import com.github.jakz.retrocompanion.ui.PathsPanel;
import com.github.jakz.retrocompanion.ui.PlaylistTablePanel;
import com.pixbits.lib.ui.UIUtils;
import com.pixbits.lib.ui.WrapperFrame;

public class Main 
{  
  public static final Path OPTIONS_PATH = Paths.get("options.json");
  
  public static Options options = new Options();
  
  public static boolean loadOptions()
  {
    if (Files.exists(OPTIONS_PATH))
    {
      try
      {
        options.load(OPTIONS_PATH);
      } 
      catch (IOException e)
      {
        e.printStackTrace();
        return false;
      }
      
      return true;
    }
    
    return false;
  }
  
  public static void saveOptions()
  {
    try
    {
      options.save(OPTIONS_PATH);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  private static EntryInfoPanel entryInfoPanel;
  
  private static class MyMediator implements Mediator
  {

    @Override
    public void onEntrySelected(Entry entry)
    {
      entryInfoPanel.setEntry(entry);
    }
    
  }
  
  public static void main(String[] args)
  {
    try
    {
      MyMediator mediator = new MyMediator();
      
      UIUtils.setNimbusLNF();
      
      CoreSet cores = new CoreParser().parse(options);
      options.cores = cores;
      CoreTablePanel coresPanel = new CoreTablePanel(options);
      coresPanel.setCores(cores);
      
      WrapperFrame<?> coresFrame = UIUtils.buildFrame(coresPanel, "Cores");

      coresFrame.exitOnClose();
      coresFrame.centerOnScreen();
      coresFrame.setVisible(true);
      
      PlaylistParser parser = new PlaylistParser(options);
      Playlist playlist = parser.parse(Paths.get("F:\\Misc\\Frontends\\Retroarch\\playlists\\NES.lpl"));
      //Playlist playlist = parser.parse(Paths.get("/Volumes/Vicky/Misc/Frontends/Retroarch/playlists/NES.lpl"));
      
      PlaylistTablePanel playlistPanel = new PlaylistTablePanel(mediator, options);
      WrapperFrame<?> playlistFrame = UIUtils.buildFrame(playlistPanel, "Playlist");
      
      playlistPanel.setPlaylist(playlist);

      playlistFrame.exitOnClose();
      playlistFrame.centerOnScreen();
      playlistFrame.setVisible(true);
      
      {
        entryInfoPanel = new EntryInfoPanel(options);
        WrapperFrame<?> entryInfoFrame = UIUtils.buildFrame(entryInfoPanel, "Entry Info");
        
        entryInfoFrame.centerOnScreen();
        entryInfoFrame.setVisible(true);
      }
      
      if (true)
        return;

      playlist.save(Paths.get("test.lpl"));
    } 
    catch (IOException e)
    {
      e.printStackTrace();
    }
    
    if (true)
      return;
    
    
    WrapperFrame<?> frame = UIUtils.buildFrame(new PathsPanel(options), "Paths");

    frame.exitOnClose();
    frame.centerOnScreen();
    frame.setVisible(true);

  }
}
