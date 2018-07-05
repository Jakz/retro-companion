package com.github.jakz.retrocompanion.ui;

import java.awt.Container;
import java.util.List;

import com.github.jakz.retrocompanion.Options;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.Playlist;

public interface Mediator
{
  public void onEntrySelected(Entry entry);
  
  public void selectPlaylist(Playlist playlist);
  public void selectEntry(Entry entry);
  
  public List<Entry> getSelectedEntries();
  public void removeEntriesFromPlaylist(List<Entry> entries);
  
  public Playlist playlist();
  public Options options();
  
  public Container modalTarget();
}
