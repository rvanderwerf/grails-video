<%@ page import="com.cantina.lab.Movie" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Movie List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create">New Movie</g:link></span>
        </div>
        <div class="body">
            <h1>Movie List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" />
                        
                   	        <g:sortableColumn property="createDate" title="Create Date" />
                        
                   	        <g:sortableColumn property="createdBy" title="Created By" />
                        
                   	        <g:sortableColumn property="description" title="Description" />
                        
                   	        <g:sortableColumn property="fileName" title="File Name" />
                        
                   	        <g:sortableColumn property="contentType" title="Content Type" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${movieList}" status="i" var="movie">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${movie.id}">${movie.id?.encodeAsHTML()}</g:link></td>
                        
                            <td>${movie.createDate?.encodeAsHTML()}</td>
                        
                            <td>${movie.createdBy?.encodeAsHTML()}</td>
                        
                            <td>${movie.description?.encodeAsHTML()}</td>
                        
                            <td>${movie.fileName?.encodeAsHTML()}</td>
                        
                            <td>${movie.contentType?.encodeAsHTML()}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${Movie.count()}" />
            </div>
        </div>
    </body>
</html>
