<%-- /*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */ --%> 

<!DOCTYPE html>
<%@page import="org.pentaho.platform.api.engine.IPluginManager"%>
<%@page import="org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction"%>
<%@page import="org.pentaho.platform.engine.core.system.PentahoSessionHolder"%>
<%@page import="org.pentaho.platform.api.engine.IAuthorizationPolicy"%>
<%@page import="org.pentaho.platform.engine.core.system.PentahoSystem"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Locale"%>
<%@page import="javax.servlet.http.HttpServletRequest"%>
<%
  boolean canCreateContent = PentahoSystem.get(IAuthorizationPolicy.class, PentahoSessionHolder.getSession()).isAllowed(RepositoryCreateAction.NAME);
  List<String> pluginIds = PentahoSystem.get(IPluginManager.class, PentahoSessionHolder.getSession()).getRegisteredPlugins();
  Locale locale = request.getLocale();
%>
<html lang="en" class="bootstrap">
<head>
  <meta charset="utf-8">
  <title>Home Page</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="locale" content="<%=locale.toString()%>">

  <!-- Le styles -->
  <link href="css/home.css" rel="stylesheet">

  <!-- We need web context for requirejs and css -->
  <script type="text/javascript" src="webcontext.js?context=mantle&cssOnly=true"></script>
  <script language='JavaScript' type='text/javascript' src='http://admin.brightcove.com/js/BrightcoveExperiences.js'></script>

  <!-- Avoid 'console' errors in browsers that lack a console. -->
  <script type="text/javascript">
    if (!(window.console && console.log)) {
      (function() {
        var noop = function() {};
        var methods = ['assert', 'debug', 'error', 'info', 'log', 'trace', 'warn'];
        var length = methods.length;
        var console = window.console = {};
        while (length--) {
          console[methods[length]] = noop;
        }
      }());
    }
  </script>

  <!-- Require Home -->
  <script type="text/javascript">
    var Home = null;
    pen.require(["home/home", 
      "common-ui/util/ContextProvider"], function(pentahoHome, ContextProvider) {
      Home = pentahoHome;      

      // Define properties for loading context
      var contextConfig = [{
        path: "properties/config",
        post: function(context, loadedMap) {
          context.config = loadedMap;
        }
      }, {
        path: "properties/messages",
        post: function(context, loadedMap) {
          context.i18n = loadedMap;
        }
      }];

      // Define permissions
      ContextProvider.addProperty("canCreateContent", <%=canCreateContent%>);
      ContextProvider.addProperty("hasAnalyzerPlugin", <%=pluginIds.contains("analyzer")%>);
      ContextProvider.addProperty("hasIRPlugin", <%=pluginIds.contains("pentaho-interactive-reporting")%>);
      ContextProvider.addProperty("hasDashBoardsPlugin", <%=pluginIds.contains("dashboards")%>);
      ContextProvider.addProperty("hasDataAccess", false); // default

      // BISERVER-8631 - Manage datasources only available to roles/users with appropriate permissions
      var serviceUrl = Home.getUrlBase() + "plugin/data-access/api/permissions/hasDataAccess";
      Home.getContent(serviceUrl, function(result) {
        ContextProvider.addProperty("hasDataAccess", result);
        ContextProvider.get(Home.init, contextConfig); // initialize
      }, function(error) {
        console.log(error);
        ContextProvider.get(Home.init, contextConfig); // log error and initialize anyway
      });

    });
  </script>

</head>

