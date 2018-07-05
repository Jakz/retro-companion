package com.github.jakz.retrocompanion.ui;

import java.util.Locale;
import java.util.ResourceBundle;

public enum Strings implements I18n
{ 
  OPEN_IN_FILE_EXPLORER,
  DELETE_FROM_DISK
  
  ;
  
  private static final ResourceBundle res = ResourceBundle.getBundle("com.github.jakz.retrocompanion.ui.Strings", Locale.ENGLISH);
  
  public String text()
  {
    return res.getString(this.name());
  }
  
  public String toString() { return text(); }
}
