<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.hillsdon.net/ns/svnwiki/tags" prefix="sw" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Wiki List</tiles:putAttribute>
  <tiles:putAttribute name="heading">Wiki List</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <ul id="wikiList">
      <c:forEach var="wikiName" items="${configuration.wikiNames}">
        <li>
          <a href="<c:url value="/pages/${wikiName}/FrontPage"/>"><c:out value="${wikiName}"/></a>
          <c:if test="${wikiName == configuration.defaultWiki}">(also available at the <a href="<c:url value="/pages/FrontPage"/>">default location</a>)</c:if>
        </li>
      </c:forEach>
    </ul>
  </tiles:putAttribute>
</tiles:insertTemplate>
