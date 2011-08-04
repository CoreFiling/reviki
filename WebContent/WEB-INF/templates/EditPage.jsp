<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="*${pageInfo.title} - ${pageInfo.revisionName}"/></tiles:putAttribute>
  <tiles:putAttribute name="heading"><c:out value="${pageInfo.title}"/></tiles:putAttribute>
  <tiles:putAttribute name="content">
  <script type='text/javascript'>
    $(document).ready(function() {
      $("#previewTabs").tabs();
      $("#editFormTabs").tabs();
    });
  </script>

    <c:if test="${not empty preview or not empty markedUpDiff}">
      <div id="previewTabs">
        <ul id="previewTab-header">
          <c:if test="${not empty preview}">
            <li><a href="#preview-area">Preview</a></li>
          </c:if>
          <c:if test="${not empty markedUpDiff}">
            <li><a href="#diff-area">Diff</a></li>
          </c:if>
        </ul>

        <c:if test="${not empty preview}">
          <div id="preview-area" class="tab-content">
            <div id="wiki-rendering">${preview}</div>
          </div>
        </c:if>
        <c:if test="${not empty markedUpDiff}">
          <div id="diff-area" class="tab-content">
            <div id="markedUpDiff">${markedUpDiff}</div>
          </div>
        </c:if>
      </div>
    </c:if>

    <form id="editForm" name="editForm" action="<c:url value="${sw:urlEncode(page.name)}"/>" style="clear:left" method="post">
      <div id="editFormTabs">
        <ul id="editFormTab-header">
          <li><a id="editFormContent-link" href="#editFormContent-area">Content</a></li>
          <li><a id="editFormAttributes-link" href="#editFormAttributes-area">Attributes</a></li>
        </ul>
        <div id="editFormContent-area" class="tab-content">
          <textarea rows="25" cols="80" id="content" name="content"><c:out value="${pageInfo.content}"/></textarea>
        </div>
        <div id="editFormAttributes-area" class="tab-content">
          <textarea rows="10" cols="80" id="attributes" name="attributes"><c:forEach var="entry" items="${pageInfo.attributes}">"${entry.key}" = "${entry.value}"&#10;</c:forEach></textarea><br />
        </div>
      </div>
      <input type="hidden" name="baseRevision" value="<c:out value="${pageInfo.revision}"/>"/>
      <input type="hidden" name="lockToken" value="<c:out value="${pageInfo.lockToken}"/>"/>
      <input type="hidden" name="sessionId" value="<c:out value="${sessionId}"/>"/>
      <c:set var="attrs" value=""/>
      <c:forEach var="entry" items="${originalAttributes}">
        <c:set var="attrs" value="${attrs} ${entry.key} &#10;"/>
      </c:forEach>
      <input type="hidden" name="originalAttrs" value="${attrs}" />
      <hr/>
      <label for="description">Describe your change</label><input style="width:19em;margin-left:0.2em;margin-right:0.2em;" id="description" name="description" type="text" value="<c:out value="${param.description}"/>"/>
      <input style="width:5em;" name="save" type="submit" value="Save" id="save"/>
      <input style="width:5em;" name="unlock" type="submit" value="Cancel" id="cancel"/>
      <input style="width:5em;" name="preview" type="submit" value="Preview" id="preview"/>
      <br />
      <label for="minorEdit">Minor edit?</label>
      <input type="checkbox" id="minorEdit" name="minorEdit" <c:if test="${not empty param.minorEdit}">checked="checked"</c:if> />
    </form>
    <c:if test="${empty preview}">
      <script type='text/javascript'>
      $(document).ready(function() {
        $("#content").focus();
      });
      </script>
    </c:if>
    <jsp:include page="cheatsheet.html"></jsp:include>
  </tiles:putAttribute>
</tiles:insertTemplate>
