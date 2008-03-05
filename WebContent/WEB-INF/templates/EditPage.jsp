<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="svnwiki - ${pageInfo.title} - ${pageInfo.revisionName}"/></tiles:putAttribute>
  <tiles:putAttribute name="heading"><c:out value="${pageInfo.title}"/></tiles:putAttribute>
  <tiles:putAttribute name="content">
    <c:if test="${not empty preview}">
      <h2>Preview <a href="#editForm">(skip)</a></h2>
      <div id="wiki-rendering">
      ${preview}
      </div>
    </c:if>
    
    <form id="editForm" name="editForm" action="" method="post">
      <textarea rows="25" cols="80" name="content"><c:out value="${pageInfo.content}"/></textarea>
      <input type="hidden" name="baseRevision" value="<c:out value="${pageInfo.revision}"/>"/>
      <input type="hidden" name="lockToken" value="<c:out value="${pageInfo.lockToken}"/>"/>
      <hr/>
      <label for="description">Describe your change</label><input style="width:19em;margin-left:0.2em;margin-right:0.2em;" id="description" name="description" type="text"/>
      <input style="width:5em;" name="preview" type="submit" value="Preview"/>
      <input style="width:5em;" name="save" type="submit" value="Save"/>
      <input style="width:5em;" name="cancel" type="submit" value="Cancel"/>
      <br />
      <label for="minorEdit">Minor edit?</label><input type="checkbox" id="minorEdit" name="minorEdit"/>
    </form>
    <c:if test="${empty preview}">
      <script type='text/javascript'>document.editForm.content.focus();</script>
    </c:if>
    <jsp:include page="cheatsheet.html"></jsp:include>
  </tiles:putAttribute>
</tiles:insertTemplate>
