<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Search results</tiles:putAttribute>
  <tiles:putAttribute name="heading">Search results for '<c:out value="${param.query}"/>'</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <c:choose>
      <c:when test="${not empty results}">
        <ul>
          <c:forEach var="match" items="${results}">
            <li>
              <sw:wikiPage page="${match.page}"/>
              <p style="margin-top: 0px">
                ${match.htmlExtract}
              </p>
            </li>
          </c:forEach>
        </ul>
      </c:when>
      <c:otherwise>
        <p>No results found.</p>
      </c:otherwise>
    </c:choose>
    <c:if test="${not empty suggestCreate}">
      <a name="create" href="<sw:wikiUrl page="${suggestCreate}"/>">Create new page <c:out value="${suggestCreate}"/></a>
    </c:if>
  </tiles:putAttribute>
</tiles:insertTemplate>
