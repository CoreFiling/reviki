/**
 * Copyright 2008 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Wrapper for Properties to enable testing. 
 * 
 * @author mth
 */
public class PropertiesFile extends AbstractPropertiesStore implements PersistentStringMap {

  private final File _file;

  public PropertiesFile(final File file) {
    _file = file;
  }
  
  @Override
  protected InputStream inputStream() throws IOException {
    return _file == null ? null : new FileInputStream(_file);
  }

  @Override
  protected OutputStream outputStream() throws IOException {
    return _file == null ? null : new FileOutputStream(_file);
  }

  public boolean isPersistable() {
    return _file != null && (_file.exists() ? _file.canWrite() : _file.getParentFile().canWrite());
  }

  public File getFile() {
    return _file;
  }
  
}
