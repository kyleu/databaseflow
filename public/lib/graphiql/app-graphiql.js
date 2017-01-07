function initFileOptions() {
  var optionsShown = false;
  $(".toggle-button").on('click', function() {
    if(optionsShown) {
      $("#graphiql").removeClass('padded');
      $("#file-options").hide();
    } else {
      $("#graphiql").addClass('padded');
      $("#file-options").show();
    }
    optionsShown = !optionsShown;
  });
}

$(function(global) {
  var search = window.location.search;
  var parameters = {};
  search.substr(1).split('&').forEach(function(entry) {
    var eq = entry.indexOf('=');
    if(eq >= 0) {
      parameters[decodeURIComponent(entry.slice(0, eq))] =
        decodeURIComponent(entry.slice(eq + 1).replace(/\+/g, '%20'));
    }
  });

  if(parameters.variables) {
    try {
      parameters.variables = JSON.stringify(JSON.parse(query.variables), null, 2);
    } catch(e) {
      console.log('Cannot parse parameters.', e);
    }
  }

  function onEditQuery(newQuery) {
    parameters.query = newQuery;

    $('.save-body-input').val(newQuery);
  }

  function onEditVariables(newVariables) {
    parameters.variables = newVariables;
  }

  function graphQLFetcher(graphQLParams) {
    return fetch(window.location.origin + '/graphql', {
      method: 'post',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(graphQLParams),
      credentials: 'include'
    }).then(function(response) {
      return response.text();
    }).then(function(responseBody) {
      try {
        return JSON.parse(responseBody);
      } catch(error) {
        console.log(error);
        return responseBody;
      }
    });
  }

  $('html > head').append($('<style>.variable-editor {display: none !important}</style>'))

  global.renderGraphiql = function(elem) {
    $('.save-body-input').val(parameters.query);
    $('.save-dir-input').val(parameters.dir);
    $('.save-name-input').val(parameters.name);

    var gqlChildren = [
      React.createElement(GraphiQL.Logo, {}, [
        React.createElement("div", { "className": "toggle-button" }, "☰"),
        React.createElement("a", { "href": "/", "className": "title-link" }, "Database Flow GraphQL")
      ])
    ];

    var gqlProps = {
      fetcher: graphQLFetcher,
      query: parameters.query,
      variables: parameters.variables,
      response: parameters.response,
      onEditQuery: onEditQuery,
      onEditVariables: onEditVariables,
      defaultQuery: "query FirstQuery {\n  \n  }\n}"
    };

    var gql = React.createElement(GraphiQL, gqlProps, gqlChildren);

    React.render(gql, elem);

    initFileOptions();
  }
}(window));
