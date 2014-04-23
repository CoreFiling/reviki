<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Error</tiles:putAttribute>
  <tiles:putAttribute name="heading">An error has occurred</tiles:putAttribute>
  <tiles:putAttribute name="content">
  <c:if test="${not empty customMessage}">
  <p>
    <c:out value="${customMessage}"/>
  </p>
  </c:if>
  <p>
    <c:out value="${exception.message}"/>
  </p>
  <p><a id="goBack" href="#">Go back</a></p>
  <script type="text/javascript">
    // XHTML5 validator fails when inline JS is used.
    $(document).ready(function() {
        $('#goBack').click(function(evt) {
          evt.preventDefault();
          history.go(-1);
          });
        });
  </script>
    
  </tiles:putAttribute>
</tiles:insertTemplate>
