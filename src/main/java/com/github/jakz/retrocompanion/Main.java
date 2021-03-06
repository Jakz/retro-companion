package com.github.jakz.retrocompanion;

import java.awt.Container;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.SwingUtilities;

import com.github.jakz.retrocompanion.data.CoreSet;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.Playlist;
import com.github.jakz.retrocompanion.parsers.CoreParser;
import com.github.jakz.retrocompanion.tasks.TaskException;
import com.github.jakz.retrocompanion.tasks.Tasks;
import com.github.jakz.retrocompanion.ui.EntryInfoPanel;
import com.github.jakz.retrocompanion.ui.ExceptionPanel;
import com.github.jakz.retrocompanion.ui.MainPanel;
import com.github.jakz.retrocompanion.ui.Mediator;
import com.github.jakz.retrocompanion.ui.OptionsPanel;
import com.github.jakz.retrocompanion.ui.PlaylistInfoPanel;
import com.github.jakz.retrocompanion.ui.PlaylistTablePanel;
import com.pixbits.lib.log.ProgressLogger;
import com.pixbits.lib.ui.UIUtils;
import com.pixbits.lib.ui.WrapperFrame;
import com.pixbits.lib.ui.elements.ProgressDialog;
import com.pixbits.lib.util.ShutdownManager;

