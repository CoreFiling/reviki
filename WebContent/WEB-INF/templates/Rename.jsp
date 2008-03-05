<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.hillsdon.net/ns/svnwiki/tags" prefix="sw" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Rename "<c:out value="${page.title}"/>"</tiles:putAttribute>
  <tiles:putAttribute name="heading">Rename "<c:out value="${page.title}"/>"</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <p>Edit history will be retained.  Links to the page will not be updated.</p>
    <form name="renameForm" method="post" action="">
      <label for="toPage">New page name </label><input name="toPage" type="text" value=""/>
      <input name="rename" type="submit" value="Rename"/>
    </form>
  </tiles:putAttribute>
</tiles:insertTemplate>
