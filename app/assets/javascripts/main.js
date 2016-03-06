/* global requirejs:false */
/* global ace:false */
/* global DatabaseFlow:false */
/* global $:false */
requirejs.config({
  baseUrl: '/assets/javascripts',
  paths: {
    lib: '/assets/lib'
  }
});

requirejs([], function() {
  'use strict';

  $('#nav-menu-button').sideNav();

  if(DatabaseFlow !== undefined) {
    window.dbf = new DatabaseFlow();
  }

  if(ace !== undefined) {
    ace.require('ace/ext/language_tools');
    var editor = ace.edit('sql-textarea');
    //editor.setTheme('ace/theme/monokai');
    editor.setShowPrintMargin(false);
    editor.setHighlightActiveLine(false);
    editor.setAutoScrollEditorIntoView(true);
    editor.getSession().setMode('ace/mode/sql');
    editor.getSession().setTabSize(2);
    editor.setOptions({
      enableBasicAutocompletion: true,
      enableLiveAutocompletion: true,
      minLines: 4,
      maxLines: 1000
    });
  }
});
