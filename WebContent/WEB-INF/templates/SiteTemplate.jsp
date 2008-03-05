<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.hillsdon.net/ns/svnwiki/tags" prefix="sw" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>svnwiki - <tiles:insertAttribute name="title"/></title>
  <c:if test="${wikiIsValid != null and wikiIsValid}">
    <link rel="alternate" type="application/atom+xml" title="RecentChanges feed" href="<sw:wikiUrl page="RecentChanges"/>/atom.xml"" />
    <link rel="search" href="<sw:wikiUrl page="FindPage"/>/opensearch.xml" type="application/opensearchdescription+xml" title="Wiki Search" />
  </c:if>
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
    
    /* JHighlighter styles */
    code {
      color: rgb(0,0,0); font-family: monospace; font-size: 12px; white-space: nowrap;
    }
    .java_type {
      color: rgb(0,44,221);
    }
    .java_comment {
      color: rgb(147,147,147); background-color: rgb(247,247,247);
    }
    .java_operator {
      color: rgb(0,124,31);
    }
    .java_separator {
      color: rgb(0,33,255);
    }
    .java_plain {
      color: rgb(0,0,0);
    }
    .java_javadoc_comment {
      color: rgb(147,147,147); background-color: rgb(247,247,247); font-style: italic;
    }
    .java_keyword {
      color: rgb(0,0,0); font-weight: bold;
    }
    .java_literal {
      color: rgb(188,0,0);
    }
    .java_javadoc_tag {
      color: rgb(147,147,147); background-color: rgb(247,247,247); font-style: italic; font-weight: bold;
    }
    
    div#MarkupHelp {
      background-color: white; color: #333;
      font-family: Verdana, Arial, Helvetica, sans-serif; line-height: 1.3em;
    }
    div#MarkupHelp table {
      margin-bottom: 0; border-top: 3px solid #999; border-left: 3px solid #999;
      border-right: 3px solid #BBB; border-bottom: 3px solid #BBB
    }
    div#MarkupHelp td {
      font-size: 80%; padding: 0.2em; margin: 0; border: 1px solid #999; border-width: 1px 0 1px 0;
      vertical-align: top; white-space: nowrap;
    }
    div#MarkupHelp td.arrow {
      padding-right: 5px; padding: 0 0.75em; color: #999;
    }
    div#MarkupHelp h3 {
      font-size: 90%; font-weight: bold; margin: 0 0 5px 0; padding: 5px 0 0 0;
    }
    div#MarkupHelp p {
      font-size: 70%;
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
    <c:if test="${wikiIsValid != null and wikiIsValid}">
      <ul class="menu">
        <tiles:insertAttribute name="menuItems" ignore="true"/>
        <li class="menu"><sw:wikiLink page="FrontPage"/></li>
        <li class="menu"><sw:wikiLink page="RecentChanges"/></li>
        <li class="menu"><sw:wikiLink page="AllPages"/></li>
        <li class="menu">
          <form style="display: inline;" name="searchForm" style="margin-top:0.2em;" action="<c:url value="/pages/${wikiName}/FindPage"/>" method="get">
            <input name="query" type="text" value="<c:out value="${param.query}"/>"/>
            <input value="Go" type="submit"/>
          </form>
        </li>
      </ul>
    </c:if>
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