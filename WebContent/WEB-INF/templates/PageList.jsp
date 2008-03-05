<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>Page list</title>
</head>
<body>
  <ul>
    <c:forEach var="path" items="${pageList}">
      <li><a href="<c:url value="${path}"/>"><c:out value="${path}"/></a></li>
    </c:forEach>
  </ul>
</body>
</html>