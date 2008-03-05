<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title"><c:out value="svnwiki - ${pageInfo.title} - ${pageInfo.revisionName}"/></tiles:putAttribute>
  <tiles:putAttribute name="content">
    <h1><c:out value="${pageInfo.title}"/></h1>
    <form name="editForm" action="" method="post">
      <textarea rows="25" cols="80" name="content"><c:out value="${pageInfo.content}"/></textarea>
      <input type="hidden" name="baseRevision" value="<c:out value="${pageInfo.revision}"/>"/>
      <input type="hidden" name="lockToken" value="<c:out value="${pageInfo.lockToken}"/>"/>
      <hr/>
      <label for="description">Describe your change</label><input style="width:19em;margin-left:0.2em;margin-right:0.2em;" id="description" name="description" type="text"/>
      <input style="width:5em;" name="action" type="submit" value="Save"/>
      <input style="width:5em;" name="action" type="submit" value="Cancel"/>
    </form>
    <h3>Syntax cheatsheet</h3>
    <table>
    <tr><td colspan="2">
    <pre>
= Main heading
== Sub-heading
=== Sub-sub heading
</pre></td></tr>
    <tr><td><pre>//italic//</pre></td><td><pre>**bold**</pre></td></tr>
    <tr><td><pre>--strikethrough--</pre></td><td><pre>---- (horizontal rule)</pre></td>
    <tr><td><pre>WikiWord c2:InterWiki</pre></td><td><pre>{attached:file.txt} {image:file.jpg}</pre></td></tr>
    <tr>
    <td><pre>
# Numbered list
# Entry 2
## Nesting
    </pre></td>
    <td><pre>
* Bulleted list
* Entry 2
** Nesting
    </pre></td>
    </tr></table>
    <script type='text/javascript'>document.editForm.content.focus();</script>
  </tiles:putAttribute>
</tiles:insertTemplate>
