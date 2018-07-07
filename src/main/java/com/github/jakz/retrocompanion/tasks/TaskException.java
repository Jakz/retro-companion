package com.github.jakz.retrocompanion.tasks;

public class TaskException extends Exception
{
  public final boolean shouldShowErrorDialog;
  public final String dialogMessage;

  public TaskException(String message, Throwable cause)
  {
    super(message, cause);
    shouldShowErrorDialog = true;
    this.dialogMessage = message;
  }
  
  public TaskException(String message, String dialogMessage, Throwable cause)
  {
    super(message, cause);
    shouldShowErrorDialog = true;
    this.dialogMessage = dialogMessage;
  }
  
  public TaskException(String message, String dialogMessage)
  {
    super(message);
    shouldShowErrorDialog = true;
    this.dialogMessage = dialogMessage;
  }
}
