<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw" %>

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <c:set var="titlePrefix">
    <c:choose>
      <c:when test="${not empty wikiName}">${wikiName}</c:when>
      <c:otherwise>reviki</c:otherwise>
    </c:choose>
  </c:set>
  <%-- Prevent indexing of 'unusual' pages. --%>
  <% if (!request.getParameterMap().isEmpty()) { %>
  <meta name="robots" content="noindex, nofollow"/>
  <% } %>
  <title><c:out value="${titlePrefix}"/> - <tiles:insertAttribute name="title"/></title>
  <link rel="shortcut icon" href="<sw:iconUrl name="favicon.ico"/>" />
  <c:if test="${wikiIsValid != null and wikiIsValid}">
    <link rel="alternate" type="application/atom+xml" title="RecentChanges feed" href="<sw:wikiUrl page="RecentChanges" query="ctype=atom"/>" />
    <link rel="search" href="<sw:wikiUrl page="FindPage" extraPath="/opensearch.xml"/>" type="application/opensearchdescription+xml" title="Wiki Search" />
  </c:if>
  <link rel="stylesheet" href="<sw:resourceUrl path="bootstrap.css"/>" media="all" type="text/css" />
  <link rel="stylesheet" href="<c:url value="${cssUrl}"/>" media="all" type="text/css" />
  <link rel="stylesheet" href="<sw:resourceUrl path="themes/reviki-flat/reviki-flat.css"/>" media="screen" type="text/css" />
  <script type="text/javascript" src="<sw:resourceUrl path="jquery-1.11.0.min.js"/>"></script>
  <script type="text/javascript" src="<sw:resourceUrl path="jquery-ui.min.js"/>"></script>
  <script type="text/javascript" src="<sw:resourceUrl path="jquery.ui.autocomplete.html.js"/>"></script>
  <script type="text/javascript" src="<sw:resourceUrl path="jquery.hotkeys.js"/>"></script>
  <script type="text/javascript" src="<sw:resourceUrl path="jquery.textchange.js"/>"></script>
  <script type="text/javascript" src="<sw:resourceUrl path="common.js"/>"></script>
  <script type="text/javascript" src="<sw:resourceUrl path="bootstrap.min.js"/>"></script>
  <script type="text/javascript">
    reviki.SEARCH_URL = "<sw:wikiUrl page="FindPage"/>";
    $(function() {
    	$(".nojs").removeClass("nojs");
    });
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
  <c:if test="${wikiIsValid != null and wikiIsValid}">
    <nav class="navbar navbar-default navbar-fixed-top" role="navigation">
      <div class="container-fluid">
        <div class="row col-md-12">
          <div class="navbar-header">
            <div class="navbar-brand">
              <c:set var="brandTitle">
                <c:choose>
                  <c:when test="${not empty renderedHeader}">${renderedHeader}</c:when>
                  <c:otherwise>${renderedHeader}</c:otherwise>
                </c:choose>
              </c:set>
              ${brandTitle}
            </div>
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
              <span class="sr-only">Toggle navigation</span>
              <span class="icon-bar"></span>
              <span class="icon-bar"></span>
              <span class="icon-bar"></span>
            </button>
          </div>
          <div class="collapse navbar-collapse">
            <ul class="nav navbar-nav">
              <li><sw:wikiPage page="FrontPage"/></li>
              <li><sw:wikiPage page="RecentChanges"/></li>
              <li><sw:wikiPage page="AllPages"/></li>
            </ul>
            <form id="searchForm" name="searchForm" action="<sw:wikiUrl page="FindPage"/>" method="get" class="navbar-form navbar-right" role="search">
              <div class="form-group">
                <input id="query" class="form-control input-sm" name="query" type="text" value="<c:out value="${param.query}"/>"/>
                <input class="btn btn-default btn-sm" value="Go" type="submit"/>
              </div>
            </form>
          </div><!--navbar-collapse-->
        </div>
      </div><!--container-->
    </nav><!--nav-->
  </c:if>
  <div class="container-fluid">
    <div class="row">
      <div class="col-xs-12">
        <div id="content-area" class="panel panel-default">
          <div class="panel-heading">
            <div class="row">
              <div class="col-xs-6">
                <h1 class="title"><tiles:insertAttribute name="heading"/></h1>
              </div>
              <div class="col-xs-6">
                <div class="pull-right">
                  <ul>
                    <c:set var="menuItems"><tiles:getAsString name="menuItems" ignore="true"/></c:set>
                    <c:out value="${menuItems}" escapeXml="false"/>
                  </ul>
                </div>
              </div>
            </div>
          </div>
          <div class="panel-body">
            <tiles:insertAttribute name="content"/>
          </div>
          <div class="panel-footer">
            <div id="footer" class="auxillary">
              ${renderedFooter}
              <p id="build-details">Version $Version$.</p>
            </div>
            <tiles:insertAttribute name="content-controls" ignore="true"/>
          </div>
        </div><!--panel-->
      </div><!--column-->
    </div><!--row-->
  </div><!--container-->
  <tiles:insertAttribute name="body-level" ignore="true" />
</body>
</html>
