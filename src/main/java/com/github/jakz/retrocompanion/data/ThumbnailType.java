package com.github.jakz.retrocompanion.data;

public enum ThumbnailType
{
  BOXART("Named_Boxarts", "boxart"),
  SNAP("Named_Snaps", "snap"),
  TITLE("Named_Titles", "title")
  
  ;
  
  private ThumbnailType(String folderName, String name)
  {
    this.folderName = folderName;
    this.name = name;
  }
  
  public String folderName;
  public String name;
}
