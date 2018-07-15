package com.github.jakz.retrocompanion.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import com.github.jakz.retrocompanion.Options;
import com.github.jakz.retrocompanion.ui.Mediator;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.io.archive.ArchiveFormat;

public class Entry
{
  private Path path;
  private String name;
  private Optional<Core.Ref> core;
  Optional<DBRef> dbref;
  Playlist playlist;
  
  private long sizeInBytes;
  
  public Entry(Playlist playlist, Path path, String name, Optional<Core.Ref> core, Optional<DBRef> dbref)
  {
    this.playlist = playlist;
    
    this.path = path;
    this.name = name;
    
    this.core = core;
    this.dbref  = dbref;
    
    this.sizeInBytes = -1;
  }
  
  public void setPath(Path path) { this.path = path; }
  public Path path() { return path; }
  public Path absolutePath(Mediator mediator)
  { 
    if (path.isAbsolute())
      return path;
    else //TOOD: always safe?
      return mediator.options().retroarchPath.resolve(path).normalize().toAbsolutePath();
  }
  
  public long sizeInBytes(Mediator mediator)
  {
    if (sizeInBytes == -1)
    {
      try 
      {
        Path path = absolutePath(mediator);
        
        if (FileUtils.pathExtension(path).equals("scummvm"))
          sizeInBytes += FileUtils.folderSize(path.getParent(), true, false);
        else
          sizeInBytes = Files.size(path);  
      } 
      catch (IOException e)
      {
        e.printStackTrace();
        sizeInBytes = 0;
      }
    }
    
    return sizeInBytes;
  }
  
  public boolean isCompressed() { return ArchiveFormat.guessFormat(path) != null; }
    
  public void setPlayList(Playlist playlist) { this.playlist = playlist; }
  public Playlist playlist() { return playlist; }
  
  public void setCore(Optional<Core.Ref> core) { this.core = core; }
  public Optional<Core.Ref> core() { return core; } 
  public String name() { return name; }
  
  public void relativizePath(Path path)
  {
    if (this.path.isAbsolute())
      this.path = path.relativize(this.path).normalize();
  }
  
  public void makeAbsolutePath(Path path)
  {
    if (!this.path.isAbsolute())
      this.path = path.resolve(this.path).toAbsolutePath().normalize();
  }

  public boolean rename(String name, Options options)
  {
    try 
    {
      /* rename thumbnails */
      for (ThumbnailType tt : ThumbnailType.values())
      {
        Path oldPath = options.pathForThumbnail(playlist, tt, this);
        Path newPath = options.pathForThumbnail(playlist, tt, name());
        
        if (Files.exists(oldPath))
          Files.move(oldPath, newPath);
      }
      
      this.name = name;
      
      /* TODO: this should be done on filename change, not on entry name change
      
      // rename save states and memory cards 
      //TODO: this should be verified against strange cores like ePSXe which may use strange namings
      FolderScanner scanner = new FolderScanner(FileSystems.getDefault().getPathMatcher("glob:"+FileUtils.fileNameWithoutExtension(path.getFileName())+".*"), true);
      
      Set<Path> states = scanner.scan(Arrays.asList(new Path[] { options.savesPath, options.statesPath }));
      for (Path path : states)
      {
        String extension = FileUtils.pathExtension(path);
        
        Path finalPath = path.getParent().resolve(name + "." + extension);
        Files.move(path, finalPath);
      };     
      
      */
    } 
    catch (IOException e)
    {
      e.printStackTrace();
      return false;
    }  

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
      
      .append(dbref
          .map(DBRef::toString)
          .orElse("DETECT")
      ).append(nl)
      
      .append(playlist.nameWithExtension()).append(nl);
    
    return sb.toString();
  }
}
