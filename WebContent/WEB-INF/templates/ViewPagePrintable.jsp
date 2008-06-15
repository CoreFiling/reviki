<%-- A much simpler template for printing. --%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><c:out value="${pageInfo.title} - ${pageInfo.revisionName}"/></title>
  <meta name="robots" content="noindex, nofollow" />
  <link rel="shortcut icon" href="<sw:resourceUrl path="favicon.ico"/>" />
  <link rel="stylesheet" href="${cssUrl}" media="screen" type="text/css" />
</head>
<body>
  <h1><c:out value="${pageInfo.title}"/></h1>
  <div id="wiki-rendering">
  ${renderedContents}
  </div>
</body>
</html>
