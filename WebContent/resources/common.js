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


function parseTxtResults(txt, q, options) {
  
  var items = [];
  var tokens = txt.split(options.delimiter);
  
  // parse returned data for non-empty items and prepare them for the autocomplete
  for (var i = 0; i < tokens.length; i++) {
    var tokenValue = $.trim(tokens[i]);
    if (tokenValue) {
      colonIndex = tokenValue.indexOf(':');
      if (colonIndex > -1) {
        tokenCategory = tokenValue.substring(0, colonIndex + 1);
        tokenValue = tokenValue.substring(colonIndex + 1);
      }
      else {
        tokenCategory = "";
      }
      tokenLabel = tokenValue.replace(
          new RegExp(q, 'ig'), 
          function(q) { return '<span class="' + options.matchClass + '">' + q + '</span>' }
          );
      items[items.length] = {label:tokenLabel, value:tokenValue, category:tokenCategory};
    }
  }
  return items;
}

reviki.configureAutoSuggest = function() {
  var options = {};
  options.source = reviki.SEARCH_URL + "?ctype=txt&limit=20&force";
  options.param = 'query';
  options.delay = 100;
  options.selectClass = 'ui-autocomplete-over';
  options.matchClass = 'ui-autocomplete-match';
  options.minchars = 2;
  options.delimiter = '\n';
  
  var searchBox = $("#query"); 
  var searchForm = $("#searchForm");
  var submitted = false;
  var cancelSearch = false;
  var prevQuery = "";
  var cache = {};
  var lastXhr;
  
  searchForm.bind('submit', function(evt) {
    submitted = true;
  });
  
  $.widget( "custom.catcomplete", $.ui.autocomplete, {
    _renderMenu: function( ul, items ) {
      var self = this,
        currentCategory = "";
      $.each( items, function( index, item ) {
        if ( item.category != currentCategory ) {
          ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
          currentCategory = item.category;
        }
        self._renderItem( ul, item );
      });
    }
  });
  
  var suggestions = searchBox.catcomplete({
    minLength: options.minchars,
    delay: options.delay,
    select: function(event, ui) {
      searchForm.val(ui.item.value);
      searchForm.submit();
      return false;
    },
    html: true
  });
  
  searchBox.catcomplete("option", "source", function(request, response) {
    var term;
    term = request.term;
    if (term in cache) {
      response(cache[term]);
      return;
    }
    query = {};
    query[options.param] = term;
    lastXhr = $.ajax({
      url: options.source,
      data: query,
      success: function(txtData, status, xhr) {
        if(!cancelSearch) {
          data = parseTxtResults(txtData, term, options);
          cache[term] = data;
          if(xhr==lastXhr) {
            response(data);
            suggestions.removeClass("ui-autocomplete-loading");
          } 
          else {
            suggestions.data("catcomplete").pending--;
          } 
        }
        else {
          // don't response with any data if the search was cancelled, as doing so would refresh the suggestions
          cancelSearch = false;
          suggestions.removeClass("ui-autocomplete-loading");
          suggestions.data("catcomplete").pending--;
          suggestions.data("catcomplete").term = prevQuery;
        }
      },
      error: function(txt) {
        if(!submitted) {
          response([{label:'<span class="ui-autocomplete-searcherror">Error during the search</span>', value:""}]);
        }
      }
    });
  });
  // remember the query so that it doesn't get replaced by the currently selected value
  searchBox.bind("catcompletefocus", function(event, ui) {
    if(!cancelSearch) {
      cancelSearch = true;
      prevQuery = searchBox.val();
    }
    else if(prevQuery == searchBox.val()) {
      // resume searching if the searchBox didn't change (e.g. focus on item on mouse over)
      cancelSearch = false;
    }
  });
  // resume searching if the user returned to the search box
  searchBox.bind("textchange", function(event) {
    if(prevQuery == searchBox.val()) {
      cancelSearch = false;
    }
  });
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
reviki.unlockOnUnload = true;

reviki.confirmLeaveFunc = function() {
  reviki.lockToken = $("input[name=lockToken]").val();
  if (reviki.confirmLeave) {
    return "Changes will be lost. The page will be unlocked.";
  }
}

reviki.leaving = function() {
  if(reviki.unlockOnUnload) {
    $.ajax({
      type: "POST",
      url: $("#editForm").action,
      data: "unlock=&lockToken=" + reviki.lockToken,
      success: function(msg){
        alert(msg);
      }
    });
  }
}

reviki.addConfirm = function() {
  reviki.confirmLeave = true;  
}

reviki.removeConfirm = function() {
  reviki.confirmLeave = false;
}

reviki.removeUnlockOnUnload = function() {
  reviki.unlockOnUnload = false;
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
    $("#editForm").bind("submit", reviki.removeUnlockOnUnload);
    $("#cancel").bind("click", reviki.removeUnlockOnUnload);
    $("#save").bind("click", reviki.removeUnlockOnUnload);
    window.onunload = reviki.leaving;
    window.onbeforeunload = reviki.confirmLeaveFunc;
  }
}

$(document).ready(reviki.configureAutoSuggest);
$(document).ready(reviki.setupShortcutKeys);
$(document).ready(reviki.setupLeaveConfirm);