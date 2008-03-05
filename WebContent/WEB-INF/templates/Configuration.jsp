<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>Configuration</title>
</head>
<body>
  <h1>Settings</h1>
  <p>
    The wiki will store pages at the directory pointed to by the given SVN URL.
    The URL should not be the root of the repository unless you really want svnwiki
    to put its files there.
  </p>
  
  <form action="" method="post">
    <table>
      <tr>
        <td><label for="url">SVN URL </label></td>
        <td><input style="width:25em;" id="url" name="url" value="<c:out value="${configuration.url}"/>"/></td>
      </tr>
      <tr><td colspan="2"><c:out value="${error}"/></td></tr>
      <tr><td colspan="2"><input type="submit" value="Save"/></td></tr>
    </table>
  </form> 
</body>
</html>