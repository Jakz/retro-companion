package com.github.jakz.retrocompanion.playlist;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class Playlist implements Iterable<Entry>
{
  private Path path;
  
  private final List<Entry> entries;
  
  public Playlist(Path path)
  {
    this.path = path;
    entries = new ArrayList<>();
  }
  
  public void add(Entry entry)
  {
    entries.add(entry);
  }
  
  public String nameWithExtension() 
  { 
    return path.getFileName().toString(); 
  }
  
  public void save(Path path) throws IOException
  {
    try (BufferedWriter wrt = Files.newBufferedWriter(path))
    {
      for (Entry entry : entries)
        wrt.write(entry.toPlaylistFormat());
    }
  }

  @Override
  public Iterator<Entry> iterator() { return entries.iterator(); }
  public Stream<Entry> stream() { return entries.stream(); }
}
