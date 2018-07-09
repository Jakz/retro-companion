package com.github.jakz.retrocompanion.tasks;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import javax.swing.JMenuItem;

import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.Playlist;
import com.github.jakz.retrocompanion.tasks.Tasks.Standalone;
import com.github.jakz.retrocompanion.ui.Mediator;
import com.pixbits.lib.functional.StreamException;

@FunctionalInterface
public interface PlaylistTask
{
  boolean process(Mediator mediator, Playlist playlist) throws TaskException;
  
  public static PlaylistTask of(EntryTask task)
  {
    return (mediator, playlist) -> {
      boolean successOnAny = playlist.stream()
          .map(StreamException.rethrowFunction(entry -> Tasks.executeEntryTask(mediator, task, entry)))
          .reduce(false, Boolean::logicalOr);
      
      return successOnAny;
    };
  }
  
  public static final PlaylistTask SortPlaylistAlphabetically = (mediator, playlist) ->
  {
    //TODO: localize
    boolean confirmed = Tasks.askForConfirmation(mediator, "Sorting the playlist can't be undone, are you sure you want to proceed?"); 
    
    if (confirmed)
    {                 
      Standalone.sortPlaylistAlphabetically(playlist);      
      mediator.refreshPlaylist();
    }
    
    return true;
  };
  
  public static final PlaylistTask AddNewEntryToPlaylist = (mediator, playlist) ->
  {
    List<Entry> entries = mediator.getSelectedEntries();
    
    int index = entries.stream()
      .map(playlist::indexOf)
      .max(Integer::compare)
      .orElse(playlist.size());
    
    Entry entry = new Entry(playlist, Paths.get("locate.me"), "Name", Optional.empty(), Optional.empty());
    playlist.add(index, entry);
    
    playlist.markDirty();
    mediator.refreshPlaylist();
    mediator.selectEntry(entry);  
    
    return true;
  };
  
  public static final PlaylistTask SavePlaylist = (mediator, playlist) ->
  {
    try
    {
      playlist.save(playlist.path());
      return true;
    }
    catch (IOException e)
    {
      throw new TaskException("Excepton while saving", e);
      //e.printStackTrace();
    }
  };
  
  public static final PlaylistTask DeletePlaylist = (mediator, playlist) ->
  {
    try
    {
      if (!Tasks.askForConfirmation(mediator, "Deleting a playlist can't be undone, do you want to proceed?"))
        return false;
      
      if (Files.exists(playlist.path()))
        Files.delete(playlist.path());
      
      mediator.removePlaylist(playlist);
      return true;
    }
    catch (IOException e)
    {
      throw new TaskException("Exception while deleting playlist", e);
    }
  };
  
  public static PlaylistTask RenamePlaylist(String name)
  {
    return (mediator, playlist) ->
    {
      if (playlist.name().equals(name))
        return false;
      else
      {
        
      }
      
      return true;
    };
  }
  
  public static PlaylistTask ImportIconsFromExisting(String name)
  {
    return (mediator, playlist) ->
    {
      try
      {
        Path[] destPaths = mediator.options().pathsForPlaylistIcon(playlist.name());
        boolean anyExists = Files.exists(destPaths[0]) || Files.exists(destPaths[1]);
      
        if (anyExists && !mediator.options().overwriteThumbnailWithoutConfirmation && !Tasks.askForConfirmation(mediator, "Are you sure you want to override exising icons?"))
          return false;
      
        Path[] srcPaths = mediator.options().pathsForPlaylistIcon(name);
        
        for (int i = 0; i < destPaths.length; ++i)
          Files.copy(srcPaths[i], destPaths[i], StandardCopyOption.REPLACE_EXISTING);
        
        return true;
      }
      catch (IOException e)
      {
        throw new TaskException("Exception while importing icons for playlist", e);
      }
    };
  }
}