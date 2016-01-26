tsApp.filter('highlight', function($sce) {
  return function(text, phrase) {
    var htext = text;
    if (text && phrase) {
      hext = text.replace(new RegExp('(' + phrase + ')', 'gi'),
        '<span class="highlighted">$1</span>');
    }
    return $sce.trustAsHtml(jtext);
  };
});

tsApp.filter('highlightLabelFor', function($sce) {
  return function(text, phrase) {
    var htext = text;
    if (text && phrase) {
      htext = text.replace(new RegExp('(' + phrase + ')', 'gi'),
        '<span style="background-color:#e0ffe0;">$1</span>');
    }
    return $sce.trustAsHtml(htext);
  };
});

tsApp.filter('highlightLabel', function($sce) {
  return function(text, phrase) {
    var htext = text;
    if (text && phrase) {
      htext = text.replace(new RegExp('(' + phrase + ')', 'gi'),
        '<span style="background-color:#e0e0ff;">$1</span>');
    }
    return $sce.trustAsHtml(htext);
  };
});