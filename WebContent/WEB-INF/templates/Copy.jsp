<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Copy "<c:out value="${page.title}"/>"</tiles:putAttribute>
  <tiles:putAttribute name="heading">Copy <sw:wikiPage page="${page}"/></tiles:putAttribute>
  <tiles:putAttribute name="content">
    <p>The edit history from the old page will be viewable from the copy.</p>
    <form class="form-horizontal" name="copyForm" method="post" action="<c:url value="${sw:urlEncode(page.name)}"/>">
      <div class="form-group">
        <label for="toPage" class="col-sm-2 control-label">New page name </label>
        <div class="col-sm-5">
          <input id="toPage" class="form-control" name="toPage" type="text" value=""/>
        </div>
      </div>
      <div class="form-group">
        <div class="col-sm-offset-2 col-sm-10">
          <button class="btn btn-default" name="copy" type="submit">Copy</button>
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
