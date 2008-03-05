<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>svnwiki - <tiles:insertAttribute name="title"/></title>
  <style type="text/css">
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
    ul.menu {
      list-style: none;
      padding: 0;
      margin: 0;
    }
    #sidebar {
      float: right;
    }
    #wiki-rendering {
      width: 55em;
    }
    #flash {
      text-align: center;
      background-color: yellow;
    }
    h3.heading-1 {
      font-weight: bold;
      font-size: 150%;
    }
    h3.heading-1-1 {
      font-weight: bold;
      font-size: 120%;
    }
    h3.heading-1-1-1 {
      font-weight: bold;
      font-size: 100%;
    }
    h3.heading-1-1-1-1 {
      font-size: 100%;
    }
    h3.heading-1-1-1-1-1 {
      font-size: 80%;
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
  <div id="sidebar">
    <ul class="menu">
      <li class="menu"><a href="<c:url value="/pages/RecentChanges"/>">Recent changes</a></li>
      <li class="menu"><a href="<c:url value="/pages/AllPages"/>">All pages</a></li>
    </ul>
    <form name="searchForm" style="margin-top:0.2em;" action="<c:url value="/search"/>" method="get">
      <input name="query" type="text" value="<c:out value="${param.query}"/>"/>
      <input value="Go" type="submit"/>
    </form>
  </div>
  <div id="content">
    <tiles:insertAttribute name="content"/>
  </div>
</body>
</html>