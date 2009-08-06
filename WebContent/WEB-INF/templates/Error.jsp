<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Error</tiles:putAttribute>
  <tiles:putAttribute name="heading">An error has occurred</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <p><c:out value="${exception.message}"/></p>
    <p><a href="javascript:history.go(-1)">Go back</a></p>
  </tiles:putAttribute>
</tiles:insertTemplate>
