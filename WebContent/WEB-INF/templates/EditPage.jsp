<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="svnwiki - ${pageInfo.title} - ${pageInfo.revisionName}"/></tiles:putAttribute>
  <tiles:putAttribute name="content">
    <h1><c:out value="${pageInfo.title}"/></h1>
    <form name="edit" action="" method="post">
      <textarea rows="25" cols="80" name="content"><c:out value="${pageInfo.content}"/></textarea>
      <input type="hidden" name="baseRevision" value="<c:out value="${pageInfo.revision}"/>"/>
      <input type="hidden" name="lockToken" value="<c:out value="${pageInfo.lockToken}"/>"/>
      <hr/>
      <label for="description">Describe your change</label><input style="width:19em;margin-left:0.2em;margin-right:0.2em;" id="description" name="description" type="text"/>
      <input style="width:5em;" name="action" type="submit" value="Save"/>
      <input style="width:5em;" name="action" type="submit" value="Cancel"/>
    </form>
    <script type='text/javascript'>document.edit.content.focus();</script>
  </tiles:putAttribute>
</tiles:insertTemplate>
