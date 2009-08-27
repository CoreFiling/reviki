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
  if (queryInput.length == 1) {
    queryInput.attr["autocomplete"] = "off";
    queryInput.suggest(reviki.SEARCH_URL + "?ctype=txt&limit=20&force", {
      param: "query",
      onSelect: function() {
        $("#searchForm").submit();
      }
    });
  }
}

function shortcutForButton(id, shortcut, confirmMsg) {
  var button = $("#" + id);
  if (button.length == 1) {
    jQuery(document).bind('keydown', shortcut, function (evt) {
      if (confirmMsg != undefined) {
        var answer = window.confirm(confirmMsg);
        if (answer) {
          button.click();
          return false;
        }
        else {
          return true;
        }
      }
      else {
        button.click();
        return false;
      }
      
    });
  }
}

reviki.setupShortcutKeys = function() {
  var editButtonForm = $("#editTop");
  if (editButtonForm.length == 1) {
    jQuery(document).bind('keydown', {combi:'e', disableInInput: true}, function (evt) {
        editButtonForm.submit();
        return false;
      });
  }

  shortcutForButton("save", "ctrl+return");
  shortcutForButton("cancel", "esc", "Are you sure you wish to cancel?");

  var searchBar = $("#query");
  if (searchBar.length == 1) {
    jQuery(document).bind('keydown', {combi:'s', disableInInput: true}, function (evt) {
      searchBar.focus();
      return false;
    });
    searchBar.bind('keydown', 'esc', function (evt) {
    	searchBar.blur();
      return false;
    });
  }
  
}
$(document).ready(reviki.configureAutoSuggest);
$(document).ready(reviki.setupShortcutKeys);
