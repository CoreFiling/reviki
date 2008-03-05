<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">History for <c:out value="${page.title}"/></tiles:putAttribute>
  <tiles:putAttribute name="heading">History for <a href="<c:url value="${page.path}"/>"><c:out value="${page.title}"/></a></tiles:putAttribute>
  <tiles:putAttribute name="content">
    <table>
      <tr><th>Date</th><th>Name</th><th>User</th><th>Description</th></tr>
      <c:forEach var="change" items="${changes}">
        <tr>
          <td>
            <c:choose>
              <c:when test="${change.deletion}">
                <del><f:formatDate type="both" value="${change.date}"/></del>
              </c:when>
              <c:otherwise>
                <f:formatDate type="both" value="${change.date}"/>
              </c:otherwise>
            </c:choose>
          </td>
          <td>
            <c:choose>
              <c:when test="${change.deletion}">
                <del><a href="<c:url value="${change.name}"/>"><c:out value="${change.name}"/></a></del>
              </c:when>
              <c:otherwise>
                <a href="<c:url value="${change.name}"/>"><c:out value="${change.name}"/></a>
              </c:otherwise>
            </c:choose>
          </td>
          <td><c:out value="${change.user}"/></td>
          <td><a href="<c:url value="${change.name}?revision=${change.revision}&diff=${change.revision - 1}"/>"><c:out value="${change.description}"/></a></td>
        </tr>
      </c:forEach>
    </table>
  </tiles:putAttribute>
</tiles:insertTemplate>
