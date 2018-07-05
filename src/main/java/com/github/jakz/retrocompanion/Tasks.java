package com.github.jakz.retrocompanion;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.jakz.retrocompanion.data.Core;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.Playlist;
import com.github.jakz.retrocompanion.ui.Mediator;
import com.github.jakz.retrocompanion.ui.Toolbar;
import com.pixbits.lib.ui.UIUtils;

public class Tasks
{
  public static class Standalone
  {
    public static void sortPlaylistAlphabetically(Playlist playlist)
    {
      List<Entry> entries = playlist.stream()
          .sorted((e1, e2) -> e1.name().compareToIgnoreCase(e2.name()))
          .collect(Collectors.toList());
      
      playlist.clear();
      entries.forEach(playlist::add);
    }
    
    public static void makePathsRelative(Playlist playlist, Path path)
    {
      playlist.stream().filter(e -> e.path.isAbsolute()).forEach(entry -> entry.path = path.relativize(entry.path).normalize());
    }
    
    public static void makePathsAbsolute(Playlist playlist, Path path)
    {
      playlist.stream().forEach(entry -> entry.path = path.resolve(entry.path).toAbsolutePath().normalize());
    }
    
    public static void save(Playlist playlist)
    {
      playlist.save(playlist.path());
    }
  }
  

  
  public static void removeSelectedEntriesFromPlaylist(Mediator mediator)
  {
    Playlist playlist = mediator.playlist();
    List<Entry> entries = mediator.getSelectedEntries();
    
    if (playlist != null && !entries.isEmpty())
    {
      //TODO: localize
      boolean confirmed = !mediator.options().showConfirmationDialogForUndoableOperations || UIUtils.showConfirmDialog(
          mediator.modalTarget(),
          "Warning",
          "This can't be undone, are you sure you want to proceed?"
      );
      
      if (confirmed)
        mediator.removeEntriesFromPlaylist(entries);
    }
    
  }
  
  public static void addEntryToPlaylist(Mediator mediator)
  {
    Playlist playlist = mediator.playlist();
    List<Entry> entries = mediator.getSelectedEntries();
    
    if (playlist != null)
    {
      int index = entries.stream()
        .map(playlist::indexOf)
        .max(Integer::compare)
        .orElse(playlist.size()-1);
      
      Entry entry = new Entry(playlist, Paths.get("locate.me"), "Name", Optional.empty());
      playlist.add(index, entry);
      mediator.refreshPlaylist();
      mediator.selectEntry(entry);
    }
  }
    
  public static void sortPlaylistAlphabetically(Mediator mediator)
  {
    Playlist playlist = mediator.playlist();
    
    if (playlist != null)
    {
      //TODO: localize
      boolean confirmed = !mediator.options().showConfirmationDialogForUndoableOperations || UIUtils.showConfirmDialog(
          mediator.modalTarget(),
          "Warning",
          "Sorting the playlist can't be undone, are you sure you want to proceed?"
      );
      
      if (confirmed)
      {                 
        Standalone.sortPlaylistAlphabetically(playlist);      
        mediator.refreshPlaylist();
      }
    }
  }
  
  public static void relativizePathsToRetroarch(Mediator mediator)
  {
    Playlist playlist = mediator.playlist();
    Path retroarchPath = mediator.options().retroarchPath;
    
    if (Files.exists(retroarchPath))
    {
      if (playlist != null)
        Standalone.makePathsRelative(playlist, retroarchPath);
      
      mediator.refreshPlaylist();
    }
    else
      UIUtils.showErrorDialog(mediator.modalTarget(), "Error", "Retroarch path doesn't exist");
  }
  
  public static void makePathsAbsolute(Mediator mediator)
  {
    Playlist playlist = mediator.playlist();
    Path retroarchPath = mediator.options().retroarchPath;
    
    if (Files.exists(retroarchPath))
    {
      if (playlist != null)
        Standalone.makePathsAbsolute(playlist, retroarchPath);
      
      mediator.refreshPlaylist();
    }
    else
      UIUtils.showErrorDialog(mediator.modalTarget(), "Error", "Retroarch path doesn't exist");
  }
}
