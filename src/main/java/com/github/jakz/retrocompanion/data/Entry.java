package com.github.jakz.retrocompanion.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.github.jakz.retrocompanion.Options;

public class Entry
{
  public Path path;
  private String name;
  private Optional<Core.Ref> core;
  Optional<DatabaseReference> databaseEntry;
  Playlist playlist;
  
  public Entry(Playlist playlist, Path path, String name, Optional<Core.Ref> core)
  {
    this.playlist = playlist;
    
    this.path = path;
    this.name = name;
    
    this.core = core;
    this.databaseEntry  = Optional.empty();
  }
    
  public void setPlayList(Playlist playlist) { this.playlist = playlist; }
  public Playlist playlist() { return playlist; }
  
  public void setCore(Optional<Core.Ref> core) { this.core = core; }
  public Optional<Core.Ref> core() { return core; } 
  public String name() { return name; }
  
  public boolean rename(String name, Options options)
  {
    for (ThumbnailType tt : ThumbnailType.values())
    {
      Path oldPath = options.pathForThumbnail(playlist, tt, this);
      Path newPath = oldPath.getParent().resolve(name+".png");
      
      if (Files.exists(oldPath))
      {
        try 
        {
          Files.move(oldPath, newPath);
        } 
        catch (IOException e)
        {
          e.printStackTrace();
          return false;
        }
      }
    }
    
    this.name = name;
    return true;
  }
  
  public String toPlaylistFormat()
  {
    StringBuilder sb = new StringBuilder();
    String nl = System.lineSeparator();
    
    sb.append(path.toString()).append(nl)
      
      .append(name).append(nl)
      
      .append(core
          .map(Core.Ref::path)
          .map(Object::toString)
          .orElse("DETECT")
      ).append(nl)
      
      .append(core
          .map(Core.Ref::name)
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
