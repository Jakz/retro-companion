package com.github.jakz.retrocompanion;

import java.util.List;
import java.util.stream.Collectors;

import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.Playlist;
import com.github.jakz.retrocompanion.ui.Mediator;
import com.github.jakz.retrocompanion.ui.Toolbar;
import com.pixbits.lib.ui.UIUtils;

public class Tasks
{
  public static void sortPlaylistAlphabetically(Playlist playlist)
  {
    List<Entry> entries = playlist.stream()
        .sorted((e1, e2) -> e1.name().compareToIgnoreCase(e2.name()))
        .collect(Collectors.toList());
    
    playlist.clear();
    entries.forEach(playlist::add);
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
        sortPlaylistAlphabetically(playlist);      
        mediator.selectPlaylist(playlist);
      }
    }
  }
}
