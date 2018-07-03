package com.github.jakz.retrocompanion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.jakz.retrocompanion.playlist.Entry;
import com.github.jakz.retrocompanion.playlist.Playlist;
import com.github.jakz.retrocompanion.playlist.ThumbnailType;
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
  
  public boolean autoFixPlaylistNamesInEntries;
  
  Options()
  {
    retroarchPath = Paths.get("/retro/arch/path");
    playlistsPath = Paths.get("/play/lists/arch/path");
    thumbnailsPath = Paths.get("/thumbnails/arch/path");
    autoFixPlaylistNamesInEntries = true;
  }
  
  public Path pathForThumbnail(Playlist playlist, ThumbnailType type, Entry game)
  {
    return thumbnailsPath
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

