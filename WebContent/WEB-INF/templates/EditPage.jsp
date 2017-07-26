<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="*${pageInfo.title} - ${pageInfo.revisionName}"/></tiles:putAttribute>
  <tiles:putAttribute name="heading"><c:out value="${pageInfo.title}"/></tiles:putAttribute>
  <tiles:putAttribute name="content">
    <script type="text/javascript" src="<sw:resourceUrl path="sisyphus.min.js"/>"></script>
    <c:choose>
      <c:when test="${not empty pageInfo.attributes['syntax']}">
        <c:set var="syntax" value="${pageInfo.attributes['syntax']}"/>
      </c:when>
      <c:otherwise>
        <c:set var="syntax" value="${defaultSyntax}"/>
      </c:otherwise>
    </c:choose>

    <c:if test="${not empty preview or not empty markedUpDiff}">
      <div class="row">
        <div class="col-sm-10 col-sm-offset-1">
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
                  <div class="panel panel-default preview-content">
                    <div id="wiki-rendering">${preview}</div>
                  </div>
                </div>
              </div>
            </c:if>
            <c:if test="${not empty markedUpDiff}">
              <div id="diff-area" class="tab-pane">
                <div class="well">
                  <div class="panel panel-default preview-content">
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
   	  <div class="row form-horizontal">
        <div class="form-group">
          <label class="col-sm-offset-1 col-sm-1 control-label" for="syntax">Syntax:</label>
          <div class="col-sm-2">
            <select id="syntax" class="form-control" name="syntax">
              <option value="reviki" <c:if test="${syntax == 'reviki'}">selected </c:if>>Reviki</option>
      	    <option value="markdown" <c:if test="${syntax == 'markdown'}">selected </c:if>>Markdown</option>
            </select>
          </div><!--col-->
        </div><!--form-group-->
      </div><!--row-->

      <div class="form-group">
        <div class="row">
          <div class="col-sm-10 col-sm-offset-1">
            <ul class="nav nav-tabs">
              <li class="active"><a id="editFormContent-link" href="#content" data-toggle="tab">Content</a></li>
              <li><a id="editFormAttributes-link" href="#attributes" data-toggle="tab">Attributes</a></li>
            </ul>
          </div><!--col-->
        </div><!--row-->

        <div class="tab-content">
          <div class="tab-pane active" id="content">
            <div class="row">
              <div class="col-sm-10 col-sm-offset-1">
                <div class="well">
                  <textarea id="contentArea" name="content" class="form-control" rows="20" cols="80"><c:out value="${pageInfo.content}"/></textarea>
                </div><!--well-->
              </div><!--col-->
            </div><!--row-->
          </div>
          <div class="tab-pane" id="attributes">
            <div class="row">
              <div class="col-sm-10 col-sm-offset-1">
                <div class="well">
                  <textarea name="attributes" class="form-control" rows="20" cols="80"><c:forEach var="entry" items="${pageInfo.attributes}">"${entry.key}" = "${entry.value}"&#10;</c:forEach></textarea>
                </div><!--well-->
              </div><!--col-->
            </div><!--row-->
          </div>
        </div><!--tabcontent-->
      </div><!--form-group-->

      <div class="form-group">
        <div class="row">
          <div class="col-sm-4 col-sm-offset-1 col-md-4 col-md-offset-1">
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
          <div class="col-sm-5 col-md-5">
            <div class="btn-group">
              <button id="save" class="btn btn-default" type="submit" name="save">Save</button>
              <button id="preview" class="btn btn-default" type="submit" name="preview">Preview</button>
            </div>
            <div class="btn-group">
              <button id="restore" data-toggle="tooltip" data-placement="bottom" title="Restore last unsaved edit" class="btn btn-default hidden">Restore</button>
              <button id="cancel" class="btn btn-default" type="submit" name="unlock">Cancel</button>
            </div>
          </div>
        </div>
      </div>

      <!--hiddendata-->
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
    <div class="row">
      <div class="col-sm-10 col-sm-offset-1">
        <jsp:include page="cheatsheet.html"></jsp:include>
      </div><!--col-->
    </div><!--row-->
  </tiles:putAttribute>
</tiles:insertTemplate>
