package com.github.jakz.retrocompanion.data;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
  
  public List<Optional<Core.Ref>> buildRefList()
  {
    Stream<Optional<Core.Ref>> cores = Stream.concat(
        Stream.of(Optional.empty()), 
        stream()
          .map(c -> Optional.of(new Core.Ref(c, Optional.empty())))
          .sorted((c1, c2) -> c1.get().shortLibraryName().compareTo(c2.get().shortLibraryName()))
    );
    
    return cores.collect(Collectors.toList());
  }
  
  @Override public Iterator<Core> iterator() { return cores.iterator(); }
  @Override public Core get(int index) { return cores.get(index); }
  @Override public int size() { return cores.size(); }
  @Override public int indexOf(Core object) { return cores.indexOf(object); }
}
