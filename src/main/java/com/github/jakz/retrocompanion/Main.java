package com.github.jakz.retrocompanion;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.jakz.retrocompanion.playlist.Playlist;
import com.github.jakz.retrocompanion.playlist.PlaylistParser;
import com.github.jakz.retrocompanion.playlist.ThumbnailType;
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
  
  public static void main(String[] args)
  {
    try
    {
      UIUtils.setNimbusLNF();
      
      //loadOptions();

      PlaylistParser parser = new PlaylistParser(options);
      Playlist playlist = parser.parse(Paths.get("F:\\Misc\\Frontends\\Retroarch\\playlists\\NES.lpl"));
      //Playlist playlist = parser.parse(Paths.get("/Volumes/Vicky/Misc/Frontends/Retroarch/playlists/NES.lpl"));
      
      PlaylistTablePanel panel = new PlaylistTablePanel(options);
      WrapperFrame<?> frame = UIUtils.buildFrame(panel, "Playlist");
            
      panel.setPlaylist(playlist);

      frame.exitOnClose();
      frame.centerOnScreen();
      frame.setVisible(true);
      
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
