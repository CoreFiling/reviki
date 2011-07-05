<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Recent Changes</tiles:putAttribute>
  <tiles:putAttribute name="heading">Recent Changes</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <table class="history">
      <tr class="history">
        <th class="history" style="white-space: nowrap;">Date</th>
        <th class="history">Page</th>
        <th class="history">User</th>
        <th class="history">Description</th>
      </tr>
      <c:forEach var="change" items="${recentChanges}">
        <tr class="history">
          <td class="history" style="white-space: nowrap;">
            <c:choose>
              <c:when test="${change.attachment}">
                <a href="<sw:wikiUrl page="${change.page}"/>/attachments/">
                  <f:formatDate type="both" value="${change.date}"/>
                </a>
              </c:when>
              <c:otherwise>
                <a href="<c:url value="${change.name}"/>?revision=${change.revision}&amp;diff=${change.revision - 1}">
                  <f:formatDate type="both" value="${change.date}"/>
                </a>
              </c:otherwise>
            </c:choose>
          </td>
          <td class="history">
            <c:set var="link">
              <c:choose>
                <c:when test="${change.attachment}">
                  <a href="<c:url value="${change.page}/attachments/${change.name}"/>"><c:out value="${change.page}/attachments/${change.name}"/></a>
                </c:when>
                <c:otherwise>
                  <a href="<c:url value="${change.name}"/>"><c:out value="${change.name}"/></a>
                </c:otherwise>
              </c:choose>
            </c:set>
            <c:set var="revisionLink">
              <c:choose>
                <c:when test="${change.attachment}">
                  <a href="<c:url value="${change.page}/attachments/${change.name}?revision=${change.revision}"/>">r<c:out value="${change.revision}"/></a>
                </c:when>
                <c:otherwise>
                  <a href="<c:url value="${change.name}?revision=${change.revision}"/>">r<c:out value="${change.revision}"/></a>
                </c:otherwise>
              </c:choose>
            </c:set>
            <c:choose>
              <c:when test="${change.deletion}">
                <del>${link}</del> <del>r${change.revision}</del>
              </c:when>
              <c:otherwise>
                ${link} ${revisionLink}
              </c:otherwise>
            </c:choose>
          </td>
          <td class="history"><c:out value="${change.user}"/></td>
          <td class="history">
            <c:out value="${change.description}"/>
          </td>
        </tr>
      </c:forEach>
    </table>
  </tiles:putAttribute>
</tiles:insertTemplate>
