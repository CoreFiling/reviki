<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Config Svn Location</tiles:putAttribute>
  <tiles:putAttribute name="heading">Config Svn Location</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <p>
      The wiki will store pages at the directory pointed to by the given SVN URL.
      The URL should not be the root of the repository unless you really want to
      store the data there.
    </p>
    <p>
      If the location contains existing wiki data it may take some minutes for
      indexing to complete after you submit this form.
    </p>
    
    <form name="configurationForm" action="" method="post">
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
