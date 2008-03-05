<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Error</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <h1>An error has occurred.</h1>
    <p><c:out value="${exception.message}"/></p>
  </tiles:putAttribute>
</tiles:insertTemplate>
