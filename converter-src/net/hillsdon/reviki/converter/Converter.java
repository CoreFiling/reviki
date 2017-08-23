package net.hillsdon.reviki.converter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ProxyInputStream;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class Converter {

  private static InputStream unclosable(final InputStream in) {
    return new ProxyInputStream(in) {
      @Override
      public void close() {
      }
    };
  }

	public static void main(final String[] args) throws IOException {
	  List<InputStream> streams = Lists.newArrayList();
	  if (args.length >= 1) {
	    streams.addAll(Collections2.transform(Arrays.asList(args), new Function<String, InputStream>() {
        @Override
        public InputStream apply(final String path) {
          try {
    	      if (path.equals("-")) {
    	        return unclosable(System.in);
    	      }
            return new FileInputStream(path);
          }
          catch (FileNotFoundException e) {
            throw new RuntimeException(e);
          }
        }
	    }));
	  }
	  else {
	    System.out.println("Usage: java -jar reviki-converter.jar <input_file | - >");
	    System.out.println();
	    System.out.println("  input_file  : reviki formatted file to convert");
	    System.out.println("                use '-' to read from stdin");
	    System.exit(1);
	  }

	  MarkdownConverter converter = new MarkdownConverter();
	  for (InputStream stream : streams) {
      System.out.print(converter.convert(IOUtils.toString(stream, "UTF-8")));
      System.out.println("-----");
	  }
  }

}
