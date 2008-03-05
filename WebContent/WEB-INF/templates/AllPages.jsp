<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">All Pages</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <h1>All Pages</h1>
    <ul>
      <c:forEach var="path" items="${pageList}">
        <li><a href="<c:url value="${path}"/>"><c:out value="${path}"/></a></li>
      </c:forEach>
    </ul>
  </tiles:putAttribute>
</tiles:insertTemplate>
