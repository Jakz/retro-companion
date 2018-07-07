package com.github.jakz.retrocompanion.parsers;

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
