package com.github.jakz.retrocompanion.ui;

import com.github.jakz.retrocompanion.Options;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.Playlist;

public interface Mediator
{
  public void onEntrySelected(Entry entry);
  
  public void selectPlaylist(Playlist playlist);
  public void selectEntry(Entry entry);
  
  public Playlist playlist();
  public Options options();
}
