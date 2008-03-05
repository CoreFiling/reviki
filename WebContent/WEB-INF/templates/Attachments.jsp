<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Attachments for <c:out value="${page.title}"/></tiles:putAttribute>
  <tiles:putAttribute name="content">
    <h1>Attachments for <c:out value="${page.title}"/></h1>
    <h2>Add a new attachment</h2>
    <form action="" method="post" enctype="multipart/form-data">
      <table>
        <tr><th></th><td><input id="file" type="file" name="file"/></td></tr>
        <tr><th><label for="attachmentName">Attachment name (optional)</label></th><td><input id="attachmentName" type="text" name="attachmentName"/></td></tr>
        <tr><td></td><td><input type="submit" value="Upload"/></td></tr>
      </table>      
    </form>
  </tiles:putAttribute>
</tiles:insertTemplate>
