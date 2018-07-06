package com.github.jakz.retrocompanion.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.swing.DefaultCellEditor;
import javax.swing.DropMode;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.github.jakz.retrocompanion.Options;
import com.github.jakz.retrocompanion.data.Core;
import com.github.jakz.retrocompanion.data.DBRef;
import com.github.jakz.retrocompanion.data.Entry;
import com.github.jakz.retrocompanion.data.Playlist;
import com.github.jakz.retrocompanion.data.ThumbnailType;
import com.pixbits.lib.functional.StreamException;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.io.FolderScanner;
import com.pixbits.lib.ui.FileTransferHandler;
import com.pixbits.lib.ui.table.ColumnSpec;
import com.pixbits.lib.ui.table.DataSource;
import com.pixbits.lib.ui.table.SimpleListSelectionListener;
import com.pixbits.lib.ui.table.TableModel;
import com.pixbits.lib.ui.table.TableRowTransferHandler;
import com.pixbits.lib.ui.table.editors.PathArgumentEditor;
import com.pixbits.lib.ui.table.renderers.DefaultTableAndListRenderer;

public class PlaylistTablePanel extends JPanel
{
  private final Mediator mediator;
  private final Options options;
  
  private JTable table;
  private Model model;
  private Playlist playlist;
  
  @SuppressWarnings("unchecked")
  public PlaylistTablePanel(Mediator mediator)
  {
    this.mediator = mediator;
    this.options = mediator.options();
    
    table = new JTable();
    model = new Model(table);
    
    table.setDragEnabled(true);
    table.setDropMode(DropMode.INSERT_ROWS);
    table.setTransferHandler(new TableRowTransferHandler<Entry>(model));
    table.setFillsViewportHeight(true);

    JScrollPane tablePane = new JScrollPane(table);
    tablePane.setPreferredSize(new Dimension(800, 400));
    
    setLayout(new BorderLayout());
    add(tablePane, BorderLayout.CENTER);
    
    try
    {
      ColumnSpec<Entry,?> nameColumn = new ColumnSpec<>("Name", String.class, e -> e.name(), (e,v) -> {
        /* skip if name didn't change */
        if (v.equals(e.name()))
          return;
        
        final Entry entryWithSameName = playlist.get(v);
        
        /* don't do anything if name is already used for another entry */
        if (entryWithSameName != null && entryWithSameName != e)
          JOptionPane.showMessageDialog(this, "The name is already used for another entry!", "Error", JOptionPane.ERROR_MESSAGE);
        else
        {
          e.rename(v, options);
          mediator.onEntrySelected(e);
        }
      });
      nameColumn.setWidth(250);
      nameColumn.setEditable(true);
      model.addColumn(nameColumn);
      
      final String[] thumbnailCaptions = { "B", "S", "T" };
      final ThumbnailType[] thumbnailType = { ThumbnailType.BOXART, ThumbnailType.SNAP, ThumbnailType.TITLE };

      for (int i = 0; i < 1/*ThumbnailType.values().length*/; ++i)
      {
        final ThumbnailType tt = thumbnailType[i];
        
        ColumnSpec<Entry, ?> thumbnailColumn = new ColumnSpec<>(thumbnailCaptions[i], boolean.class, e -> Files.exists(options.pathForThumbnail(playlist, tt, e)));
        thumbnailColumn.setWidth(30);
        model.addColumn(thumbnailColumn);
      }
      
      model.setDefaultRenderer(boolean.class, new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
          JLabel field = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
          
          field.setHorizontalAlignment(JLabel.CENTER);
          
          if ((boolean)value)
          {
            field.setForeground(Color.GREEN);
            field.setText("✔");
          }
          else
          {
            field.setForeground(Color.RED);
            field.setText("❌");
          }
          
          return field;
        }
      });
             
      //e.core().map(Core.Ref::shortLibraryName).orElse("DETECT")
      Function<Entry, Optional<Core.Ref>> getter = e -> e.core();
      BiConsumer<Entry, Optional<Core.Ref>> setter = (e,v) -> e.setCore(v.map(Core.Ref::dupe));
      ColumnSpec<Entry, Optional<Core.Ref>> coreColumn = new ColumnSpec<Entry, Optional<Core.Ref>>("Core", (Class<Optional<Core.Ref>>)(Class<?>)Optional.class, getter, setter);
      model.addColumn(coreColumn);
      
