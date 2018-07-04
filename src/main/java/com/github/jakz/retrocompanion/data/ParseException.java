package com.github.jakz.retrocompanion.data;

public class ParseException extends IllegalArgumentException
{
  public ParseException(String message)
  {
    super(message);
  }
  
  public ParseException(String message, Object... args)
  {
    super(String.format(message, args));
  }
}
