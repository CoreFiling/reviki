<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>svnwiki - <tiles:insertAttribute name="title"/></title>
  <style type="text/css">
    body {
      font-family: Verdana, Arial, sans-serif
    }
    table {
      border-collapse: collapse;
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
    #flash {
      text-align: center;
      background-color: yellow;
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
  </div>
  <div id="content">
    <tiles:insertAttribute name="content"/>
  </div>
</body>
</html>