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
package net.hillsdon.reviki.vc.impl;

import static java.util.Arrays.asList;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.getCurrentArguments;

import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ChangeType;
import net.hillsdon.reviki.vc.VersionedPageInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.RenameException;
import net.hillsdon.reviki.vc.StoreKind;
import net.hillsdon.reviki.vc.impl.SVNPageStore.SVNRenameAction;

import org.apache.commons.collections.MapUtils;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.io.ISVNEditor;

/**
 * Unit tests {@link SVNPageStore} by exploring its interactions with
 * {@link BasicSVNOperations}.  Mostly useful for edge cases etc. as
 * this is done via mocks, the more substantial testing of this code
 * is done via the webtests for now.
 *
 * @author mth
 */
public class TestSVNPageStore extends TestCase {

  private SVNPageStore _store;
  private DeletedRevisionTracker _tracker;
  private BasicSVNOperations _operations;
  private ISVNEditor _commitEditor;

  protected void setUp() {
    _commitEditor = createMock(ISVNEditor.class);
    _tracker = createMock(DeletedRevisionTracker.class);
    _operations = createMock(BasicSVNOperations.class);
    _store = new SVNPageStore("wiki", _tracker, _operations, createMock(AutoPropertiesApplier.class), new FixedMimeIdentifier());
  }

  public void testGetLatestRevisionJustDelegates() throws Exception {
    expect(_operations.getLatestRevision()).andReturn(4L);
    replay();
    assertEquals(4, _store.getLatestRevision());
    verify();
  }

  public void testDeleteAttachment() throws Exception {
    final String pageName = "ThePage";
    final PageReference pageRef = new PageReferenceImpl(pageName);
    final String attachmentName = "attachment.txt";
    _operations.execute((SVNEditAction) anyObject());
    expectLastCall().andAnswer(new IAnswer<Object>() {
      public Object answer() throws Throwable {
        SVNEditAction action = (SVNEditAction) getCurrentArguments()[0];
        action.driveCommitEditor(_commitEditor, _operations);
        return 2L;
      }
    });
    _operations.delete((ISVNEditor) anyObject(), eq("ThePage-attachments/attachment.txt"), eq(-1L));
    replay();
    assertEquals(2, _store.deleteAttachment(pageRef, attachmentName, -1, "A delete"));
    verify();
  }

  public void testHistoryLogsToHeadIfNoDeletedRevision() throws Exception {
    final String path = "ThePage";
    final ChangeInfo previousEdit  = new ChangeInfo(path, path, "mth", new Date(), 3, "An edit", StoreKind.PAGE, ChangeType.MODIFIED, null, -1);
    expect(_tracker.getChangeThatDeleted(path)).andReturn(null);
    expect(_operations.log(path, -1, LogEntryFilter.PATH_ONLY, false, 0, -1)).andReturn(asList(previousEdit));
    replay();
    assertEquals(asList(previousEdit), _store.history(new PageReferenceImpl(path)));
    verify();
  }

  public void testHistoryLogsUptoDeletedRevisionAndIncludesIt() throws Exception {
    final String path = "ThePage";
    final ChangeInfo previousEdit  = new ChangeInfo(path, path, "mth", new Date(), 3, "An edit", StoreKind.PAGE, ChangeType.MODIFIED, null, -1);
    final ChangeInfo deleteChange = new ChangeInfo(path, path, "mth", new Date(), 7, "Deleted", StoreKind.PAGE, ChangeType.DELETED, null, -1);
    expect(_tracker.getChangeThatDeleted(path)).andReturn(deleteChange);
    expect(_operations.log(path, -1, LogEntryFilter.PATH_ONLY, false, 0, deleteChange.getRevision() - 1)).andReturn(asList(previousEdit));
    replay();
    List<ChangeInfo> history = _store.history(new PageReferenceImpl(path));
    assertEquals(asList(deleteChange, previousEdit), history);
    verify();
  }

  public void testHistoryStepsBackOverCopies() throws Exception {
    final String originalName = "TheOriginalPage";
    final String copyName = "TheCopiedPage";
    final ChangeInfo create  = new ChangeInfo(originalName, originalName, "mth", new Date(), 1, "Initial create", StoreKind.PAGE, ChangeType.ADDED, null, -1);
    final ChangeInfo copyRemove = new ChangeInfo(copyName, copyName, "mth", new Date(), 2, "Copy delete", StoreKind.PAGE, ChangeType.DELETED, null, -1);
    final ChangeInfo copyAdd  = new ChangeInfo(copyName, copyName, "mth", new Date(), 2, "Copy add", StoreKind.PAGE, ChangeType.ADDED, originalName, 1);
    final ChangeInfo edit  = new ChangeInfo(copyName, copyName, "mth", new Date(), 3, "Edit", StoreKind.PAGE, ChangeType.MODIFIED, null, -1);
    expect(_tracker.getChangeThatDeleted(copyName)).andReturn(null);
    expect(_operations.log(copyName, -1, LogEntryFilter.PATH_ONLY, false, 0, -1)).andReturn(asList(edit, copyAdd));
    expect(_operations.log(originalName, -1, LogEntryFilter.PATH_ONLY, false, 0, 1)).andReturn(asList(copyRemove, create));
    replay();
    assertEquals(asList(edit, copyAdd, copyRemove, create), _store.history(new PageReferenceImpl(copyName)));
    verify();
  }

