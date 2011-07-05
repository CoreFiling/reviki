<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Search results</tiles:putAttribute>
  <tiles:putAttribute name="heading">Search results for '<c:out value="${param.query}"/>'</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <c:choose>  
      <c:when test="${not empty results}">
      	<c:set var="lastWiki" value="${null}"/>
        <c:forEach var="match" items="${results}">
          <c:if test="${lastWiki != match.wiki}">
            <c:if test="${lastWiki != null}"></ul></c:if>
        	<h2 class="results-wiki">
        	  <c:choose>
        	    <c:when test="${match.sameWiki}">
        	    This Wiki (${match.wiki})
        	    </c:when>
        	    <c:otherwise>
        	    ${match.wiki}
        	    </c:otherwise>
        	  </c:choose>
        	</h2>
          	<ul>
          </c:if>
          <li>
            <sw:wikiPage wiki="${match.wiki}" page="${match.page}"/>
            <p style="margin-top: 0px">
              ${match.htmlExtract}
            </p>
          </li>
          <c:set var="lastWiki" value="${match.wiki}"/>
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
