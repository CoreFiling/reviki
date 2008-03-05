<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="${pageInfo.path} - ${pageInfo.revisionName}"/></tiles:putAttribute>
  <tiles:putAttribute name="content">
    <h1><c:out value="${pageInfo.path}"/></h1>
    <div id="wiki-rendering">
    ${renderedContents}
    </div>
    <hr/>
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
        <form action="" method="post">
          <input type="submit" value="Edit"/>
        </form> 
      </c:otherwise>
    </c:choose>
    <c:if test="${not pageInfo.new}">
      <p>
      <a href="?diff=${pageInfo.lastChangedRevision - 1}">Last changed by <c:out value="${pageInfo.lastChangedUser}"/> on <f:formatDate type="both" value="${pageInfo.lastChangedDate}"/>.</a>
      </p>
    </c:if>
  </tiles:putAttribute>
</tiles:insertTemplate>
