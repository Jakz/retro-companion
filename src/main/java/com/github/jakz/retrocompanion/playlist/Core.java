package com.github.jakz.retrocompanion.playlist;

import java.nio.file.Path;

public class Core
{
  public final Path path;
  public final String displayName;
  public final String coreName;
  public final String systemName;
  
  public Core(Path path, String displayName, String coreName, String systemName)
  {
    this.path = path;
    this.displayName = displayName;
    this.coreName = coreName;
    this.systemName = systemName;
  }
  
}
