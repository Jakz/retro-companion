package com.github.jakz.retrocompanion.tasks;

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
}