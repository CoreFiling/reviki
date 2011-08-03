<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw"%>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="${page.title}" /> attachments</tiles:putAttribute>
  <tiles:putAttribute name="heading"><a href="<sw:wikiUrl page="${page}"/>"><c:out value="${page.title}" /> </a> attachments</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <c:choose>
      <c:when test="${not empty currentAttachments}">
        <h4>Current attachments</h4>
        <table>
          <c:forEach var="attachment" items="${currentAttachments}">
            <tr>
              <td class="attachmentNameAndDescription">
                <a href="<c:url value="${attachment.name}"/>"><c:out value ="${attachment.name}" /></a> 
                <c:if test="${not empty attachment.previousVersions}">(latest)</c:if> - <c:out value="${attachment.versions[0].commitMessage}"/>
              </td>
              <td>
                <form name="replaceAttachmentUpload" action="<c:url value=""/>" method="post" enctype="multipart/form-data">
                  <table>
                    <tr>
                      <td class="text-align-right"><label for="file_${attachment.name}">File:</label></td>
                      <td><input type="file" name="file" id="file_${attachment.name}"/> 
                          <input type="hidden" name="attachmentName" value="<c:out value="${attachment.name}"/>" />
                          <input type="hidden" name="baseRevision" value="<c:out value="${attachment.revision}"/>" /></td>
                      <td><input type="submit" value="Upload new version" /></td>
                    </tr>
                    <tr>
                      <td class="text-align-right"><label for="message_${attachment.name}">Message:</label></td>
                      <td><input type="text" name="attachmentMessage" id="message_${attachment.name}"/></td>
                    </tr>
                  </table>
                </form>
              </td>
              <td><a href="<c:url value="${attachment.name}?delete"/>">delete</a>
              </td>
            </tr>
            <tr>
              <td class="attachmentNameAndDescription">
                <c:if test="${not empty attachment.previousVersions}">
                  <ul>
                    <c:forEach var="version" items="${attachment.previousVersions}">
                      <li><a href="<c:url value="${attachment.name}?revision=${version.revision}"/>"><c:out value="${attachment.name} (r${version.revision})" /></a>
                    - <c:out value="${version.commitMessage}"/></li>
                    </c:forEach>
                  </ul>
                </c:if>
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
          <tr>
            <td><c:out value="${attachment.name}" /></td>
          </tr>
          <tr>
            <td class="attachmentNameAndDescription">
              <c:if test="${not empty attachment.versions}">
                <ul>
                  <c:forEach var="version" items="${attachment.versions}">
                    <li><a href="<c:url value="${attachment.name}?revision=${version.revision}"/>"><c:out value="${attachment.name} (r${version.revision})" /></a>
                    - <c:out value="${version.commitMessage}"/></li>
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