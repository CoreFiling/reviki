<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><c:out value="${pageInfo.path} (r${pageInfo.revision})"/></title>
</head>
<body>
  <div id="content">
    <pre><c:out value="${pageInfo.content}"/></pre>
  </div>
  <form action="" method="post">
    <input type="submit" value="Edit"/>
  </form> 
</body>
</html>