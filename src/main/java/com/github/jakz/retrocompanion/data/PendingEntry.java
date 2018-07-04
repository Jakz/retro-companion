package com.github.jakz.retrocompanion.data;

import java.nio.file.Path;
import java.util.Optional;

public class PendingEntry
{
  Path path;
  String name;
  Optional<Path> corePath;
  Optional<String> coreName;
  Optional<DatabaseReference> databaseEntry;
  Path playlistName;
}
