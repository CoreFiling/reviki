<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Search results</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <h1>Search results for '<c:out value="${param.query}"/>'</h1>
    <c:choose>
      <c:when test="${not empty results}">
        <ul>
          <c:forEach var="match" items="${results}">
            <li>
              <a href="<c:url value="/pages/${match.page}"/>"><c:out value="${match.page}"/></a>
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
      <a href="<c:url value="/pages/${suggestCreate}"/>">Create new page <c:out value="${suggestCreate}"/></a>
    </c:if>
  </tiles:putAttribute>
</tiles:insertTemplate>
