<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.hillsdon.net/ns/svnwiki/tags" prefix="sw" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <c:set var="titlePrefix">
    <c:choose>
      <c:when test="${not empty wikiName}">${wikiName}</c:when>
      <c:otherwise>svnwiki</c:otherwise>
    </c:choose>
  </c:set>
  <title><c:out value="${titlePrefix}"/> - <tiles:insertAttribute name="title"/></title>
  <c:if test="${wikiIsValid != null and wikiIsValid}">
    <link rel="alternate" type="application/atom+xml" title="RecentChanges feed" href="<sw:wikiUrl page="RecentChanges"/>/atom.xml"" />
    <link rel="search" href="<sw:wikiUrl page="FindPage"/>/opensearch.xml" type="application/opensearchdescription+xml" title="Wiki Search" />
  </c:if>
  <link href="${cssUrl}" rel="stylesheet" media="screen" type="text/css" />
  <link href="<c:url value="/resources/jquery.suggest.css"/>" rel="stylesheet" media="screen" type="text/css" />
  <script type="text/javascript" src="<c:url value="/resources/jquery.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/resources/jquery.dimensions.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/resources/jquery.bgiframe.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/resources/jquery.suggest.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/resources/common.js"/>"></script>
  <script type="text/javascript">
    svnwiki.BASE_URL = "<sw:wikiUrl page=""/>"
  </script>
</head>
<body>
  <c:if test="${not empty flash}">
    <div id="flash">
      <p>
        <c:out value="${flash}"/>
      </p>
    </div>
  </c:if>
  <div id="topbar">
    <c:if test="${wikiIsValid != null and wikiIsValid}">
      <ul class="menu">
        <c:set var="menuItems"><tiles:getAsString name="menuItems" ignore="true"/></c:set>
        ${menuItems}
        <c:if test="${not empty menuItems}">
          <li class="menu menu-separator"></li>
        </c:if>
        <li class="menu"><sw:wikiLink page="FrontPage"/></li>
        <li class="menu"><sw:wikiLink page="RecentChanges"/></li>
        <li class="menu"><sw:wikiLink page="AllPages"/></li>
        <li class="menu">
          <form style="display: inline;" name="searchForm" style="margin-top:0.2em;" action="<c:url value="/pages/${wikiName}/FindPage"/>" method="get">
            <input id="query" name="query" type="text" value="<c:out value="${param.query}"/>"/>
            <input value="Go" type="submit"/>
          </form>
        </li>
      </ul>
    </c:if>
  </div>
  <div id="header">
  ${renderedHeader}
  </div>
  <div id="content">
    <h1 class="title"><tiles:insertAttribute name="heading"/></h1>
    <div id="sidebar" style="float:right">
    ${renderedSideBar}
    </div>
    <tiles:insertAttribute name="content"/>
  </div>
  <div id="footer">
  ${renderedFooter}
    <p id="build-details">Built from r$BuildRevision$.</p>
  </div>
</body>
</html>
