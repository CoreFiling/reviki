<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw" %>

<c:set var="lastEditAction">
  <c:choose>
    <c:when test="${pageInfo.renamed}">Renamed</c:when>
    <c:when test="${pageInfo.deleted}">Deleted</c:when>
    <c:when test="${not pageInfo.newPage}">Last changed</c:when>
  </c:choose>
</c:set>

<c:set var="encodedPageName" value="${sw:urlEncode(page.name)}" />

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="${pageInfo.title} - ${pageInfo.revisionName}"/></tiles:putAttribute>
  <tiles:putAttribute name="heading"><c:out value="${pageInfo.title}"/><c:if test="${showHeadRev}"> - r${pageInfo.revision}</c:if></tiles:putAttribute>
  <tiles:putAttribute name="menuItems">
    <c:if test="${not pageInfo.locked or pageInfo.lockedBy == username}">
      <li class="menu">
        <form id="editTop" name="editTop" action="<c:url value="${encodedPageName}"/>" method="post" style="display:inline;">
          <input class="btn btn-default" type="submit" value="Edit"/>
        </form>
      </li>
      <li class="menu">
        <a name="attachments" href="<c:url value="${encodedPageName}/attachments/"/>">Attachments</a>
      </li>
      <c:if test="${not pageInfo.newPage}">
        <li class="menu">
          <a name="rename" href="<c:url value="${encodedPageName}?rename"/>">Rename</a>
        </li>
        <li class="menu">
          <a name="copy" href="<c:url value="${encodedPageName}?copy"/>">Copy</a>
        </li>
      </c:if>
    </c:if>
    <c:if test="${not empty lastEditAction}">
	    <li class="menu">
	      <a name="history" href="<c:url value="${encodedPageName}?history"/>">History</a>
	    </li>
    </c:if>

    <li class="menu ctypes">
      <form action="<c:url value="${encodedPageName}"/>" method="get">
        <select name="ctype" onchange="this.form.submit()">
          <c:forEach items="${contentTypes}" var="ctype">
            <c:choose>
              <c:when test="${ctype eq 'default'}">
                <option value="${ctype}" selected="selected">${ctype}</option>
              </c:when>
              <c:otherwise>
                <option value="${ctype}">${ctype}</option>
              </c:otherwise>
            </c:choose>
          </c:forEach>
        </select>
      </form>
    </li>
  </tiles:putAttribute>
  <tiles:putAttribute name="content">
    <div id="sidebar" style="float:right">
      ${renderedSideBar}
    </div>

    <div id="wiki-rendering">
      <c:if test="${pageInfo.renamed}">
        <div class="row">
          <div class="col-sm-4">
            <div class="well">
              <c:choose>
                <c:when test="${pageInfo.renamedInThisWiki}">
                  <a name="renamedTo" href="<c:url value="${renamedUrl}"/>">Renamed to ${sw:urlEncode(pageInfo.renamedPageName)}</a>
                </c:when>
                <c:otherwise>
                  <p>Page has been moved outside of this wiki.</p>
                  <c:if test="${not empty renamedUrl}">
                    <p><a name="renamedTo" href="<c:url value="${renamedUrl}"/>">Moved to ${sw:urlEncode(pageInfo.renamedPageName)}</a></p>
                  </c:if>
                </c:otherwise>
              </c:choose>
            </div>
          </div>
        </div>
      </c:if>
    ${renderedContents}
    </div>
  </tiles:putAttribute>
  <tiles:putAttribute name="content-controls">
    <div class="auxillary">
	    <c:if test="${pageInfo.newPage and empty pageInfo.content}">
	    <div style="margin-top: 1em">
	    <form id="editContent" name="editContent" action="<c:url value="${encodedPageName}"/>" method="post">
	      <input class="btn btn-default" type="submit" value="Edit this new page" />
	    </form>
	    </div>
	    </c:if>
	    <p id="backlinks">
	    <c:if test="${not empty backlinks}">
	      Referenced on:
	      <c:forEach var="backlink" items="${backlinks}">
	        <sw:wikiPage page="${backlink}"/>
	      </c:forEach>
	      <c:if test="${backlinksLimited}">
	        <a href="<sw:wikiUrl page="FindPage"/>?query=${pageInfo.name}&amp;force">...</a>
	      </c:if>
	    </c:if>
	    </p>
      <ul id="bottomNav">
        <c:choose>
	        <c:when test="${pageInfo.locked}">
            <c:choose>
	            <c:when test="${pageInfo.lockedBy == username}">
                <li class="menu">
                  <form id="editBottom" name="editBottom" action="<c:url value="${encodedPageName}"/>" method="post">
	                  <input class="btn btn-default" type="submit" value="Edit"/>
	                </form> 
                </li>
                <li class="menu">
                  <a name="history" href="<c:url value="${encodedPageName}?history"/>">History</a>
                </li>
                <li class="menu" id="lockedInfo">
                  You have locked this page.
                </li>
	            </c:when>
	            <c:otherwise>
                <li class="menu">
                  <a name="history" href="<c:url value="${encodedPageName}?history"/>">History</a>
                </li>
                <li class="menu" id="lockedInfo">
                  Locked for editing by <c:out value="${pageInfo.lockedBy}"/> since <f:formatDate type="both" value="${pageInfo.lockedSince}"/>.
                </li>
  	          </c:otherwise>
	          </c:choose>
            <li class="menu">
              <form id="unlock" name="unlock" action="<c:url value="${encodedPageName}"/>" method="post" style="display:inline">
                <input type="hidden" name="lockToken" value="<c:out value="${pageInfo.lockToken}"/>"/>
                <input class="btn btn-warning" name="unlock" type="submit" value="Unlock"/>
              </form>
            </li>
  	      </c:when>
	        <c:otherwise>
            <li class="menu">
              <form id="editBottom" name="editBottom" action="<c:url value="${encodedPageName}"/>" method="post" style="display:inline;">
                <button name="editButton" type="submit">Edit</button>
              </form>
            </li>
            <li class="menu">
              <a name="attachments" href="<c:url value="${encodedPageName}/attachments/"/>">Attachments</a>
            </li>
            <li class="menu">
              <a name="history" href="<c:url value="${encodedPageName}?history"/>">History</a>
            </li>
	        </c:otherwise>
	      </c:choose>
      </ul>
	    <c:if test="${not empty lastEditAction}">
        <p>
          <a name="lastChanged"  href="<c:url value="${encodedPageName}?revision=${pageInfo.lastChangedRevision}&amp;diff=${pageInfo.lastChangedRevision - 1}"/>">${lastEditAction} by <c:out value="${pageInfo.lastChangedUser}"/> on <f:formatDate type="both" value="${pageInfo.lastChangedDate}"/></a>
        </p>
		  </c:if>
		</div>
  </tiles:putAttribute>
  <tiles:putAttribute name="body-level">
	  <script type="text/javascript">
	    reviki.formAsJavaScriptLink("editBottom", "Edit");
      reviki.formAsJavaScriptLink("editContent", "Edit this new page.");
      reviki.formAsJavaScriptLink("editTop", "Edit");
	  </script>
  </tiles:putAttribute>
</tiles:insertTemplate>
