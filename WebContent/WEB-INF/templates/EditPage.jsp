<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><c:out value="${pageInfo.path} (r${pageInfo.revision})"/></title>
</head>
<body>
  <form action="" method="post">
    <textarea rows="25" cols="80" name="content"><c:out value="${pageInfo.content}"/></textarea>
    <input type="hidden" name="baseRevision" value="<c:out value="${pageInfo.revision}"/>"/>
    <input type="submit" value="Save"/>
  </form> 
</body>
</html>