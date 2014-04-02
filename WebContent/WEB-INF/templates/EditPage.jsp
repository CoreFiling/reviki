<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="*${pageInfo.title} - ${pageInfo.revisionName}"/></tiles:putAttribute>
  <tiles:putAttribute name="heading"><c:out value="${pageInfo.title}"/></tiles:putAttribute>
  <tiles:putAttribute name="content">
  <div class="container">
    <c:if test="${not empty preview or not empty markedUpDiff}">
      <div class="row">
        <div class="col-sm-9">
          <ul class="nav nav-tabs">
            <c:if test="${not empty preview}">
              <li class="active"><a href="#preview-area" data-toggle="tab">Preview</a></li>
            </c:if>
            <c:if test="${not empty markedUpDiff}">
              <li><a href="#diff-area" data-toggle="tab">Diff</a></li>
            </c:if>
          </ul>

          <div class="tab-content">
            <c:if test="${not empty preview}">
              <div class="tab-pane active" id="preview-area">
                <div class="well">
                  <div class="panel panel-default">
                    <div id="wiki-rendering">${preview}</div>
                  </div>
                </div>
              </div>
            </c:if>
            <c:if test="${not empty markedUpDiff}">
              <div id="diff-area" class="tab-pane">
                <div class="well">
                  <div class="panel panel-default">
                    ${markedUpDiff}
                  </div>
                </div>
              </div>
            </c:if>
          </div><!--tabcontent-->
        </div><!--col-->
      </div><!--row-->
    </c:if>

    <form class="form" role="form" name="editForm" action="<c:url value="${sw:urlEncode(page.name)}"/>" method="post">
      <div class="form-group">
        <div class="row">
          <div class="col-sm-9">
            <ul class="nav nav-tabs">
              <li class="active"><a href="#content" data-toggle="tab">Content</a></li>
              <li><a href="#attributes" data-toggle="tab">Attributes</a></li>
            </ul>
          </div><!--col-->
        </div><!--row-->

        <div class="tab-content">
          <div class="tab-pane active" id="content">
            <div class="row">
              <div class="col-sm-9">
                <div class="well">
                  <textarea id="contentArea" style="resize: none;" name="content" class="form-control" rows="20" cols="80"><c:out value="${pageInfo.content}"/></textarea>
                </div><!--well-->
              </div><!--col-->
              <div class="col-sm-2">
                <div id="sidebar" class="panel">
                  ${renderedSideBar}
                </div>
              </div><!--col-->
            </div><!--row-->
          </div>
          <div class="tab-pane" id="attributes">
            <div class="row">
              <div class="col-sm-9">
                <div class="well">
                  <textarea style="resize: none;" name="attributes" class="form-control" rows="20" cols="80"><c:forEach var="entry" items="${pageInfo.attributes}">"${entry.key}" = "${entry.value}"&#10;</c:forEach></textarea>
                </div><!--well-->
              </div><!--col-->
              <div class="col-sm-2">
                <div id="sidebar" class="panel">
                  ${renderedSideBar}
                </div>
              </div><!--col-->
            </div><!--row-->
          </div>
        </div><!--tabcontent-->
      </div><!--form-group-->

      <div class="form-group row">
        <div class="col-sm-3 col-md-4">
          <c:choose>
            <c:when test="${not empty param.description}">
              <c:set var="descriptionVal" value="${param.description}"/>
            </c:when>
            <c:otherwise>
              <c:set var="descriptionVal" value=""/>
            </c:otherwise>
          </c:choose>

          <label class="sr-only" for="description">Describe your change</label>
          <input id="description" name="description" type="text" class="form-control" placeholder="Describe your change" value="${descriptionVal}"/>
        </div>
        <div class="col-sm-2 col-md-2">
          <div class="checkbox">
            <label>
              <input name="minorEdit" type="checkbox" <c:if test="${not empty param.minorEdit}">checked="checked"</c:if>/>
              Minor edit
            </label>
          </div>
        </div>
        <div class="col-sm-4 col-md-3">
          <div class="btn-group">
            <button class="btn btn-default" type="submit" name="save">Save</button>
            <button class="btn btn-default" type="submit" name="preview">Preview</button>
          </div>
          <button class="btn btn-default" type="submit" name="unlock">Cancel</button>
        </div>
      </div><!--form-group row-->

      <!--hiddendata-->
      <input type="hidden" name="attributes" value="<c:forEach var="entry" items="${pageInfo.attributes}">"${entry.key}" = "${entry.value}"&#10;</c:forEach>"/>
      <input type="hidden" name="baseRevision" value="<c:out value="${pageInfo.revision}"/>"/>
      <input type="hidden" name="lockToken" value="<c:out value="${pageInfo.lockToken}"/>"/>
      <input type="hidden" name="sessionId" value="<c:out value="${sessionId}"/>"/>
      <c:set var="attrs" value=""/>
      <c:forEach var="entry" items="${originalAttributes}">
        <c:set var="attrs" value="${attrs} ${entry.key} &#10;"/>
      </c:forEach>
      <input type="hidden" name="originalAttrs" value="${attrs}" />
    </form>
    <c:if test="${empty preview}">
      <script type='text/javascript'>
      $(document).ready(function() {
        $("#contentArea").focus();
      });
      </script>
    </c:if>
    <jsp:include page="cheatsheet.html"></jsp:include>
  </div>
  </tiles:putAttribute>
</tiles:insertTemplate>
