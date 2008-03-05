<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>svnwiki - <tiles:insertAttribute name="title"/></title>
  <link rel="alternate" type="application/atom+xml" title="RecentChanges feed" href="<c:url value="/pages/RecentChanges/atom.xml"/>" />
  <link rel="search" href="<c:url value="/pages/${wikiName}/FindPage/opensearch.xml"/>" type="application/opensearchdescription+xml" title="Wiki Search" />
  <style type="text/css">
    h1.title {
      margin: 0;
      clear: both;
      display: block;
      border-bottom: 1px solid black;
      border-top: 1px solid black;
      font-size: 150%;
    }
    h1 {
      font-size: 150%;
    }
    h2 {
      font-size: 130%;
    }
    h3 {
      font-size: 120%;
    }
    body {
      font-size: 11pt;
      font-family: Verdana, Arial, sans-serif
    }
    table {
      border-collapse: collapse;
    }
    th {
      text-align: left;
    }
    th, td {
      padding-left: 0.4em;
      padding-right: 0.4em;
    }
    a.new-page {
      color: #888888;
    }
    li.menu {
      display: inline;
    }
    ul.menu {
      list-style: none;
      padding: 0;
      margin: 0;
    }
    #topbar {
      float: right;
      margin-bottom: 0.3em;
    }
    #wiki-rendering {
      width: 55em;
    }
    #flash {
      text-align: center;
      background-color: yellow;
    }
    hr {
      color: black;
      background-color: black;
      height: 1px;
      border: 0px;
      border-style: solid;
      clear: both;
    }
  </style>
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
    <ul class="menu">
      <tiles:insertAttribute name="menuItems" ignore="true"/>
      <li class="menu"><a href="<c:url value="/pages/${wikiName}/FrontPage"/>">FrontPage</a></li>
      <li class="menu"><a href="<c:url value="/pages/${wikiName}/RecentChanges"/>">RecentChanges</a></li>
      <li class="menu"><a href="<c:url value="/pages/${wikiName}/AllPages"/>">AllPages</a></li>
      <li class="menu">
        <form style="display: inline;" name="searchForm" style="margin-top:0.2em;" action="<c:url value="/pages/${wikiName}/FindPage"/>" method="get">
          <input name="query" type="text" value="<c:out value="${param.query}"/>"/>
          <input value="Go" type="submit"/>
        </form>
      </li>
    </ul>
  </div>
  <div id="content">
    <h1 class="title"><tiles:insertAttribute name="heading"/></h1>
    <div id="sidebar" style="float:right">
    ${sidebar}
    </div>
    <tiles:insertAttribute name="content"/>
  </div>
</body>
</html>