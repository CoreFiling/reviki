<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Recent changes</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <h1>Recent changes</h1>
    <ul>
      <c:forEach var="change" items="${recentChanges}">
        <li><a href="<c:url value="${change.page}"/>">
          <c:out value="${change.page}"/></a>
           on <f:formatDate type="both" value="${change.date}"/>
           by <c:out value="${change.user}"/>
        </li>
      </c:forEach>
    </ul>
  </tiles:putAttribute>
</tiles:insertTemplate>
