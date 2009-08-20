<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw" %>

<c:set var="lastEditAction">
  <c:choose>
    <c:when test="${pageInfo.deleted}">Deleted</c:when>
    <c:when test="${not pageInfo.new}">Last changed</c:when>
  </c:choose>
</c:set>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="${pageInfo.title} - ${pageInfo.revisionName}"/></tiles:putAttribute>
  <tiles:putAttribute name="heading"><c:out value="${pageInfo.title}"/><c:if test="${showHeadRev}"> - r${pageInfo.revision}</c:if></tiles:putAttribute>
  <tiles:putAttribute name="menuItems">
    <c:if test="${not pageInfo.locked or pageInfo.lockedBy == username}">
      <li class="menu">
        <form id="editTop" name="editTop" action="<c:url value="${page.name}"/>" method="post" style="display:inline;">
          <input type="submit" value="Edit"/>
        </form>
      </li>
      <li class="menu">
        <a href="<c:url value="${page.name}/attachments/"/>">Attachments</a>
      </li>
      <c:if test="${not pageInfo.new}">
        <li class="menu">
          <a name="rename" href="<c:url value="${page.name}?rename"/>">Rename</a>
        </li>
        <li class="menu">
          <a name="copy" href="<c:url value="${page.name}?copy"/>">Copy</a>
        </li>
      </c:if>
    </c:if>
    <c:if test="${not empty lastEditAction}">
	    <li class="menu">
	      <a name="history" href="<c:url value="${page.name}?history"/>">History</a>
	    </li>
    </c:if>
  </tiles:putAttribute>
  <tiles:putAttribute name="content">
    <div id="wiki-rendering">
    ${renderedContents}
    </div>
    <div class="auxillary">
	    <c:if test="${pageInfo.new and empty pageInfo.content}">
	    <div style="margin-top: 1em">
	    <form id="editContent" name="editContent" action="<c:url value="${page.name}"/>" method="post">
	      <input type="submit" value="Edit this new page" />
	    </form>
	    </div>
	    </c:if>
	    <hr />
	    <p id="backlinks">
	    <c:if test="${not empty backlinks}">
	      Referenced on:
	      <c:forEach var="backlink" items="${backlinks}">
	        <sw:wikiLink page="${backlink}"/>
	      </c:forEach>
	      <c:if test="${backlinksLimited}">
	        <a href="<sw:wikiUrl page="FindPage"/>?query=${pageInfo.name}&amp;force">...</a>
	      </c:if>
	    </c:if>
	    </p>
	    <c:choose>
	      <c:when test="${pageInfo.locked}">
	        <c:choose>
	          <c:when test="${pageInfo.lockedBy == username}">
	            <form id="editBottom" name="editBottom" action="<c:url value="${page.name}"/>" method="post">
	              <input type="submit" value="Edit"/>
	            </form> 
	            <p id="lockedInfo">You have locked this page.</p>
	          </c:when>
	          <c:otherwise>
	            <p id="lockedInfo">Locked for editing by <c:out value="${pageInfo.lockedBy}"/> since <f:formatDate type="both" value="${pageInfo.lockedSince}"/>.</p>
	          </c:otherwise>
	        </c:choose>
          <form id="unlock" name="unlock" action="<c:url value="${page.name}"/>" method="post" style="display:inline;">
            <input type="hidden" name="lockToken" value="<c:out value="${pageInfo.lockToken}"/>"/>
            <input name="unlock" type="submit" value="Unlock"/>
          </form>
	      </c:when>
	      <c:otherwise>
	        <form id="editBottom" name="editBottom" action="<c:url value="${page.name}"/>" method="post" style="display:inline;">
	          <input name="editButton" type="submit" value="Edit"/>
	        </form><a href="<c:url value="${page.name}/attachments/"/>">Attachments</a>
	      </c:otherwise>
	    </c:choose>
	    <c:if test="${not empty lastEditAction}">
		    <p>
		      <a href="<c:url value="${page.name}?revision=${pageInfo.lastChangedRevision}&amp;diff=${pageInfo.lastChangedRevision - 1}"/>">${lastEditAction} by <c:out value="${pageInfo.lastChangedUser}"/> on <f:formatDate type="both" value="${pageInfo.lastChangedDate}"/></a> <a name="history" href="<c:url value="${page.name}?history"/>">[History]</a>
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
