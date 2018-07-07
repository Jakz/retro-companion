package com.github.jakz.retrocompanion.ui;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import com.github.jakz.retrocompanion.data.Entry;
import com.pixbits.lib.functional.StreamException;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.io.FolderScanner;
import com.pixbits.lib.ui.table.ModifiableDataSource;
import com.pixbits.lib.ui.table.TableModel;

public class PlaylistTableTransferHandler extends TransferHandler 
{
  private final Mediator mediator;
  private final TableModel<Entry> model;
  
  private static final DataFlavor FILE_FLAVOR = DataFlavor.javaFileListFlavor;
  private static final DataFlavor localObjectFlavor = new DataFlavor(Integer.class, "Integer Row Index");
  
  private final JTable table;

  public PlaylistTableTransferHandler(Mediator mediator, JTable table, TableModel<Entry> model) 
  {
    this.mediator = mediator;
    this.model = model;
    this.table = table;
  }

  @Override
  protected Transferable createTransferable(JComponent c) 
  {
    assert (c == table);
    return new DataHandler(Integer.valueOf(table.getSelectedRow()), localObjectFlavor.getMimeType());
  }

  @Override
  public boolean canImport(TransferHandler.TransferSupport info) 
  {
    boolean isExternalFile = info.isDataFlavorSupported(FILE_FLAVOR);
    
    boolean isInternalTableRow = info.getComponent() == table && info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);

    table.setCursor(isInternalTableRow ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
    
    return isInternalTableRow || isExternalFile;
  }

  @Override
  public int getSourceActions(JComponent c) 
  {
    return TransferHandler.COPY_OR_MOVE;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean importData(TransferHandler.TransferSupport info) 
  {    
    JTable target = (JTable) info.getComponent();
    JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
    
    int index = dl.getRow();
    int max = table.getModel().getRowCount();
    
    if (index < 0 || index > max)
      index = max;
    
    target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

    try 
    {
      ModifiableDataSource<Entry> data = (ModifiableDataSource<Entry>)model.data();
      
      if (info.isDataFlavorSupported(localObjectFlavor))
      {
        Integer rowFrom = (Integer) info.getTransferable().getTransferData(localObjectFlavor);
        
        if (rowFrom != -1 && rowFrom != index) 
        {
          int[] rows = table.getSelectedRows();
          int dist = 0;
          for (int row : rows) {
            if (index > row) {
              dist++;
            }
          }
          
          index -= dist;

          List<Integer> indices = Arrays.stream(table.getSelectedRows())
            .map(table::convertRowIndexToModel)
            .mapToObj(Integer::valueOf)
            .collect(Collectors.toList());
     
        
          List<Entry> values = indices.stream()
          .map(model.data()::get)
          .collect(Collectors.toList());
        
          indices.stream().forEach(data::remove);
          
          int cindex = index;
          
          for (Entry value : values)
            data.add(cindex++, value);
          
          table.getSelectionModel().setSelectionInterval(index, cindex-1);
        }
      }
      else if (info.isDataFlavorSupported(FILE_FLAVOR))
      {
        List<File> files = (List<File>)info.getTransferable().getTransferData(FILE_FLAVOR);
        info.setDropAction(LINK);
        Path[] paths = files.stream().map( f -> f.toPath() ).toArray(Path[]::new);
        int count = recursiveAdd(index, paths);
        
        if (count > 0)
          table.getSelectionModel().setSelectionInterval(index, index+count-1);
      }
      
      return true;
    } 
    catch (Exception e) 
    {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  protected void exportDone(JComponent c, Transferable t, int act)
  {
    if (act == TransferHandler.MOVE)
    {
      table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }
  
  public int recursiveAdd(int index, Path[] start) throws IOException
  {
    Deque<Path> deque = new ArrayDeque<>();  
    FolderScanner scanner = new FolderScanner(FolderScanner.FolderMode.ADD_TO_RESULT);
    
    Set<Path> used = mediator.options().skipImportingDuplicates ?
      mediator.playlist().stream().map(e -> e.absolutePath(mediator)).collect(Collectors.toSet()) :
      Collections.emptySet();
    
    int count = 0;
    
    for (Path path : start)
      deque.addLast(path);
    
    while (!deque.isEmpty())
    {
      Path path = deque.removeLast();
      
      if (Files.isDirectory(path))
      {
        Set<Path> children = scanner.scan(path);
        children.stream()
          .filter(StreamException.rethrowPredicate(p -> !Files.isHidden(p)))
          .filter(p -> !p.getFileName().toString().startsWith("."))
          .forEach(deque::addLast);
      }
      else
      {
        if (!used.contains(path))
        {
          addEntry(index, path);
          ++count;
          
          if (mediator.options().skipImportingDuplicates)
            used.add(path);
        }
      }
    }
    
    return count;
  }
  
  public void addEntry(int index, Path path)
  {
    Entry entry = new Entry(mediator.playlist(), path, FileUtils.fileNameWithoutExtension(path), Optional.empty(), Optional.empty());
    
    if (mediator.options().autoRelativizePathsWhenImporting)
      entry.relativizePath(mediator.options().retroarchPath);
    
    mediator.playlist().add(index, entry);
  }
}