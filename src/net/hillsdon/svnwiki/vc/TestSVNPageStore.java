package net.hillsdon.svnwiki.vc;

import static java.util.Arrays.asList;
import static net.hillsdon.fij.core.Functional.set;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;

import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.easymock.EasyMock;

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

  protected void setUp() {
    _tracker = createMock(DeletedRevisionTracker.class);
    _operations = createMock(BasicSVNOperations.class);
    _store = new SVNPageStore(_tracker, _operations);
  }
  
  public void testGetLatestRevisionJustDelegates() throws Exception {
    expect(_operations.getLatestRevision()).andReturn(4L);
    replay();
    assertEquals(4, _store.getLatestRevision());
    verify();
  }
  
  public void testHistoryLogsToHeadIfNoDeletedRevision() throws Exception {
    final String path = "ThePage";
    final ChangeInfo previousEdit  = new ChangeInfo(path, path, "mth", new Date(), 3, "An edit", StoreKind.PAGE, ChangeType.MODIFIED);
    expect(_tracker.getChangeThatDeleted(_operations, path)).andReturn(null);
    expect(_operations.log(path, -1, true, false, 0, -1)).andReturn(asList(previousEdit));
    replay();
    assertEquals(asList(previousEdit), _store.history(new PageReference(path)));
    verify();
  }
  
  public void testHistoryLogsUptoDeletedRevisionAndIncludesIt() throws Exception {
    final String path = "ThePage";
    final ChangeInfo previousEdit  = new ChangeInfo(path, path, "mth", new Date(), 3, "An edit", StoreKind.PAGE, ChangeType.MODIFIED);
    final ChangeInfo deleteChange = new ChangeInfo(path, path, "mth", new Date(), 7, "Deleted", StoreKind.PAGE, ChangeType.DELETED);
    expect(_tracker.getChangeThatDeleted(_operations, path)).andReturn(deleteChange);
    expect(_operations.log(path, -1, true, false, 0, deleteChange.getRevision() - 1)).andReturn(asList(previousEdit));
    replay();
    List<ChangeInfo> history = _store.history(new PageReference(path));
    assertEquals(asList(deleteChange, previousEdit), history);
    verify();
  }
  
  public void testListReturnsPageReferenceForEveryFileInRepoRoot() throws Exception {
    expect(_operations.listFiles("")).andReturn(asList("FooPage", "BarPage", "random.stuff"));
    replay();
    final Set<PageReference> expected = set(new PageReference("FooPage"), new PageReference("BarPage"), new PageReference("random.stuff"));
    assertEquals(expected, _store.list());
    verify();
  }
  
  private void verify() {
    EasyMock.verify(_tracker, _operations);
  }
  private void replay() {
    EasyMock.replay(_tracker, _operations);
  }
  
}
