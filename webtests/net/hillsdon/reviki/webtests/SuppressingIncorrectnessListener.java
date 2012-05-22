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
package net.hillsdon.reviki.webtests;

import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.IncorrectnessListenerImpl;

public class SuppressingIncorrectnessListener implements IncorrectnessListener {

    /**
     * Internet Explorer really does not like the RFC and IANA official type
     * of application/javascript
     * http://stackoverflow.com/questions/1288263/why-doesnt-ie8-recognize-type-application-javascript-in-a-script-tag
     * so we use text/javascript which all browsers seem to accept.
     */
    private static final String TEXT_JAVASCRIPT_OBSOLETE_MESSAGE = "Obsolete content type encountered: 'text/javascript'.";

    private final IncorrectnessListener _delegate;

    public SuppressingIncorrectnessListener() {
      _delegate = new IncorrectnessListenerImpl();
    }

    public void notify(String message, Object origin) {
      if (!isIgnorable(message)) {
        _delegate.notify(message, origin);
      }
    }

    private boolean isIgnorable(final String message) {
      if (message.equals(TEXT_JAVASCRIPT_OBSOLETE_MESSAGE)) {
        return true;
      }
      return false;
    }
}
