reviki = {};
/**
 * POST is a sensible way to do non-idempotent operations
 * but form buttons look ugly so this will replace a form
 * with a JavaScript link.
 */
reviki.formAsJavaScriptLink = function(formId, linkText) {
  var script = "javascript:document.getElementById('" + formId + "').submit()";
  var form = document.getElementById(formId);
  if (form) {
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
}
reviki.configureAutoSuggest = function() {
  var queryInput = $("#query");
  if (queryInput) {
    queryInput.attr["autocomplete"] = "off";
    queryInput.suggest(reviki.BASE_URL + "FindPage?ctype=txt&limit=20&force", {
      param: "query",
      onSelect: function() {
        $("#searchForm").submit();
      }
    });
  }
}
$(document).ready(reviki.configureAutoSuggest);
