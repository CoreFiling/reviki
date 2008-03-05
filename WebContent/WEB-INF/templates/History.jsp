<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">History for <c:out value="${page.title}"/></tiles:putAttribute>
  <tiles:putAttribute name="content">
    <h1>History for <a href="<c:url value="${page.path}"/>"><c:out value="${page.title}"/></a></h1>
    <table>
      <tr><th>Date</th><th>User</th><th>Description</th></tr>
      <c:forEach var="change" items="${changes}">
        <tr>
          <td><f:formatDate type="both" value="${change.date}"/></td>
          <td><c:out value="${change.user}"/></td>
          <td><a href="<c:url value="${change.page}?revision=${change.revision}&diff=${change.revision - 1}"/>"><c:out value="${change.description}"/></a></td>
        </tr>
      </c:forEach>
    </table>
  </tiles:putAttribute>
</tiles:insertTemplate>
