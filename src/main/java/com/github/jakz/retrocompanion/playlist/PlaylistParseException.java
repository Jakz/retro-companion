package com.github.jakz.retrocompanion.playlist;

public class PlaylistParseException extends IllegalArgumentException
{
  public PlaylistParseException(String message)
  {
    super(message);
  }
  
  public PlaylistParseException(String message, Object... args)
  {
    super(String.format(message, args));
  }
}
