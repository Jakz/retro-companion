package com.github.jakz.retrocompanion.playlist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.github.jakz.retrocompanion.Options;
import com.pixbits.lib.functional.StreamException;
import com.pixbits.lib.functional.StreamUtil;

public class PlaylistParser
{
  public static PendingEntry parseEntry(List<String> lines)
  {
    if (lines.size() != 6)
      throw new ParseException("an entry requires 6 lines to be parsed correctly");
    
    PendingEntry entry = new PendingEntry();
    entry.path = Paths.get(lines.get(0));
    entry.name = lines.get(1);
    entry.corePath = lines.get(2).equals("DETECT") ? Optional.empty() : Optional.of(Paths.get(lines.get(2)));
    entry.coreName = lines.get(3).equals("DETECT") ? Optional.empty() : Optional.of(lines.get(3));
    entry.databaseEntry = Optional.empty();
    entry.playlistName = Paths.get(lines.get(5));
    
    return entry;
  }
  
  private final Options options;
  
  public PlaylistParser(Options options)
  {
    this.options = options;
  }
  
  public Entry resolve(Playlist playlist, PendingEntry entry)
  {
    /* parse core */
    /* parse database reference */
    
    if (!options.autoFixPlaylistNamesInEntries && !entry.playlistName.toString().equals(playlist.nameWithExtension()))
      throw new ParseException("Name of playlist in entry doesn't match name of playlist itself ('%s' != '%s')", entry.playlistName.toString(), playlist.nameWithExtension());

    return new Entry(playlist, entry.path, entry.name);
  }
  
  public Playlist parse(Path filepath)
  {
    Playlist playlist = new Playlist(filepath);
    
    try
    {    
      StreamUtil.assemble(Files.lines(filepath), 6)
        .map(StreamException.rethrowFunction(PlaylistParser::parseEntry))
        .map(StreamException.rethrowFunction(pe -> resolve(playlist, pe)))
        .forEach(playlist::add);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return null;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }

    return playlist;
  }
}