      Stream<Optional<Core.Ref>> cores = Stream.concat(
          Stream.of(Optional.empty()), 
          options.cores.stream()
            .map(c -> Optional.of(new Core.Ref(c, Optional.empty())))
            .sorted((c1, c2) -> c1.get().shortLibraryName().compareTo(c2.get().shortLibraryName()))
      );


      DefaultTableAndListRenderer<Optional<Core.Ref>> renderer = new DefaultTableAndListRenderer<>()
      {
        @Override
        public void decorate(JLabel label, JComponent source, Optional<Core.Ref> value, int index, boolean isSelected, boolean hasFocus)
        {
          label.setFont(PlaylistTablePanel.this.getFont().deriveFont(PlaylistTablePanel.this.getFont().getSize()*0.8f));
          label.setText(value.map(c -> c.shortLibraryName()).orElse("DETECT"));
        }
      };
      
      JComboBox<Optional<Core.Ref>> comboBox = new JComboBox<>(cores.toArray(i -> new Optional<?>[i]));
      comboBox.setRenderer(renderer);
      
      coreColumn.setEditable(true);
      coreColumn.setRenderer(renderer);
      coreColumn.setEditor(new DefaultCellEditor(comboBox));
      coreColumn.setWidth(150);
      
      ColumnSpec<Entry, Path> pathColumn = new ColumnSpec<Entry, Path>("Path", Entry.class.getField("path"), true);
      pathColumn.setEditor(new PathArgumentEditor(JFileChooser.FILES_ONLY));
      model.addColumn(pathColumn);
      
      model.fireTableStructureChanged();
      
      table.getSelectionModel().addListSelectionListener(SimpleListSelectionListener.ofJustSingle(i -> {
        mediator.onEntrySelected(i != -1 ? model.data().get(i) : null);      
      }));
      
      table.setTransferHandler(new FileTransferHandler(new DragDropListener()));
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public List<Entry> getSelectedEntries()
  {
    int[] rows = table.getSelectedRows();
        
    return Arrays.stream(rows)
        .map(table::convertRowIndexToModel)
        .mapToObj(playlist::get)
        .collect(Collectors.toList());
  }
  
  public boolean selectEntry(Entry entry)
  {
    if (playlist != null)
    {
      int index = playlist.indexOf(entry);
      
      if (index != -1)
      {
        int aindex = table.convertRowIndexToView(index);
        table.getSelectionModel().setSelectionInterval(aindex, aindex);
        return true;
      }
    }
    
    return false;
  }
  
  public void setPlaylist(Playlist playlist)
  {
    this.playlist = playlist;
    table.clearSelection();
    model.setData(playlist != null ? playlist : DataSource.empty());
    refresh();
  }
  
  public Playlist playlist()
  {
    return playlist;
  }
  
  public void refresh()
  {
    model.fireTableDataChanged();
  }

  private class Model extends TableModel<Entry>
  {
    
    Model(JTable table)
    {
      super(table, DataSource.empty());
    }
  }
  
  private class DragDropListener implements FileTransferHandler.Listener
  {
    @Override
    public void filesDropped(TransferHandler.TransferSupport info, Path[] files)
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
        recursiveAdd(index, files);
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
      
      mediator.refreshPlaylist();
    }
    
    public void recursiveAdd(int index, Path[] start) throws IOException
    {
      Deque<Path> deque = new ArrayDeque<>();  
      FolderScanner scanner = new FolderScanner(FolderScanner.FolderMode.ADD_TO_RESULT);
      
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
            .forEach(deque::addLast);
        }
        else
          addEntry(index, path);
      }
    }
    
    public void addEntry(int index, Path path)
    {
      Entry entry = new Entry(playlist, path, FileUtils.fileNameWithoutExtension(path), Optional.empty(), Optional.empty());
      
      if (mediator.options().autoRelativizePathsWhenImporting)
        entry.relativizePath(mediator.options().retroarchPath);
      
      playlist.add(index, entry);
    }
  }
}
