package com.github.jakz.retrocompanion.playlist;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.ui.table.DataSource;

public class Playlist implements Iterable<Entry>, DataSource<Entry>
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
  
  public String name()
  {
    return FileUtils.fileNameWithoutExtension(path);
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
  
  public Entry get(String name)
  {
    return stream()
      .filter(e -> e.name().equals(name))
      .findFirst()
      .orElse(null);
  }

  @Override
  public Iterator<Entry> iterator() { return entries.iterator(); }
  public Stream<Entry> stream() { return entries.stream(); }

  @Override
  public Entry get(int index) { return entries.get(index); }

  @Override
  public int size() { return entries.size(); }

  @Override
  public int indexOf(Entry entry) { return entries.indexOf(entry); }
}
