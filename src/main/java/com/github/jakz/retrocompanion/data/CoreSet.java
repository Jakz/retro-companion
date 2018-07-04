package com.github.jakz.retrocompanion.data;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import com.pixbits.lib.ui.table.DataSource;

public class CoreSet implements DataSource<Core>
{
  private List<Core> cores;
  
  public CoreSet(List<Core> cores)
  {
    this.cores = cores;
  }
  
  public Core forPath(Path path)
  {
    return cores.stream()
      .filter(core -> core.path.equals(path))
      .findFirst()
      .orElse(null);
  }
  
  @Override public Iterator<Core> iterator() { return cores.iterator(); }
  @Override public Core get(int index) { return cores.get(index); }
  @Override public int size() { return cores.size(); }
  @Override public int indexOf(Core object) { return cores.indexOf(object); }
}
