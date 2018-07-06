package com.github.jakz.retrocompanion;

import com.github.jakz.retrocompanion.data.Playlist;
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
}