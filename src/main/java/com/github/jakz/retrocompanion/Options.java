package com.github.jakz.retrocompanion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.jakz.retrocompanion.data.CoreSet;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.Playlist;
import com.github.jakz.retrocompanion.data.ThumbnailType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.pixbits.lib.json.PathAdapter;

public class Options
{
  public Path retroarchPath;
  public Path playlistsPath;
  public Path thumbnailsPath;
  public Path coresPath;
  public Path infoPath;
  
  public boolean autoFixPlaylistNamesInEntries;
  
  /* TODO: we're passing it here but maybe there's a better place */
  public transient CoreSet cores;
  
  Options()
  {
    retroarchPath = Paths.get("F:\\Misc\\Frontends\\Retroarch");
    derivePathsFromRetroarch();
    autoFixPlaylistNamesInEntries = true;
  }
  
  public void derivePathsFromRetroarch()
  {
    playlistsPath = retroarchPath.resolve("playlists");
    thumbnailsPath = retroarchPath.resolve("thumbnails");
    coresPath = retroarchPath.resolve("cores");
    infoPath = retroarchPath.resolve("info");
  }
  
  public Path pathForThumbnail(Playlist playlist, ThumbnailType type, Entry game)
  {
    return thumbnailsPath
      .resolve(playlist.name())
      .resolve(type.folderName)
      .resolve(game.name().replaceAll("[\\&\\*\\/\\:\\`\\<\\>\\?\\|]", "_") + ".png");
  }
    
  void save(Path path) throws IOException
  {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Path.class, new PathAdapter())
      .setPrettyPrinting()
      .create();
        
    try (BufferedWriter wrt = Files.newBufferedWriter(path))
    {
      wrt.write(gson.toJson(this, Options.class));
    }
  }
  
  public void load(Path path) throws IOException
  {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Path.class, new PathAdapter())
        .registerTypeAdapter(Options.class, new InstanceCreator<Options>()
        {
          @Override public Options createInstance(Type type) {
            return Options.this;
          }    
        })
        .setPrettyPrinting()
        .create();
    
    try (BufferedReader rdr = Files.newBufferedReader(path))
    {
      gson.fromJson(rdr, Options.class);
    }
  }
  
}

