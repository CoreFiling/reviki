<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Recent changes</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <h1>Recent changes</h1>
    <table>
      <tr><th>Date</th><th>Page</th><th>User</th><th></th></tr>
      <c:forEach var="change" items="${recentChanges}">
        <tr>
          <td><f:formatDate type="both" value="${change.date}"/></td>
          <td><a href="<c:url value="${change.page}"/>"><c:out value="${change.page}"/></a></td>
          <td><c:out value="${change.user}"/></td>
          <td><a href="<c:url value="${change.page}?revision=${change.revision}&diff=${change.revision - 1}"/>">Show change</a></td>
        </tr>
      </c:forEach>
    </table>
  </tiles:putAttribute>
</tiles:insertTemplate>
