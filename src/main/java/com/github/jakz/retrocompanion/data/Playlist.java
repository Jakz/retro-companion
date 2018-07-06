package com.github.jakz.retrocompanion.data;

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
import com.pixbits.lib.ui.table.ModifiableDataSource;

public class Playlist implements Iterable<Entry>, ModifiableDataSource<Entry>
{
  private Path path;
  private final List<Entry> entries;
  
  public boolean dirty;
  
  public Playlist(Path path)
  {
    this.path = path;
    entries = new ArrayList<>();
  }
  
  public String toString()
  {
    return path.getFileName().toString() + " (" + entries.size() + ")";
  }
  
  public void markDirty() 
  {
    dirty = true;
  }
  
  public void add(Entry entry)
  {
    entries.add(entry);
  }
  
  public Path path()
  {
    return path;
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

  @Override
  public void add(int index, Entry element) { entries.add(index, element); }

  @Override
  public void remove(int index) { entries.remove(index); }
  
  @Override
  public void clear() { entries.clear(); }
}
