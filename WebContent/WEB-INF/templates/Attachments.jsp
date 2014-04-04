<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="${page.title}" /> attachments</tiles:putAttribute>
  <tiles:putAttribute name="heading"><a href="<sw:wikiUrl page="${page}"/>"><c:out value="${page.title}" /> </a> attachments</tiles:putAttribute>
  <tiles:putAttribute name="content">
  <!--upload an attachment-->
    <div class="container">
      <div class="row">
        <div class="col-sm-12">
          <div class="panel panel-default">
            <div class="panel-heading">
              <h3 class="panel-title">Upload a new attachment</h3>
            </div><!--heading-->
            <div class="panel-body">
              <form class="form-horizontal" name="attachmentUpload" action="<c:url value=""/>" method="post" enctype="multipart/form-data" role="form">
                <div class="form-group">
                  <label for="attachmentName" class="col-sm-4 control-label">Attachment name</label>
                  <div class="col-sm-6">
                    <input type="text" class="form-control" name="attachmentName" id="attachmentName" placeholder="optional">
                  </div>
                </div>
                <div class="form-group">
                  <label for="attachmentMessage" class="col-sm-4 control-label">Message</label>
                  <div class="col-sm-6">
                    <input type="text" class="form-control" name="attachmentMessage" id="attachmentMessage" placeholder="optional">
                  </div>
                </div>
                <div class="form-group">
                  <div class="col-sm-offset-4 col-sm-8">
                    <input type="file" id="file" name="file">
                  </div>
                </div>
                <div class="form-group">
                  <div class="col-sm-offset-4 col-sm-8">
                    <button type="submit" name="upload" class="btn btn-default">Upload</button>
                  </div>
                </div>
              </form>
            </div><!--panelbody-->
          </div><!--panel-->
        </div><!--col-->
      </div><!--row-->

      <c:choose>
        <c:when test="${not empty currentAttachments}">
          <!--current attachments-->
          <div class="row">
            <div class="col-sm-12">
              <div class="panel panel-default">
                <div class="panel-heading">
                  <h3 class="panel-title">Current attachments</h3>
                </div><!--heading-->
                <div class="panel-body">
                  <c:forEach var="attachment" items="${currentAttachments}">
                    <c:set var="encodedName" value="${sw:urlEncode(attachment.name)}"/>
                    <c:set var="ncName" value="${fn:replace(encodedName, '%', '__')}"/>
                    <!--attachment-row-->
                    <div class="row">
                      <div class="col-sm-6 col-sm-offset-1">
                        <div class="row">
                          <h3><a href="${encodedName}"><c:out value="${attachment.name}" /></a>
                              <c:if test="${not empty attachment.previousVersions}">(latest)</c:if>
                              <form role="form" class="form-inline" name="deleteAttachment" action="<c:url value="${encodedName}"/>" method="post" style="display: inline">
                                <button class="btn btn-xs btn-danger attachdelete" name="delete">Delete</button>
                              </form>
                          </h3>
                          <p><c:out value="${attachment.versions[0].commitMessage}"/></p>
                        </div><!--row-->
                        <c:if test="${not empty attachment.previousVersions}"> 
                          <div class="row">
                            <ul>
                              <c:forEach var="version" items="${attachment.previousVersions}">
                                <li>
                                  <a href="<c:url value="${encodedName}?revision=${version.revision}"/>"><c:out value="${attachment.name} (r${version.revision})" /></a><br>
                                  <c:out value="${version.commitMessage}"/>
                                </li>
                              </c:forEach>
                            </ul>
                          </div><!--row-->
                        </c:if>
                      </div><!--col-->
                      <div class="col-sm-5">
                        <div class="panel panel-default">
                          <div class="panel-heading">
                            <div class="panel-title">Upload a new version</div>
                          </div>
                          <div class="panel-body">
                            <div class="row">
                              <div class="col-sm-10 col-sm-offset-1">
                                <form name="replaceAttachmentUpload" class="form-horizontal" role="form" action="<c:url value=""/>" method="post" enctype="multipart/form-data">
                                  <div class="form-group">
                                    <input id="message_${ncName}" type="text" name="attachmentMessage" class="form-control input-xs" placeholder="Message">
                                  </div><!--formgroup-->
                                  <div class="form-group">
                                    <input type="file" name="file" id="file_${ncName}">
                                  </div><!--formgroup-->
                                  <div class="form-group">
                                    <input type="submit" class="btn btn-default" value="Upload">
                                  </div><!--formgroup-->
                                  <!--hiddenparams-->
                                  <input type="hidden" name="attachmentName" value="<c:out value="${attachment.name}"/>">
                                  <input type="hidden" name="baseRevision" value="<c:out value="${attachment.revision}"/>">
                                </form>
                              </div>
                            </div>
                          </div>
                        </div><!--panel-->
                      </div><!--col-->
                    </div><!--row-->
                  </c:forEach>
                </div><!--panelbody-->
              </div><!--panel-->
            </div><!--col-->
          </div><!--row-->
        </c:when>
        <c:otherwise>
          <h4>No files currently attached to the page.</h4>
        </c:otherwise>
      </c:choose>
      <c:if test="${not empty deletedAttachments}">
        <div class="row">
          <div class="col-sm-12">
            <div class="panel panel-default">
              <div class="panel-heading">
                <h3 class="panel-title">Deleted attachments</h3>
              </div><!--heading-->
              <div class="panel-body">
                <c:forEach var="attachment" items="${deletedAttachments}">
                  <c:set var="encodedName" value="${sw:urlEncode(attachment.name)}"/>
                  <div class="row">
                    <div class="col-sm-10 col-sm-offset-1">
                      <div class="row">
                        <h3><c:out value="${attachment.name}" /></h3>
                      </div><!--namerow-->
                      <c:if test="${not empty attachment.versions}">
                        <div class="row">
                          <ul>
                            <c:forEach var="version" items="${attachment.versions}">
                              <c:if test="${version.isDeletion}">
                                <li>
                                  <span style="text-decoration: line-through"><c:out value="${attachment.name} (r${version.revision})" /></span><br>
                                  <c:out value="${version.commitMessage}"/>
                                </li>
                              </c:if>
                              <c:if test="${not version.isDeletion}">
                                <li>
                                  <a href="<c:url value="${encodedName}?revision=${version.revision}"/>"><c:out value="${attachment.name} (r${version.revision})" /></a><br>
                                  <c:out value="${version.commitMessage}"/>
                                </li>
                              </c:if>
                            </c:forEach>
                          </ul>
                        </div><!--row-->
                      </c:if>
                    </div><!--col-->
                  </div><!--row-->
                </c:forEach>
              </div><!--body-->
            </div><!--panel-->
          </div><!--col-->
        </div><!--row-->
      </c:if>
    </div><!--container-->
  </tiles:putAttribute>
</tiles:insertTemplate>
