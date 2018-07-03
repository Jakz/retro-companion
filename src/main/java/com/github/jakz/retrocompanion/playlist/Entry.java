package com.github.jakz.retrocompanion.playlist;

import java.nio.file.Path;
import java.util.Optional;

public class Entry
{
  public Path path;
  public String name;
  Optional<CoreReference> core;
  Optional<DatabaseReference> databaseEntry;
  Playlist playlist;
  
  public Entry(Playlist playlist, Path path, String name)
  {
    this.playlist = playlist;
    
    this.path = path;
    this.name = name;
    
    this.core = Optional.empty();
    this.databaseEntry  = Optional.empty();
  }
    
  public void setPlayList(Playlist playlist) { this.playlist = playlist; }
  
  public String toPlaylistFormat()
  {
    StringBuilder sb = new StringBuilder();
    String nl = System.lineSeparator();
    
    sb.append(path.toString()).append(nl)
      
      .append(name).append(nl)
      
      .append(core
          .map(CoreReference::path)
          .map(Object::toString)
          .orElse("DETECT")
      ).append(nl)
      
      .append(core
          .map(CoreReference::name)
          .orElse("DETECT")
      ).append(nl)
      
      .append(databaseEntry
          .map(DatabaseReference::toString)
          .orElse("DETECT")
      ).append(nl)
      
      .append(playlist.nameWithExtension()).append(nl);
    
    return sb.toString();
  }
}
