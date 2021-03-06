package com.github.jakz.retrocompanion.parsers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jakz.retrocompanion.Options;
import com.github.jakz.retrocompanion.data.Core;
import com.github.jakz.retrocompanion.data.CoreSet;
import com.pixbits.lib.functional.StreamException;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.io.FolderScanner;

public class CoreParser
{
  public CoreSet parse(Options options) throws IOException
  {
    if (!Files.exists(options.coresPath))
      return new CoreSet(Collections.emptyList());
    
    FolderScanner scanner = new FolderScanner(false);
    
    Set<Path> paths = scanner.scan(options.coresPath);
    
    return new CoreSet(paths.stream()
      .map(StreamException.rethrowFunction(lib -> {
        Path infoFilePath = options.infoPath.resolve(FileUtils.fileNameWithoutExtension(lib) + ".info");
  
        Map<String, String> fields = new HashMap<>();
     
        Files.lines(infoFilePath).forEach(line -> {
          int splitIndex = line.indexOf("=");
          
          String fieldName = line.substring(0, splitIndex).trim();
          String fieldValue = line.substring(splitIndex+1).trim();
  
          //if (fieldValue.charAt(0) != '\"' || fieldValue.charAt(fieldValue.length()-1) != '\"')
          //  throw new ParseException("info file value should be quoted by \" (%s)", line);
          
          if (fieldValue.charAt(0) == '\"')
            fieldValue = fieldValue.substring(1, fieldValue.length()-1);
          
          fields.put(fieldName, fieldValue);
        });
        
        String displayName = fields.get("display_name");
        String coreName = fields.get("corename");
        String systemName = fields.get("systemname");  
        
        return new Core(lib, displayName, coreName, systemName);
      }))
      .collect(Collectors.toList()));
  }
}
