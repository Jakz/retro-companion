package com.github.jakz.retrocompanion.data;

import java.nio.file.Path;
import java.util.Optional;

import com.pixbits.lib.io.FileUtils;

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
 
  public String systemName() { return systemName; }
  public String shortLibraryName() { return FileUtils.fileNameWithoutExtension(path); }

  public static class Ref
  {
    private final Core core;
    private final Optional<String> name;
    
    public Ref(Core core, Optional<String> name)
    {
      this.core = core;
      this.name = name;
    }
    
    public Ref(Core core)
    {
      this(core, Optional.empty());
    }
        
    public Path path() { return core.path; }
    public String name() { return name.orElse(null); }
    public String shortLibraryName() { return core.shortLibraryName(); }
    
    public Ref dupe() { return new Core.Ref(core, Optional.empty()); }
  }
}