<body data-spy="scroll" data-target=".sidebar">
<div class="container-fluid main-container">
  <div class="row-fluid">
    <div class="span3" id="buttonWrapper">
      <script type="text/x-handlebars-template">
        <div class="well sidebar">
          <button class="btn btn-large btn-block" onclick="window.top.mantle_setPerspective('browser.perspective')">
            {{i18n.browse}}
          </button>

          <!-- Only show create button if user is allowed -->

          {{#if canCreateContent}}
          <button id="btnCreateNew" class="btn btn-large btn-block popover-source" data-toggle="dropdown"
              data-toggle="popover" data-placement="right" data-html="true" data-id="my_hid" data-container="body" onclick="preCreatePopover();">
            {{i18n.create_new}}
          </button>
          {{/if}}

          {{#if hasDataAccess}}
          <button class="btn btn-large btn-block" onclick="window.parent.executeCommand('ManageDatasourcesCommand')">
            {{i18n.manage_datasources}}
          </button>
          {{/if}}

          <button class="btn btn-large btn-block" onclick="window.parent.executeCommand('OpenDocCommand')">
            {{i18n.documentation}}
          </button>
        </div>

        <div style="display:none" id="btnCreateNewContent"></div>
      </script>
    </div>
    <div class="span9" style="overflow:auto">

      <div class="row-fluid">

        <!-- Pre-load  getting started over graphic for smooth inital transition for mouse over css -->
        <div class="preload-getting-started-over-icon" style="display:none"></div>
        
<!--
        <script type="text/x-handlebars-template">
          <div id="getting-started" class="well getting-started widget-panel">
            <h3>{{i18n.getting_started_heading}}</h3>
            
            <ul class="nav nav-tabs" id="tab-group">
                <li><a href="#tab1">{{i18n.getting_started_tab1}}</a></li>
                <li><a href="#tab2">{{i18n.getting_started_tab2}}</a></li>
                <li><a href="#tab3">{{i18n.getting_started_tab3}}</a></li>
            </ul>
 
            <div class="tab-content">
              <div class="tab-pane" id="tab1"></div>
              <div class="tab-pane" id="tab2"></div>
              <div class="tab-pane" id="tab3"></div>
            </div>
          </div>
        </script>        
-->
      </div>

      <div class="row-fluid">

        <div class="span6">
          <script id="recentsTemplate" type="text/x-handlebars-template" delayCompile="true">
            <div id="recents" class="well widget-panel">
              <h3>
                {{i18n.recents}}
              </h3>
              <div id="recentsSpinner"></div>
              {{#if isEmpty}}
              <div class="empty-panel content-panel">
                <div class="centered">
                  <div class="empty-message">{{i18n.empty_recents_panel_message}}</div>
                  <button class="pentaho-button" onclick="window.top.mantle_setPerspective('browser.perspective');">{{i18n.browse}}</button>
                </div>
              </div>
              {{else}}
              <div id="recents-content-panel" class="content-panel">
                <ul class="nav nav-tabs nav-stacked">
                  {{#eachRecent recent}}
                  <li>
                    <a href="javascript:Home.openRepositoryFile('{{escapeQuotes fullPath}}', 'run')" title='{{title}}'>
                      <div class="row-fluid">
                        <div class="span10 ellipsis">
                          {{#if xanalyzer}}   <i class="pull-left content-icon file-xanalyzer"/>  {{/if}}
                          {{#if xdash}}       <i class="pull-left content-icon file-xdash"/>      {{/if}}
                          {{#if xcdf}}        <i class="pull-left content-icon file-xcdf"/>       {{/if}}
                          {{#if prpti}}       <i class="pull-left content-icon file-prpti"/>      {{/if}}
                          {{#if prpt}}        <i class="pull-left content-icon file-prpt"/>       {{/if}}
                          {{#if xaction}}     <i class="pull-left content-icon file-xaction"/>    {{/if}}
                          {{#if url}}         <i class="pull-left content-icon file-url"/>        {{/if}}
                          {{#if html}}        <i class="pull-left content-icon file-html"/>       {{/if}}
                          {{#if unknownType}} <i class="pull-left content-icon file-unknown"/>    {{/if}}
                          <span class="pad-left">{{title}}</span>
                        </div>
                        <div class="span2">
                          {{#unless isEmpty}}
                          {{#if isFavorite}}
                          <i title="{{../../../i18n.remove_favorite_tooltip}}" class="pull-right favorite-on" onclick="controller.unmarkRecentAsFavorite('{{escapeQuotes fullPath}}'); return false;"/>
                          {{else}}
                          <i title="{{../../../i18n.add_favorite_tooltip}}" class="pull-right favorite-off" onclick="controller.markRecentAsFavorite('{{escapeQuotes fullPath}}', '{{title}}'); return false;" />
                          {{/if}}
                          {{/unless}}
                        </div>
                      </div>
                    </a>
                  </li>
                  {{/eachRecent}}
                </ul>
              </div>
              {{/if}}
            </div>
          </script>

          <div id="recentsContianer"></div>
        </div>

        <div class="span6">
          <script id="favoritesTemplate" type="text/x-handlebars-template" delayCompile="true">
            <div id="favorites" class="well widget-panel">
              <h3>
                {{i18n.favorites}}
              </h3>
              <div id="favoritesSpinner"></div>
              {{#if isEmpty}}
              <div class="empty-panel content-panel">
                <div class="centered">
                  <div class="empty-message">{{i18n.empty_favorites_panel_message}}</div>
                  <button class="pentaho-button" onclick="window.top.mantle_setPerspective('browser.perspective')">{{i18n.browse}}</button>
                </div>
              </div>
              {{else}}
              <div id="favorites-content-panel" class="content-panel">
                <ul class="nav nav-tabs nav-stacked">
                  {{#eachFavorite favorites}}
                  <li>
                    <a href="javascript:Home.openRepositoryFile('{{escapeQuotes fullPath}}', 'run')" title='{{title}}'>
                      <div class="row-fluid">
                        <div class="span10 ellipsis">
                          {{#if xanalyzer}}   <i class="pull-left content-icon file-xanalyzer"/>  {{/if}}
                          {{#if xdash}}       <i class="pull-left content-icon file-xdash"/>      {{/if}}
                          {{#if xcdf}}        <i class="pull-left content-icon file-xcdf"/>       {{/if}}
                          {{#if prpti}}       <i class="pull-left content-icon file-prpti"/>      {{/if}}
                          {{#if prpt}}        <i class="pull-left content-icon file-prpt"/>       {{/if}}
                          {{#if xaction}}     <i class="pull-left content-icon file-xaction"/>    {{/if}}
                          {{#if url}}         <i class="pull-left content-icon file-url"/>        {{/if}}
                          {{#if html}}        <i class="pull-left content-icon file-html"/>       {{/if}}
                          {{#if unknownType}} <i class="pull-left content-icon file-unknown"/>    {{/if}}
                          <span class="pad-left">{{title}}</span>
                        </div>
                        <div class="span2">
                          {{#unless isEmpty}}
                          <i title="{{../../../i18n.remove_favorite_tooltip}}" class="pull-right favorite-on" onclick="controller.unmarkRecentAsFavorite('{{escapeQuotes fullPath}}'); return false;"/>
                          {{/unless}}
                        </div>
                      </div>
                    </a>
                  </li>
                  {{/eachFavorite}}
                </ul>
              </div>
              {{/if}}
            </div>
          </script>

          <div id="favoritesContianer"></div>

        </div>

      </div>
    </div>
  </div>
</div>

<script type="text/javascript">

  var popup_init = false;

  function preCreatePopover(){
    if(!popup_init){
      var tmp = $.fn.popover.Constructor.prototype.show;
      $.fn.popover.Constructor.prototype.show = function () {
        tmp.call(this);
        if (!$('.popover-title').html())
        $('.popover-title').hide();
      };
      popup_init = true;
    }
  }

</script>

</body>
</html>
