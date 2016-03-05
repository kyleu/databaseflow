/* global requirejs:false */
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
});
