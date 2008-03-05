<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><tiles:insertAttribute name="title"/></title>
  <link href="/style.css" rel="stylesheet" type="text/css"/>
</head>
<body>
  <div id="content">
    <h1><tiles:insertAttribute name="title"/></h1>
    <tiles:insertAttribute name="content"/>
  </div>
</body>
</html>