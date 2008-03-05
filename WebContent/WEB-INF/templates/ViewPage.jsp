<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><c:out value="${pageInfo.path} (${pageInfo.revisionName})"/></title>
</head>
<body>
  <div id="content">
  ${renderedContents}
  </div>
  <form action="" method="post">
    <input type="submit" value="Edit"/>
  </form> 
</body>
</html>