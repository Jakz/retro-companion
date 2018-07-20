package com.github.jakz.retrocompanion.parsers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import com.github.jakz.retrocompanion.Options;
import com.github.jakz.retrocompanion.data.Core;
import com.github.jakz.retrocompanion.data.CoreSet;
import com.github.jakz.retrocompanion.data.DBRef;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.PendingEntry;
import com.github.jakz.retrocompanion.data.Playlist;
import com.pixbits.lib.functional.StreamException;
import com.pixbits.lib.functional.StreamUtil;

public class PlaylistParser
{

  
  private final Options options;
  
  public PlaylistParser(Options options)
  {
    this.options = options;
  }
  
  private static PendingEntry parseEntry(List<String> lines)
  {
    if (lines.size() != 6)
      throw new ParseException("an entry requires 6 lines to be parsed correctly");
    
    PendingEntry entry = new PendingEntry();
    entry.path = Paths.get(lines.get(0));
    entry.name = lines.get(1);
    entry.corePath = lines.get(2).equals("DETECT") ? Optional.empty() : Optional.of(Paths.get(lines.get(2)));
    entry.coreName = lines.get(3).equals("DETECT") ? Optional.empty() : Optional.of(lines.get(3));
    
    if (lines.get(4).equals("DETECT"))
      entry.dbref = Optional.empty();
    else
    {
      String[] tokens = lines.get(4).split("\\|");
      
      if (tokens[1].equals("crc"))
        entry.dbref = Optional.of(new DBRef.CRC(Long.parseLong(tokens[0], 16)));
    }   
    
    entry.playlistName = Paths.get(lines.get(5));
    
    return entry;
  }
  
  public Entry resolve(Playlist playlist, PendingEntry entry)
  {
    CoreSet cores = options.cores;
    
    /* parse core */
    Optional<Core.Ref> coreRef = entry.corePath.map(StreamException.rethrowFunction(corePath -> {
      Core core = cores.forPath(corePath);
      
      if (core == null)
      {
        if (options.ignoreUnknownCores)
          return null;
        
        throw new ParseException("Unknown core for entry: %s", corePath);
      }
      
      return new Core.Ref(core, entry.coreName);
    }));

    /* parse database reference */
    
    if (!options.autoFixPlaylistNamesInEntries && !entry.playlistName.toString().equals(playlist.nameWithExtension()))
      throw new ParseException("Name of playlist in entry doesn't match name of playlist itself ('%s' != '%s')", entry.playlistName.toString(), playlist.nameWithExtension());

    Path path = options.retroarchPath.resolve(entry.path).normalize().toAbsolutePath();
    
    return new Entry(playlist, path, entry.name, coreRef, entry.dbref);
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
