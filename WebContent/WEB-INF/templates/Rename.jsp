<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Rename "<c:out value="${page.title}"/>"</tiles:putAttribute>
  <tiles:putAttribute name="heading">Rename <sw:wikiPage page="${page}"/></tiles:putAttribute>
  <tiles:putAttribute name="content">
    <p>Edit history will be retained.  Links to the page will not be updated.</p>
    <form class="form-horizontal" name="renameForm" method="post" action="<c:url value="${sw:urlEncode(page.name)}"/>">
      <div class="form-group">
        <label for="toPage" class="col-sm-2 control-label">New page name</label>
        <div class="col-sm-3">
          <input class="form-control" id="toPage" name="toPage" type="text" value="<c:url value="${sw:urlEncode(page.name)}"/>"/>
        </div>
      </div>

      <div class="form-group">
        <div class="col-sm-offset-2 col-sm-10">
          <button class="btn btn-default" name="rename" type="submit" value="Rename">Rename</button>
        </div>
      </div>
    </form>
    <script type='text/javascript'>
      $(document).ready(function() {
        $("#toPage").focus();
      });
    </script>
  </tiles:putAttribute>
</tiles:insertTemplate>
