package com.github.jakz.retrocompanion;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jakz.retrocompanion.data.Core;
import com.github.jakz.retrocompanion.data.CoreSet;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.Playlist;
import com.github.jakz.retrocompanion.parsers.PlaylistParser;
import com.github.jakz.retrocompanion.ui.Mediator;
import com.github.jakz.retrocompanion.ui.Toolbar;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.io.FolderScanner;
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
      playlist.stream().forEach(entry -> entry.relativizePath(path));
    }
    
    public static void makePathsAbsolute(Playlist playlist, Path path)
    {
      playlist.stream().forEach(entry -> entry.makeAbsolutePath(path));
    }
    
    public static void save(Playlist playlist)
    {
      try
      {
        playlist.save(playlist.path());
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    
    public static List<Playlist> loadPlaylistsFromFolder(Path folder, Options options)
    {
      try
      {
        FolderScanner scanner = new FolderScanner(FileSystems.getDefault().getPathMatcher("glob:*.lpl"), false);
        
        Set<Path> playlists = scanner.scan(folder);
        PlaylistParser parser = new PlaylistParser(options);
        
        return playlists.stream().map(parser::parse).collect(Collectors.toList());
      }
      catch (IOException e)
      {
        e.printStackTrace();
        return Collections.emptyList();
      }
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
        .orElse(playlist.size());
      
      Entry entry = new Entry(playlist, Paths.get("locate.me"), "Name", Optional.empty(), Optional.empty());
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
  
  public static void removeAllTagsFromEntryNames(Mediator mediator)
  {
    Playlist playlist = mediator.playlist();

    if (playlist != null)
    {
      for (Entry entry : playlist)
      {
        String name = entry.name();
        int firstTagIndex = name.indexOf('(');
        
        if (firstTagIndex != -1 && firstTagIndex > 0)
        {
          String newName = name.substring(0, firstTagIndex-1);
          
          entry.rename(newName, mediator.options());
        }
      }
      
      Entry selectedEntry = mediator.entry();
      mediator.refreshPlaylist();
      mediator.selectEntry(selectedEntry);
    }
  }
  
  public static void renameEntriesToMatchFilename(Mediator mediator)
  {
    Playlist playlist = mediator.playlist();

    if (playlist != null)
    {
      for (Entry entry : playlist)
      {
        String name = entry.name();
        String newName = FileUtils.fileNameWithoutExtension(entry.path);
        
        if (!name.equals(newName))      
          entry.rename(newName, mediator.options());
      }
      
      Entry selectedEntry = mediator.entry();
      mediator.refreshPlaylist();
      mediator.selectEntry(selectedEntry);
    }
  }
}
