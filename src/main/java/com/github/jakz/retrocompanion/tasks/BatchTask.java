package com.github.jakz.retrocompanion.tasks;

import java.util.List;

import com.github.jakz.retrocompanion.data.Playlist;
import com.github.jakz.retrocompanion.ui.Mediator;
import com.pixbits.lib.functional.StreamException;

@FunctionalInterface
public interface BatchTask
{
  public boolean process(Mediator mediator, List<Playlist> playlists);
  
  public static BatchTask of(PlaylistTask task)
  {
    return (mediator, playlists) -> {
      boolean successOnAny = playlists.stream()
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
