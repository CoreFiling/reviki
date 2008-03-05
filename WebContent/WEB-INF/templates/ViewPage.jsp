<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="${pageInfo.title} - ${pageInfo.revisionName}"/></tiles:putAttribute>
  <tiles:putAttribute name="heading"><c:out value="${pageInfo.title}"/></tiles:putAttribute>
  <tiles:putAttribute name="menuItems">
    <c:if test="${not(pageInfo.locked) or pageInfo.lockedBy == username}">
      <li class="menu">
        <form action="" method="post" style="display:inline;">
          <input type="submit" value="Edit"/>
        </form>
      </li>
      <li class="menu" style="margin-right: 0.5em; padding-right: 0.5em; border-right: 1px solid black;">
        <a href="${page.path}/attachments/">Attachments</a>
      </li>
    </c:if>
  </tiles:putAttribute>
  <tiles:putAttribute name="content">
    <div id="wiki-rendering">
    ${renderedContents}
    </div>
    <hr/>
    <c:if test="${not empty backlinks}">
      <p>
      Referenced on:
      <c:forEach var="backlink" items="${backlinks}">
        <a href="<c:out value="${backlink}"/>"><c:out value="${backlink}"/></a>
      </c:forEach>
      <c:if test="${backlinksLimited}">
        <a href="<c:url value="/search?query=${pageInfo.path}&force"/>">...</a>
      </c:if>
      </p>
    </c:if>
    <c:choose>
      <c:when test="${pageInfo.locked}">
        <c:choose>
          <c:when test="${pageInfo.lockedBy == username}">
            <form action="" method="post">
              <input type="submit" value="Edit"/>
            </form> 
            <p>You have locked this page.</p>
          </c:when>
          <c:otherwise>
            <p>Locked for editing by <c:out value="${pageInfo.lockedBy}"/>.</p>
          </c:otherwise>
        </c:choose>
      </c:when>
      <c:otherwise>
        <form name="editForm" action="" method="post" style="display:inline;">
          <input name="editButton" type="submit" value="Edit"/>
        </form><a href="${page.path}/attachments/">Attachments</a>
      </c:otherwise>
    </c:choose>
    <c:if test="${not pageInfo.new}">
      <p>
        <a href="?diff=${pageInfo.lastChangedRevision - 1}">Last changed by <c:out value="${pageInfo.lastChangedUser}"/> on <f:formatDate type="both" value="${pageInfo.lastChangedDate}"/></a> (<a href="?history">full history</a>).
      </p>
    </c:if>
  </tiles:putAttribute>
</tiles:insertTemplate>
