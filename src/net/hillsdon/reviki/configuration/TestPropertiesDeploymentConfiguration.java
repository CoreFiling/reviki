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

import junit.framework.TestCase;

import org.tmatesoft.svn.core.SVNURL;

public class TestPropertiesDeploymentConfiguration extends TestCase {

  private byte[] _bytes = new byte[0];

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
  };
  
  private final DeploymentConfiguration _configuration = new PropertiesDeploymentConfiguration(_properties);
  
  public void testSetURLThrowsIllegalArgumentExceptionOnRubbishInput() {
    WikiConfiguration wiki = _configuration.getConfiguration("SomeWiki", "SomeWiki");
    try {
      wiki.setUrl("foo bar");
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
  }

  public void testDefaultWiki() {
    _configuration.setDefaultWiki("foo");
    assertEquals("foo", _configuration.getDefaultWiki());
  }
  
  public void testNames() {
    WikiConfiguration wiki = _configuration.getConfiguration("actual", "given");
    assertEquals("given", wiki.getGivenWikiName());
    assertEquals("actual", wiki.getWikiName());
  }
  
  public void testLoadSave() throws Exception {
    WikiConfiguration config = _configuration.getConfiguration("foo", "bar");
    String url = "http://svn.example.com/svn";
    config.setUrl(url);
    config.save();
    _configuration.load();
    assertEquals(SVNURL.parseURIDecoded(url), _configuration.getConfiguration("foo", "bar").getUrl());
  }
  
}
