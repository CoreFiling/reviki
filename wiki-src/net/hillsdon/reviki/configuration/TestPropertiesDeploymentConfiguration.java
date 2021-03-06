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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import junit.framework.TestCase;

import org.tmatesoft.svn.core.SVNURL;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

public class TestPropertiesDeploymentConfiguration extends TestCase {

  private byte[] _bytes = new byte[0];
  
  private boolean _persistable = true;

  private final PersistentStringMap _properties = new AbstractPropertiesStore() {
    protected InputStream inputStream() throws IOException {
      return new ByteArrayInputStream(_bytes);
    }
    protected OutputStream outputStream() throws IOException {
      return new ByteArrayOutputStream() {
        @Override
        public void close() throws IOException {
          _bytes = toByteArray();
        }
      };
    }
    public boolean isPersistable() {
      return _persistable;
    }
  };

  private DataDir _dataDir;
  private DeploymentConfiguration _configuration;
  
  @Override
  protected void setUp() throws Exception {
    _dataDir = createMock(DataDir.class);
    expect(_dataDir.getProperties()).andReturn(_properties);
    replay(_dataDir);
    _configuration = new PropertiesDeploymentConfiguration(_dataDir);
  }
  
  public void testSetURLThrowsIllegalArgumentExceptionOnRubbishInput() {
    WikiConfiguration wiki = _configuration.getConfiguration("SomeWiki");
    try {
      wiki.setUrl("foo bar");
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
  }

  public void testNames() {
    WikiConfiguration wiki = _configuration.getConfiguration("wikiName");
    assertEquals("wikiName", wiki.getWikiName());
  }
  
  public void testLoadSave() throws Exception {
    WikiConfiguration config = _configuration.getConfiguration("foo");
    String url = "http://svn.example.com/svn";
    config.setUrl(url);
    config.save();
    _configuration.load();
    assertEquals(SVNURL.parseURIDecoded(url), _configuration.getConfiguration("foo").getUrl());
  }
  
  public void testEditableWhenPersistable() {
    _persistable = true;
    assertTrue(_configuration.isEditable());
    _persistable = false;
    assertFalse(_configuration.isEditable());
  }

  public void testGetFixedBaseUrlWhenNoneSetIsNull() {
    assertNull(_configuration.getConfiguration("foo").getFixedBaseUrl());
  }

  public void testWhenGenericBaseUrlSetOneIsCreatedForTheWikiTrimmedAndFixedForTrailingSlash() {
    for (String url : Arrays.asList("http://www.example.com/wikis ", " http://www.example.com/wikis/")) {
      _properties.put(PropertiesDeploymentConfiguration.KEY_BASE_URL, url);
      assertEquals("http://www.example.com/wikis/foo", _configuration.getConfiguration("foo").getFixedBaseUrl());
    }
  }
  
  public void testSpecificBaseUrlPreferredOverGeneric() {
    _properties.put(PropertiesDeploymentConfiguration.KEY_BASE_URL, "http://www.example.com/bad");
    _properties.put(PropertiesDeploymentConfiguration.KEY_PREFIX_BASE_URL + "foo", "http://www.example.com/good/foo");
    assertEquals("http://www.example.com/good/foo", _configuration.getConfiguration("foo").getFixedBaseUrl());
  }
  
  public void testRandomWikiIsntComplete() {
    WikiConfiguration configuration = _configuration.getConfiguration("moodle");
    assertEquals("moodle", configuration.getWikiName());
    assertNull(configuration.getFixedBaseUrl());
    assertNull(configuration.getUrl());
    assertFalse(configuration.isComplete());
  }
  
}
