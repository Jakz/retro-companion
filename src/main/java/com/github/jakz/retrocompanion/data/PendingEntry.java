package com.github.jakz.retrocompanion.data;

import java.nio.file.Path;
import java.util.Optional;

public class PendingEntry
{
  public Path path;
  public String name;
  public Optional<Path> corePath;
  public Optional<String> coreName;
  public Optional<DatabaseReference> databaseEntry;
  public Path playlistName;
}
