<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Copy "<c:out value="${page.title}"/>"</tiles:putAttribute>
  <tiles:putAttribute name="heading">Copy <sw:wikiLink page="${page}"/></tiles:putAttribute>
  <tiles:putAttribute name="content">
    <p>The edit history from the old page will be viewable from the copy.</p>
    <form name="copyForm" method="post" action="<c:url value="${page.name}"/>">
      <label for="toPage">New page name </label><input id="toPage" name="toPage" type="text" value=""/>
      <input name="copy" type="submit" value="Copy"/>
    </form>
    <script type='text/javascript'>
      $(document).ready(function() {
        $("#toPage").focus();
      });
    </script>
  </tiles:putAttribute>
</tiles:insertTemplate>
