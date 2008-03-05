<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="${pageInfo.title} - ${pageInfo.revisionName}"/></tiles:putAttribute>
  <tiles:putAttribute name="content">
    <h1><a href="?"><c:out value="${pageInfo.title}"/></a></h1>
    <div id="wiki-rendering">
    ${markedUpDiff}
    </div>
    <hr/>
  </tiles:putAttribute>
</tiles:insertTemplate>
