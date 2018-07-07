package com.github.jakz.retrocompanion;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

@SuppressWarnings("unchecked")
public class Option<T>
{
  private Supplier<T> getter;
  private Consumer<T> setter;
  private Consumer<T> refresher;
  private JComponent component;
  
  public Option(String textualName, String fieldName, final Object object)
  {
    try
    {
      Field field = object.getClass().getField(fieldName);
      
      getter = () -> {      
        try { return (T) field.get(object); } 
        catch (IllegalArgumentException | IllegalAccessException e) { e.printStackTrace(); return null; }
      };
      
      setter = t -> {
        try { field.set(object, t); }
        catch (IllegalArgumentException | IllegalAccessException e) { e.printStackTrace(); }
      };
      
      if (field.getType() == Boolean.class || field.getType() == boolean.class)
      {
        JCheckBox checkBox = new JCheckBox(textualName);
        checkBox.addActionListener(e -> {
          ((Consumer<Boolean>)setter).accept(checkBox.isSelected());
        });
        
        refresher = t -> checkBox.setSelected((boolean)t);
        
        this.component = checkBox;
      }
    } 
    catch (NoSuchFieldException | SecurityException e1)
    {
      e1.printStackTrace();
    }
  }

  public JComponent getComponent() { return component; }
  public void refresh() { refresher.accept(get()); }
  
  private T get() { return getter.get(); }
  private void set(T v) { setter.accept(v); }
}