  public void testRename() throws Exception {
    String originalName = "TheOriginalPage";
    String newName = "TheRenamedPage";
    final PageReference originalPage = new PageReferenceImpl(originalName);
    final PageReference newPage = new PageReferenceImpl(newName);
    _operations.moveFile(null, originalName, 1, newName);
    replay();
    SVNRenameAction action = new SVNPageStore.SVNRenameAction("", newPage, false, originalPage, 1);
    action.driveCommitEditor(null, _operations);
    verify();
  }

  public void testRenameWithAttachments() throws Exception {
    String originalName = "TheOriginalPage";
    String newName = "TheRenamedPage";
    final PageReference originalPage = new PageReferenceImpl(originalName);
    final PageReference newPage = new PageReferenceImpl(newName);
    _operations.moveFile(null, originalName, 1, newName);
    _operations.moveDir(null, originalName + "-attachments", 1, newName + "-attachments");
    replay();
    SVNRenameAction action = new SVNPageStore.SVNRenameAction("", newPage, true, originalPage, 1);
    action.driveCommitEditor(null, _operations);
    verify();
  }

  public void testRenameLocked() throws Exception {
    String originalName = "TheOriginalPage";
    String newName = "TheRenamedPage";
    final PageReference originalPage = new PageReferenceImpl(originalName);
    final PageReference newPage = new PageReferenceImpl(newName);
    _operations.moveFile(null, originalName, 1, newName);
    expectLastCall().andThrow(new SVNException(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED)));
    replay();
    SVNRenameAction action = new SVNPageStore.SVNRenameAction("", newPage, false, originalPage, 1);
    try {
      action.driveCommitEditor(null, _operations);
      fail("Expected RenameException");
    }
    catch (RenameException ex) {
      // OK
    }
    verify();
  }

  public void testCreateLocksMapThrowsException() throws Exception {
    String name = "ThePage";
    String lockToken = "";
    try {
      _store.createLocksMap(name, lockToken);
      fail("Expected IllegalArgumentException");
    }
    catch (IllegalArgumentException ex) {
      // OK
    }
  }

  @SuppressWarnings("unchecked")
  public void testGetPage() throws PageStoreException {
    final String content = "Content";
    PageReferenceImpl ref = new PageReferenceImpl("Page");
    expect(_operations.checkPath(ref.getPath(), -1L)).andReturn(SVNNodeKind.FILE);
    _operations.getFile(eq(ref.getPath()), eq(-1L), (Map<String, String>) anyObject(), (OutputStream) anyObject());
    expectLastCall().andAnswer(new IAnswer<Object>() {
      public Object answer() throws Throwable {
        OutputStream out = (OutputStream) getCurrentArguments()[3];
        out.write(content.getBytes());
        return null;
      }
    });
    expect(_operations.getLock(ref.getPath())).andReturn(null);
    replay();
    VersionedPageInfo returnValue = _store.get(ref, -1);
    assertEquals(ref.getName(), returnValue.getName());
    assertEquals(content, returnValue.getContent());
    assertEquals(MapUtils.EMPTY_MAP, returnValue.getAttributes());
    verify();
  }

  @SuppressWarnings("unchecked")
  public void testGetPageWithAttributes() throws PageStoreException {
    final String content = "Content";
    PageReferenceImpl ref = new PageReferenceImpl("Page");
    expect(_operations.checkPath(ref.getPath(), -1L)).andReturn(SVNNodeKind.FILE);
    _operations.getFile(eq(ref.getPath()), eq(-1L), (Map<String, String>) anyObject(), (OutputStream) anyObject());
    expectLastCall().andAnswer(new IAnswer<Object>() {
      public Object answer() throws Throwable {
        Map<String, String> properties = (Map<String, String>) getCurrentArguments()[2];
        properties.put("reviki:someKey", "someValue1");
        properties.put("svn:someKey", "someValue2");
        properties.put("someOtherKey", "someValue3");
        OutputStream out = (OutputStream) getCurrentArguments()[3];
        out.write(content.getBytes());
        return null;
      }
    });
    expect(_operations.getLock(ref.getPath())).andReturn(null);
    replay();
    VersionedPageInfo pageInfo = _store.get(ref, -1);
    assertEquals(ref.getName(), pageInfo.getName());
    assertEquals(content, pageInfo.getContent());
    LinkedHashMap<String, String> expectedProperties = new LinkedHashMap<String, String>();
    expectedProperties.put("someKey", "someValue1");
    assertEquals(expectedProperties, pageInfo.getAttributes());
    verify();
  }

  private void verify() {
    EasyMock.verify(_tracker, _operations);
  }
  private void replay() {
    EasyMock.replay(_tracker, _operations);
  }
}