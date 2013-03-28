<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="${page.title}" /> attachments</tiles:putAttribute>
  <tiles:putAttribute name="heading"><a href="<sw:wikiUrl page="${page}"/>"><c:out value="${page.title}" /> </a> attachments</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <c:choose>
      <c:when test="${not empty currentAttachments}">
        <h4>Current attachments</h4>
        <table>
          <c:forEach var="attachment" items="${currentAttachments}">
            <c:set var="encodedName" value="${sw:urlEncode(attachment.name)}"/>
            <c:set var="ncName" value="${fn:replace(encodedName, '%', '__')}"/>
            <tr>
              <td class="attachmentNameAndDescription">
                <a href="${encodedName}"><c:out value="${attachment.name}" /></a> 
                <c:if test="${not empty attachment.previousVersions}">(latest)</c:if> - <c:out value="${attachment.versions[0].commitMessage}"/>
              </td>
              <td>
                <form name="replaceAttachmentUpload" action="<c:url value=""/>" method="post" enctype="multipart/form-data">
                  <table>
                    <tr>
                      <td class="text-align-right"><label for="file_${ncName}">File:</label></td>
                      <td><input type="file" name="file" id="file_${ncName}"/> 
                          <input type="hidden" name="attachmentName" value="<c:out value="${attachment.name}"/>" />
                          <input type="hidden" name="baseRevision" value="<c:out value="${attachment.revision}"/>" /></td>
                      <td><input type="submit" value="Upload New Version" /></td>
                    </tr>
                    <tr>
                      <td class="text-align-right"><label for="message_${ncName}">Message:</label></td>
                      <td><input type="text" name="attachmentMessage" id="message_${ncName}"/></td>
                    </tr>
                  </table>
                </form>
              </td>
            </tr>
            <tr>
              <td class="attachmentNameAndDescription">
                <c:if test="${not empty attachment.previousVersions}">
                  <ul>
                    <c:forEach var="version" items="${attachment.previousVersions}">
                      <li><a href="<c:url value="${encodedName}?revision=${version.revision}"/>"><c:out value="${attachment.name} (r${version.revision})" /></a>
                    - <c:out value="${version.commitMessage}"/></li>
                    </c:forEach>
                  </ul>
                </c:if>
              </td>
            </tr>
            <tr>
              <td>
                <form name="deleteAttachment" action="<c:url value="${encodedName}"/>" method="post" style="display: inline">
                  <input type="submit" name="delete" value="Delete Attachment" />
                </form>
              </td>
            </tr>
          </c:forEach>
        </table>
      </c:when>
      <c:otherwise>
        <h4>No files currently attached to the page.</h4>
      </c:otherwise>
    </c:choose>
    <c:if test="${not empty deletedAttachments}">
      <h4>Deleted attachments</h4>
      <table>
        <c:forEach var="attachment" items="${deletedAttachments}">
          <c:set var="encodedName" value="${sw:urlEncode(attachment.name)}"/>
          <tr>
            <td><c:out value="${attachment.name}" /></td>
          </tr>
          <tr>
            <td class="attachmentNameAndDescription">
              <c:if test="${not empty attachment.versions}">
                <ul>
                  <c:forEach var="version" items="${attachment.versions}">
                    <c:if test="${version.isDeletion}">
                      <li><span style="text-decoration: line-through"><c:out value="${attachment.name} (r${version.revision})" /></span>
                      - <c:out value="${version.commitMessage}"/></li>
                    </c:if>
                    <c:if test="${not version.isDeletion}">
                      <li><a href="<c:url value="${encodedName}?revision=${version.revision}"/>"><c:out value="${attachment.name} (r${version.revision})" /></a>
                      - <c:out value="${version.commitMessage}"/></li>
                    </c:if>
                  </c:forEach>
                </ul>
              </c:if>
            </td>
          </tr>
        </c:forEach>
      </table>
    </c:if>
    <h4>Upload a new attachment</h4>
    <form name="attachmentUpload" action="<c:url value=""/>" method="post" enctype="multipart/form-data">
      <table>
        <tr><th class="text-align-right"><label for="file">File to upload</label></th>
          <td><input id="file" type="file" name="file" /></td>
        </tr>
        <tr>
          <th class="text-align-right"><label for="attachmentName">Attachment name (optional)</label></th>
          <td><input id="attachmentName" type="text" name="attachmentName" /></td>
        </tr>
        <tr>
          <th class="text-align-right"><label for="attachmentMessage">Message (optional)</label></th>
          <td><input id="attachmentMessage" type="text" name="attachmentMessage" /></td>
        </tr>
        <tr>
          <td></td>
          <td><input type="submit" value="Upload" /></td>
        </tr>
      </table>
    </form>
  </tiles:putAttribute>
</tiles:insertTemplate>
