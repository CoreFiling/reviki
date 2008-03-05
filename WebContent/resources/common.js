svnwiki = {};
/**
 * POST the sensible way to do non-idempotent operations
 * but form buttons look ugly so this will replace a form
 * with a JavaScript link.
 */
svnwiki.formAsJavaScriptLink = function(formId, linkText) {
  var script = "javascript:document.getElementById('" + formId + "').submit()";
  var form = document.getElementById(formId);
  form.style.display = "none";
  var a = document.createElement("a");
  a.setAttribute("href", script);
  a.setAttribute("id", formId + "SubmitLink");
  a.setAttribute("name", formId + "SubmitLink");
  a.appendChild(document.createTextNode(linkText));
  var parent = form.parentNode;
  var space = parent.insertBefore(document.createTextNode(" "), form)
  parent.insertBefore(a, space);
  
}