import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

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
  
  public static void initZipLibrary()
  {
    try
    {
      if (!SevenZip.isInitializedSuccessfully())
        SevenZip.initSevenZipFromPlatformJAR();
    } 
    catch (SevenZipNativeInitializationException e)
    {
      e.printStackTrace();
    }
  }
  
  private static ShutdownManager manager;
  
  private static final List<Playlist> playlists = new ArrayList<>();

  private static WrapperFrame<MainPanel> mainFrame;
  private static WrapperFrame<OptionsPanel> optionsFrame;
  private static WrapperFrame<ExceptionPanel> exceptionFrame;
  
  private static MainPanel mainPanel;

  private static EntryInfoPanel entryInfoPanel;
  private static PlaylistTablePanel playlistPanel;
  private static PlaylistInfoPanel playlistInfoPanel;
  
  private static ProgressDialog.Manager progress;
  
  private static class MyMediator implements Mediator
  {
    @Override
    public void scanAndLoadPlaylists()
    {
      if (Files.exists(options().playlistsPath))
      {
        List<Playlist> playlists = Tasks.Standalone.loadPlaylistsFromFolder(options().playlistsPath, options);
        playlists.sort((p1, p2) -> p1.path().compareTo(p2.path()));
        playlists.forEach(p -> p.cacheSize());
        
        Main.playlists.clear();
        Main.playlists.addAll(playlists);
        
        mainPanel.playlistChooser.removeAllItems();
        Main.playlists.forEach(mainPanel.playlistChooser::addItem);
        mainPanel.toolbar.updateSummaryLabel(this);
      }
    }
    
    @Override
    public void refreshPlaylistMetadata()
    {
      playlist().cacheSize();
      playlistInfoPanel.refresh();
      mainPanel.playlistChooser.repaint();
      mainPanel.toolbar.updateSummaryLabel(this);
    }
    
    @Override
    public void repaintPlaylistTable()
    {
      playlistPanel.repaint();
    }
    
    @Override
    public void refreshPlaylist()
    {
      playlistPanel.refresh();
      entryInfoPanel.setEntry(null);
      refreshPlaylistMetadata();
      mainPanel.toolbar.updateSummaryLabel(this);
    }
    
    @Override
    public void addPlaylist(Playlist playlist)
    {
      playlists.add(playlist);
      playlists.sort((p1, p2) -> p1.path().compareTo(p2.path()));
      
      mainPanel.playlistChooser.removeAllItems();
      Main.playlists.forEach(mainPanel.playlistChooser::addItem);
      mainPanel.toolbar.updateSummaryLabel(this);

    }
    
    @Override
    public void removePlaylist(Playlist playlist)
    {
      playlists.remove(playlist);
      
      boolean wasSelected = mainPanel.playlistChooser.getSelectedItem() == playlist;
      int index = mainPanel.playlistChooser.getSelectedIndex();

      mainPanel.playlistChooser.removeItem(playlist);
      
      if (wasSelected && mainPanel.playlistChooser.getItemCount() > 0)
        mainPanel.playlistChooser.setSelectedIndex(index);
      
      mainPanel.toolbar.updateSummaryLabel(this);
    }
    
    
    @Override
    public void onEntrySelected(Entry entry)
    {
      entryInfoPanel.setEntry(entry);
    }
    
    @Override
    public void selectPlaylist(Playlist playlist)
    {
      mainPanel.playlistChooser.setSelectedItem(playlist);
      onPlaylistSelected(playlist);
    }
    
    @Override
    public void onPlaylistSelected(Playlist playlist)
    {
      playlistPanel.setPlaylist(playlist);
      mainPanel.playlistChooser.repaint();
      entryInfoPanel.setEntry(null);
      playlistInfoPanel.setPlaylist(playlist);
      
      if (playlist != null)
        options.lastSelectedPlaylist = playlist.path();
    }
    
    @Override
    public void removeEntriesFromPlaylist(List<Entry> entries)
    {
      Playlist playlist = playlist();
      
      entries.stream()
        .map(playlist::indexOf)
        .filter(i -> i != -1)
        .forEach(playlist::remove);
      
      if (!entries.isEmpty())
        playlist.markDirty();
  
      refreshPlaylist();
    }
    
    @Override
    public void selectEntry(Entry entry)
    {
      if (playlistPanel.selectEntry(entry));
        onEntrySelected(entry);
    }
    
    @Override
    public List<Entry> getSelectedEntries()
    {
      return playlistPanel.getSelectedEntries();
    }
    
    @Override
    public Playlist playlist()
    {
      return playlistPanel.playlist();
    }
    
    @Override
    public Entry entry()
    {
      return entryInfoPanel.entry();
    }
    
    @Override
    public Options options()
    {
      return options;
    }
    
    @Override
    public List<Playlist> playlists()
    {
      return playlists;
    }
    
    @Override
    public Container modalTarget()
    {
      return mainPanel;
    }
    
    @Override
    public ProgressDialog.Manager progress()
    {
      return progress;
    }
    
    @Override
    public void showOptions()
    {
      optionsFrame.setLocationRelativeTo(mainFrame);
      
      if (!optionsFrame.isVisible())
      {
        optionsFrame.panel().refresh();
        optionsFrame.setVisible(true);
      }
      else
        optionsFrame.toFront();
    }
    
    @Override
    public void handleException(TaskException exception)
    {
      if (exception.getCause() != null)
      {
        exceptionFrame.setLocationRelativeTo(mainFrame);
        SwingUtilities.invokeLater(() -> {
          exceptionFrame.panel().exceptionThrown(exception);
          exceptionFrame.setVisible(true);
        });
      }
      else
        UIUtils.showErrorDialog(modalTarget(), "Error", exception.dialogMessage);
    }
    
  }
  
  public static void main(String[] args)
  {
    try
    {
      loadOptions();
      manager = new ShutdownManager(true);
      manager.addTask(() -> saveOptions());
      
      MyMediator mediator = new MyMediator();

      UIUtils.setNimbusLNF();
      
      CoreSet cores = new CoreParser().parse(options);
      options.cores = cores;
     
      /*CoreTablePanel coresPanel = new CoreTablePanel(mediator);
      coresPanel.setCores(cores);
      
      WrapperFrame<?> coresFrame = UIUtils.buildFrame(coresPanel, "Cores");

      coresFrame.exitOnClose();
      coresFrame.centerOnScreen();
      coresFrame.setVisible(true);*/
      
      //PlaylistParser parser = new PlaylistParser(options);
      //Playlist playlist = parser.parse(Paths.get("F:\\Misc\\Frontends\\Retroarch\\playlists\\NES.lpl"));
      //Playlist playlist = parser.parse(Paths.get("/Volumes/Vicky/Misc/Frontends/Retroarch/playlists/NES.lpl"));
      
      optionsFrame = UIUtils.buildFrame(new OptionsPanel(mediator), "Options"); 

      mainPanel = new MainPanel(mediator);
      Main.playlistPanel = mainPanel.playlistPanel;
      Main.entryInfoPanel = mainPanel.entryInfoPanel;
      Main.playlistInfoPanel = mainPanel.playlistInfoPanel;
      
      mainFrame = UIUtils.buildFrame(mainPanel, "RetroCompanion v0.1");
      mainFrame.exitOnClose();
      mainFrame.centerOnScreen();
      mainFrame.setVisible(true);
      
      progress = new ProgressDialog.Manager(mainFrame);
      
      exceptionFrame = UIUtils.buildFrame(new ExceptionPanel(), "Error");
      
      Path lastSelectedPlaylist = options.lastSelectedPlaylist;
      mediator.scanAndLoadPlaylists();
      
      if (!playlists.isEmpty())
      {
        Optional<Playlist> playlist = playlists.stream()
          .filter(p -> p.path().equals(lastSelectedPlaylist))
          .findFirst();
        
        mediator.selectPlaylist(playlist.orElse(playlists.get(0)));      
      }
    } 
    catch (IOException e)
    {
      e.printStackTrace();
    }
    
    if (true)
      return;
  }
}
