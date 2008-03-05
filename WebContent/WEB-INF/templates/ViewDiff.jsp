<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="${pageInfo.title} - ${pageInfo.revisionName}"/></tiles:putAttribute>
  <tiles:putAttribute name="heading"><a href="?"><c:out value="${pageInfo.title}"/></a></tiles:putAttribute>
  <tiles:putAttribute name="content">
    <div id="wiki-rendering">
    ${markedUpDiff}
    </div>
    <hr/>
    <p>
      <a href="?history">Full history</a>
    </p>
  </tiles:putAttribute>
</tiles:insertTemplate>
