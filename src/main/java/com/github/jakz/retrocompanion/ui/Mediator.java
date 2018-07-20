package com.github.jakz.retrocompanion.ui;

import java.awt.Container;
import java.util.List;

import com.github.jakz.retrocompanion.Options;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.Playlist;
import com.github.jakz.retrocompanion.tasks.TaskException;
import com.pixbits.lib.ui.elements.ProgressDialog;

public interface Mediator
{
  public void scanAndLoadPlaylists();  
  public void refreshPlaylist();
  public void refreshPlaylistMetadata();
  public void repaintPlaylistTable();
  public void addPlaylist(Playlist playlist);
  public void removePlaylist(Playlist playlist);
  public void selectPlaylist(Playlist playlist);

  public void onPlaylistSelected(Playlist playlist);
  public void onEntrySelected(Entry entry);
  
  public void selectEntry(Entry entry);
  
  public List<Entry> getSelectedEntries();
  public void removeEntriesFromPlaylist(List<Entry> entries);
  
  public List<Playlist> playlists();
  public Playlist playlist();
  public Entry entry();
  public Options options();
  
  public Container modalTarget();
  public void showOptions();
  public ProgressDialog.Manager progress();
  public void handleException(TaskException exception);
}
