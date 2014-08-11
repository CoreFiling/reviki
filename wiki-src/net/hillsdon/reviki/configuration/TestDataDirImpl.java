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
import java.io.IOException;

import javax.servlet.ServletContext;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;

import static net.hillsdon.fij.io.Path.join;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

public class TestDataDirImpl extends TestCase {

  private ServletContext _servletContext;
  private DataDirImpl _dataDir;
  private String _baseDir;

  @Override
  protected void setUp() throws Exception {
    File baseDirFile = File.createTempFile(getClass().getSimpleName(), "baseDir");
    assertTrue(baseDirFile.delete());
    _baseDir = baseDirFile.getCanonicalPath();
    _servletContext = EasyMock.createMock(ServletContext.class);
    _dataDir = new DataDirImpl(_servletContext, _baseDir);
  }
  
  @Override
  protected void tearDown() throws Exception {
    FileUtils.forceDelete(new File(_baseDir));
  }
  
  public void testWithoutContextParameterUsesHomeDirectory() throws Exception {
    assertUsesFile(null, join(_baseDir, "reviki-data"));
  }
  
  public void testUsesConfiguredValueWhenPresent() throws Exception {
    String configured = join(_baseDir, "dooby");
    assertUsesFile(configured, configured);
  }

  private void assertUsesFile(final String contextParameter, final String expectedPathPrefix) throws IOException {
    expect(_servletContext.getInitParameter(DataDirImpl.DATA_DIR_CONTEXT_PARAM)).andReturn(contextParameter).atLeastOnce();
    replay(_servletContext);
    PropertiesFile noInitParam = (PropertiesFile) _dataDir.getProperties();
    String actual = noInitParam.getFile().getCanonicalPath();
    assertEquals(join(expectedPathPrefix, "reviki.properties"), actual);
    assertEquals(join(expectedPathPrefix, "search-index", "foo"), _dataDir.getSearchIndexDirectory("foo").getCanonicalPath());
  }
  
}
