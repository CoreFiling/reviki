<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.hillsdon.net/ns/svnwiki/tags" prefix="sw" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Copy "<c:out value="${page.title}"/>"</tiles:putAttribute>
  <tiles:putAttribute name="heading">Copy <sw:wikiLink page="${page.path}"/></tiles:putAttribute>
  <tiles:putAttribute name="content">
    <p>The edit history from the old page will be viewable from the copy.</p>
    <form name="copyForm" method="post" action="">
      <label for="toPage">New page name </label><input name="toPage" type="text" value=""/>
      <input name="copy" type="submit" value="Copy"/>
    </form>
    <script type='text/javascript'>document.copyForm.toPage.focus();</script>
  </tiles:putAttribute>
</tiles:insertTemplate>
