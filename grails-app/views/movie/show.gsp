  
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show Movie</title>
        
        <!-- For admin interface to work in other applications, includes must be here. Should likely
             be moved into a resources module so layoutResources picks it up. -->
        <vid:includes player="jwflv"/>
        <vid:includes player="flowplayer"/>
        <!-- Stylesheet for admin interface should likely be moved to a file. -->
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
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">Movie List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Movie</g:link></span>
        </div>
    <script type="text/javascript">
        jwplayer("container").setup({
            flashplayer: "${r.resource(plugin:'gvps',dir:'jw-flv',file:'player.swf')}"
        });
    </script>

    <div class="body">
            <h1>Show Movie</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Id:</td>
                            
                            <td valign="top" class="value">${movie.id}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Create Date:</td>
                            
                            <td valign="top" class="value">${movie.createDate}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Created By:</td>
                            
                            <td valign="top" class="value">${movie.createdBy}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Description:</td>
                            
                            <td valign="top" class="value">${movie.description}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">File Name:</td>
                            
                            <td valign="top" class="value">${movie.fileName}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Content Type:</td>
                            
                            <td valign="top" class="value">${movie.contentType}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Play Time:</td>
                            
                            <td valign="top" class="value"><vid:convertVideoPlaytime time="${movie.playTime}"/></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Size:</td>
                            
                            <td valign="top" class="value">
                                <g:formatNumber number="${movie.size.toFloat()/1024.0/1024.0}" type="number" maxFractionDigits="2"/> Mb
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Video:</td>
                            
                            <td valign="top" class="value">
        
                                <table>
                                    <tr>
                                        <td> Http streaming (jwflv): <br/>
                                        <vid:display movie='${movie}' player="jwflv" stream='true'/>
                                        </td>
                                        <td> Full file download (jwflv): <br/>
                                        <vid:display movie='${movie}' player="jwflv" stream='false'/>
                                        </td>
                                    </tr>
                                      <tr>
                                        <td> Http streaming (flowplayer): <br/>
                                        <vid:display movie='${movie}' player="flowplayer" stream='true'/>
                                        </td>
                                        <td> Full file download (flowplayer): <br/>
                                        <vid:display height="260" width="320" movie='${movie}' player="flowplayer" stream='false'/>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td>jwflw captions plugin</td>
                                        <td>
                                            <p id="container">loading jw-flv player with captions</p>

                                            <script type="text/javascript">
                                            jwplayer("container").setup({
                                            file: "${g.createLink(action: 'streamFlv', id: movie.id)}",
                                            flashplayer: "${r.resource(plugin:'gvps',dir:'jw-flv',file:'player.swf')}",
                                            height: 260,
                                            streamscript: "${g.createLink(action: 'streamFlv', id: movie.id)}",
                                            provider: "http",
                                            streambuffer: 900,
                                            image: "${g.createLink(action: 'thumb', id: movie.id)}",
                                            plugins: {
                                            "captions-2": {
                                            file: "${r.resource(plugin:'gvps',dir:'assets',file:'caption.srt')}"
                                            }
                                            },
                                            width:320
                                            });
                                        </script>

                                        </td>
                                    </tr>

                                </table>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Title:</td>
                            
                            <td valign="top" class="value">${movie.title}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Url:</td>
                            
                            <td valign="top" class="value">${movie.url}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form controller="movie">
                    <input type="hidden" name="id" value="${movie?.id}" />
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
