<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title" value="Configuration"/>
  <tiles:putAttribute name="content">
    <p>
      The wiki will store pages at the directory pointed to by the given SVN URL.
      The URL should not be the root of the repository unless you really want svnwiki
      to put its files there.
    </p>
    
    <form action="" method="post">
      <table>
        <tr>
          <td><label for="url">SVN URL </label></td>
          <td><input style="width:25em;" id="url" name="url" value="<c:out value="${configuration.url}"/>"/></td>
        </tr>
        <tr><td colspan="2"><c:out value="${error}"/></td></tr>
        <tr><td colspan="2"><input type="submit" value="Save"/></td></tr>
      </table>
    </form> 
  </tiles:putAttribute>
</tiles:insertTemplate>
