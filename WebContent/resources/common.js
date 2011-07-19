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

function shortcutForButton(id, shortcutKey, confirmMsg) {
  var button = $("#" + id);
  if (button.length == 1) {
    $(document).bind('keydown', shortcutKey, function(evt) {
      return editPageKeyHandler(button, confirmMsg);
    });
    // disableInInput is not supported in this upgraded version of jquery.hotkeys library, 
    // but we can't use the previous one, as it doesn't work with the currently used version of jQuery
    // hence we have to bind the shortcuts to input text fields and textareas manually
    if(shortcutKey=="esc") {
      // the esc key has different function for search bar, don't assign it there 
      inputs = $("input[type=text][name!=query]");
    }
    else {
      inputs = $("input[type=text]");
    }
    inputs.bind('keydown', shortcutKey, function(evt) {
      return editPageKeyHandler(button, confirmMsg);
    });
    $("textarea").bind('keydown', shortcutKey, function(evt) {
      return editPageKeyHandler(button, confirmMsg);
    });
  }
}

function editPageKeyHandler(button, confirmMsg) {
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
}

reviki.setupShortcutKeys = function() {
  var editButtonForm = $("#editTop");
  if (editButtonForm.length == 1) {
    $(document).bind('keydown', 'e', function (evt) {
        editButtonForm.submit();
        return false;
      });
  }

  shortcutForButton("save", "ctrl+return");
  shortcutForButton("cancel", "esc", "Are you sure you wish to cancel?");

  var searchBar = $("#query");
  if (searchBar.length == 1) {
    $(document).bind('keydown', 's', function (evt) {
      searchBar.focus();
      return false;
    });
    searchBar.bind('keydown', 'esc', function (evt) {
      searchBar.blur();
      return false;
    });
  }
  
}

reviki.confirmLeave = false;

reviki.confirmLeaveFunc = function() {
  reviki.lockToken = $("input[name=lockToken]").val();
  if (reviki.confirmLeave) {
    return "Changes will be lost. The page will be unlocked.";
  }
}

reviki.leaving = function() {
  $.ajax({
    type: "POST",
    url: $("#editForm").action,
    data: "unlock=&lockToken=" + reviki.lockToken,
    success: function(msg){
      alert(msg);
    }
  });

}

reviki.addConfirm = function() {
  reviki.confirmLeave = true;  
}

reviki.removeConfirm = function() {
  reviki.confirmLeave = false;
}

reviki.setupLeaveConfirm = function() {
  if ($("#editForm").length == 1) {
    if ($("#preview-area").length == 1) {
      reviki.addConfirm();
    }
    else {
      $("#content").bind("textchange", reviki.addConfirm);
    }
    $("#editForm").bind("submit", reviki.removeConfirm);
    $("#cancel").bind("click", reviki.removeConfirm);
    $("#save").bind("click", reviki.removeConfirm);
    window.onunload = reviki.leaving;
    window.onbeforeunload = reviki.confirmLeaveFunc;
  }
}

$(document).ready(reviki.configureAutoSuggest);
$(document).ready(reviki.setupShortcutKeys);
$(document).ready(reviki.setupLeaveConfirm);