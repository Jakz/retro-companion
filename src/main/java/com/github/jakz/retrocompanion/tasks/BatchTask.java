package com.github.jakz.retrocompanion.tasks;

import com.github.jakz.retrocompanion.ui.Mediator;
import com.pixbits.lib.functional.StreamException;

@FunctionalInterface
public interface BatchTask
{
  public boolean process(Mediator mediator) throws TaskException;
  
  public static BatchTask of(PlaylistTask task)
  {
    return mediator -> {
      boolean successOnAny = mediator.playlists().stream()
          .map(StreamException.rethrowFunction(playlist -> Tasks.executePlaylistTask(mediator, task, playlist)))
          .reduce(false, Boolean::logicalOr);
      
      return successOnAny;
    };
  }
  
  public static BatchTask of(EntryTask task)
  {
    return BatchTask.of(PlaylistTask.of(task));
  }
}
