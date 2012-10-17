  
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show Movie</title>
        <!-- usually you use one or the other here -->

      %{--<script type="text/javascript" src="/site/js/flowplayer/flowplayer-3.2.4.min.js"></script>
      <script type="text/javascript" src="/site/js/flowplayer/flowplayer.ipad-3.2.1.js"></script>--}%

    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">Movie List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Movie</g:link></span>
        </div>
    <script type="text/javascript">
        jwplayer("container").setup({
            flashplayer: "${g.createLink(uri:'/jw-flv/player.swf')}"
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
                                            file: "${g.createLink(action: 'streamflv', id: movie.id)}",
                                            flashplayer: "${g.createLink(uri:'/jw-flv/player.swf')}",
                                            height: 260,
                                            streamscript: "${g.createLink(action: 'streamflv', id: movie.id)}",
                                            provider: "http",
                                            streambuffer: 900,
                                            image: "${g.createLink(action: 'thumb', id: movie.id)}",
                                            plugins: {
                                            "captions-2": {
                                            file: "${g.createLink(uri:'/assets/caption.srt')}"
                                            }
                                            },
                                            width:320
                                            });
                                        </script>
                                           %{-- <script type="text/javascript" src="${g.createLink(uri:'/jw-flv/jwplayer.js')}"></script>
                                            <video class="container" src="${g.createLink(action: 'streamflv', id: movie.id)}"
                                                   poster="${g.createLink(action:'thumb',id:movie.id)}"></video>--}%

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
