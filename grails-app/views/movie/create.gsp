  
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create Movie</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">Movie List</g:link></span>
        </div>
        <div class="body">
            <h1>Create Movie</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${movie}">
            <div class="errors">
                <g:renderErrors bean="${movie}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post"  enctype="multipart/form-data">
                <div class="dialog">
                    <table>
                        <tbody>

                            <tr class='prop'><td valign='top' class='name'><label for='createdBy'>Created By:</label></td><td valign='top' class='value ${hasErrors(bean:movie,field:'createdBy','errors')}'><input type="text" id='createdBy' name='createdBy' value="${movie?.createdBy?.encodeAsHTML()}"/></td></tr>
                        
                            <tr class='prop'><td valign='top' class='name'><label for='title'>Title:</label></td><td valign='top' class='value ${hasErrors(bean:movie,field:'title','errors')}'><input type="text" id='title' name='title' value="${movie?.title?.encodeAsHTML()}"/></td></tr>

                            <tr class='prop'><td valign='top' class='name'><label for='description'>Description:</label></td><td valign='top' class='value ${hasErrors(bean:movie,field:'description','errors')}'><input type="text" id='description' name='description' value="${movie?.description?.encodeAsHTML()}"/></td></tr>

                            <tr class='prop'><td valign='top' class='name'><label for='theFile'>The File:</label></td><td valign='top' class='value ${hasErrors(bean:movie,field:'theFile','errors')}'><input type='file' id='theFile' name='theFile' /></td></tr>
                        

                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><input class="save" type="submit" value="Create"></input></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
