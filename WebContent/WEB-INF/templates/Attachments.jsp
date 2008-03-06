<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="${page.title}"/> attachments</tiles:putAttribute>
  <tiles:putAttribute name="heading"><a href="<sw:wikiUrl page="${page.path}"/>"><c:out value="${page.title}"/></a> attachments</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <c:choose>
      <c:when test="${not empty attachments}">
      <h4>Current attachments</h4>
      <table>
        <c:forEach var="attachment" items="${attachments}">
          <tr>
            <td>
              <a href="<c:url value="${attachment.name}"/>"><c:out value="${attachment.name}"/></a>
              <c:if test="${not empty attachment.previousVersions}">
               (latest)
              </c:if>
           </td>
            <td>
              <form name="replaceAttachmentUpload" action="" method="post" enctype="multipart/form-data">
                <input id="file" type="file" name="file"/>
                <input type="hidden" name="attachmentName" value="<c:out value="${attachment.name}"/>"/>
                <input type="hidden" name="baseRevision" value="<c:out value="${attachment.revision}"/>"/>
                <input type="submit" value="Upload new version"/>
              </form>
            </td>
          </tr>
          <tr>
            <td>
              <ul>
                <c:forEach var="version" items="${attachment.previousVersions}">
                  <li>
                    <a href="<c:url value="${attachment.name}?revision=${version.revision}"/>"><c:out value="${attachment.name} (r${version.revision})"/></a>
                  </li>
                </c:forEach>
              </ul>
            </td>
          </tr>
        </c:forEach>
      </table>
    </c:when>
    <c:otherwise>
      <h4>No attachments yet.</h4>
    </c:otherwise>
    </c:choose>
    <h4>Upload a new attachment</h4>
    <form name="attachmentUpload" action="" method="post" enctype="multipart/form-data">
      <table>
        <tr><th style="text-align: right;"><label for="file">File to upload</label></th><td><input id="file" type="file" name="file"/></td></tr>
        <tr><th style="text-align: right;"><label for="attachmentName">Attachment name (optional)</label></th><td><input id="attachmentName" type="text" name="attachmentName"/></td></tr>
        <tr><td></td><td><input type="submit" value="Upload"/></td></tr>
      </table>      
    </form>
  </tiles:putAttribute>
</tiles:insertTemplate>
