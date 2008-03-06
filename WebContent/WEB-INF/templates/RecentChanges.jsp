<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Recent Changes</tiles:putAttribute>
  <tiles:putAttribute name="heading">Recent Changes</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <table>
      <tr><th>Date</th><th>Page</th><th>User</th><th>Description</th></tr>
      <c:forEach var="change" items="${recentChanges}">
        <tr>
          <td>
            <f:formatDate type="both" value="${change.date}"/>
          </td>
          <td>
            <c:set var="link">
              <c:choose>
                <c:when test="${change.attachment}">
                  <a href="<c:url value="${change.page}/attachments/${change.name}"/>"><c:out value="${change.name}"/></a>
                </c:when>
                <c:otherwise>
                  <a href="<c:url value="${change.name}"/>"><c:out value="${change.name}"/></a>
                </c:otherwise>
              </c:choose>
            </c:set>
            <c:choose>
              <c:when test="${change.deletion}">
                <del>${link}</del>
              </c:when>
              <c:otherwise>
                ${link}
              </c:otherwise>
            </c:choose>
          </td>
          <td><c:out value="${change.user}"/></td>
          <td>
            <c:choose>
              <c:when test="${change.attachment}">
                <a href="<sw:wikiUrl page="${change.page}"/>/attachments/"><c:out value="${change.description}"/></a>
              </c:when>
              <c:otherwise>
                <a href="<sw:wikiUrl page="${change.page}"/>?revision=${change.revision}&diff=${change.revision - 1}"><c:out value="${change.description}"/></a>
              </c:otherwise>
            </c:choose>
          </td>
        </tr>
      </c:forEach>
    </table>
  </tiles:putAttribute>
</tiles:insertTemplate>
