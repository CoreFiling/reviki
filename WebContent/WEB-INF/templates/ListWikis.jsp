<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Wiki List</tiles:putAttribute>
  <tiles:putAttribute name="heading">Wiki List</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <ul id="wikiList">
      <c:forEach var="descriptor" items="${descriptors}">
        <li>
          <a href="<c:out value="${descriptor.frontPageUrl}"/>"><c:out value="${descriptor.name}"/></a>
          <c:if test="${descriptor.default}">(also available at the <a href="<c:out value="${descriptor.defaultFrontPageUrl}"/>">default location</a>)</c:if>
        </li>
      </c:forEach>
    </ul>
    <c:if test="${empty configuration.wikiNames}">
    <p>
    There are no wikis configured yet.
    </p>
    </c:if>
    <c:if test="${configuration.editable}">
      <p>
      To configure a new wiki just go to the URL of one of its pages
      and fill in the configuration details.
      </p>
      <p>
      Enter a wiki name below to go to the FrontPage for that wiki. 
      </p>
      <form id="jump" name="jump" action="<c:url value="/jump"/>">
      <input type="text" name="name"/><input type="submit" name="go" value="Go"/>
      </form>
    </c:if>
  </tiles:putAttribute>
</tiles:insertTemplate>
