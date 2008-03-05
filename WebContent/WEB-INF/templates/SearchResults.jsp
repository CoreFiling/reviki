<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Search results</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <h1>Search results for '<c:out value="${param.query}"/>'</h1>
    <ul>
      <c:forEach var="path" items="${results}">
        <li><a href="<c:url value="/pages/${path}"/>"><c:out value="${path}"/></a></li>
      </c:forEach>
    </ul>
  </tiles:putAttribute>
</tiles:insertTemplate>
