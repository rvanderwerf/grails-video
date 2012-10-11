<!doctype html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<title><g:layoutTitle default="GVPS(Grails Video Pseudo Streamer)"/></title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon">
		<link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
		<link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-retina.png')}">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}" type="text/css">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'mobile.css')}" type="text/css">
        <g:layoutHead/>
        <g:javascript library="jquery" plugin="jquery"/>
        %{--<g:javascript src="jquery/jquery-1.7.1.js"/>--}%

            <r:require module="jquery"/>
            %{--<r:require module="jquery-ui"/>--}%

        <r:layoutResources />
        <style type="text/css">
            #grailsLogo {
                background-color: #abbf78;
                height: 112px
            }
            body {
                line-height: normal;
                font-size: 16px;
                background: #ffffff;
                color: #333333;
                margin: 0 auto;
                max-width: 1024px;
                overflow-x: hidden; /* prevents box-shadow causing a horizontal scrollbar in firefox when viewport < 960px wide */
                   -moz-box-shadow: 0 0 0.3em #255b17;
                -webkit-box-shadow: 0 0 0.3em #255b17;
                        box-shadow: 0 0 0.3em #255b17;
            }
            .nav {
                margin-bottom: 0;
                margin-left: 0;
            }
            li {
                line-height: normal;
            }
            ul {
                margin: 0;
            }
            h1 {
                color: #48802C;
                font-weight: normal;
                font-size: 1.25em;
            }
            h1, h2, h3, h4, h5, h6 {
                line-height: 1.1;
            }
            a:link {
                text-decoration: underline;
            }
        </style>
    </head>
	<body>
    %{--<r:layoutResources />--}%
		<div id="grailsLogo" role="banner"><a href="http://www.reachforce.com"><img src="${resource(dir: 'images', file: 'logosm.png')}" alt="GVPS(Grails Video Pseudo Streamer)"/></a>
        GVPS(Grails Video Pseudo Streamer)</div>
    <div id="menu">
        <sec:ifLoggedIn>
            Logged in as <sec:username/> (<g:link controller='logout'>Logout</g:link>)
        </sec:ifLoggedIn>
        <sec:ifNotLoggedIn>
            <a href='#' id='loginLink'>Login</a>
        </sec:ifNotLoggedIn>

        <nav:render group="top"/>
        <nav:renderSubItems group="top"/>
    </div>

        <g:layoutBody/>
		<div class="footer" role="contentinfo"></div>
		<div id="spinner" class="spinner" style="text-align:center;display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>

		<g:javascript src="spinner.js"/>
        <r:layoutResources />
	</body>
</html>