<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="svnwiki - ${pageInfo.path} - ${pageInfo.revisionName}"/></tiles:putAttribute>
  <tiles:putAttribute name="content">
    <h1><c:out value="${pageInfo.path}"/></h1>
    <form action="" method="post">
      <textarea rows="25" cols="80" name="content"><c:out value="${pageInfo.content}"/></textarea>
      <input type="hidden" name="baseRevision" value="<c:out value="${pageInfo.revision}"/>"/>
      <hr/>
      <input style="width:5em;" name="action" type="submit" value="Save"/>
      <input style="width:5em;" name="action" type="submit" value="Cancel"/>
    </form> 
  </tiles:putAttribute>
</tiles:insertTemplate>
