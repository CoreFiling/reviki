<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="${page.title}"/> attachments</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <h1><c:out value="${page.title}"/> attachments</h1>
    <table>
      <c:forEach var="attachment" items="${attachments}">
        <tr><td><a href="<c:url value="${attachment}"/>"><c:out value="${attachment}"/></a></td></tr>
      </c:forEach>
    </table>
    <h3>Add a new attachment</h3>
    <form action="" method="post" enctype="multipart/form-data">
      <table>
        <tr><th></th><td><input id="file" type="file" name="file"/></td></tr>
        <tr><th><label for="attachmentName">Attachment name (optional)</label></th><td><input id="attachmentName" type="text" name="attachmentName"/></td></tr>
        <tr><td></td><td><input type="submit" value="Upload"/></td></tr>
      </table>      
    </form>
  </tiles:putAttribute>
</tiles:insertTemplate>
