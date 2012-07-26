  
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit Movie</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">Movie List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Movie</g:link></span>
        </div>
        <div class="body">
            <h1>Edit Movie</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${movie}">
            <div class="errors">
                <g:renderErrors bean="${movie}" as="list" />
            </div>
            </g:hasErrors>
            <g:form controller="movie" method="post"  enctype="multipart/form-data">
                <input type="hidden" name="id" value="${movie?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
				            <tr class='prop'><td valign='top' class='name'><label for='createDate'>Create Date:</label></td><td valign='top' class='value ${hasErrors(bean:movie,field:'createDate','errors')}'><g:datePicker name='createDate' value="${movie?.createDate}" ></g:datePicker></td></tr>
                        
				            <tr class='prop'><td valign='top' class='name'><label for='createdBy'>Created By:</label></td><td valign='top' class='value ${hasErrors(bean:movie,field:'createdBy','errors')}'><input type="text" id='createdBy' name='createdBy' value="${movie?.createdBy?.encodeAsHTML()}"/></td></tr>
                        
				            <tr class='prop'><td valign='top' class='name'><label for='description'>Description:</label></td><td valign='top' class='value ${hasErrors(bean:movie,field:'description','errors')}'><input type="text" id='description' name='description' value="${movie?.description?.encodeAsHTML()}"/></td></tr>
                        
				            <tr class='prop'><td valign='top' class='name'><label for='fileName'>File Name:</label></td><td valign='top' class='value ${hasErrors(bean:movie,field:'fileName','errors')}'><input type="text" id='fileName' name='fileName' value="${movie?.fileName?.encodeAsHTML()}"/></td></tr>

				            <tr class='prop'><td valign='top' class='name'><label for='playTime'>Play Time:</label></td><td valign='top' class='value ${hasErrors(bean:movie,field:'playTime','errors')}'><input type='text' id='playTime' name='playTime' value="${movie?.playTime}" /></td></tr>

				            <tr class='prop'><td valign='top' class='name'><label for='theFile'>The File:</label></td><td valign='top' class='value ${hasErrors(bean:movie,field:'theFile','errors')}'><input type='file' id='theFile' name='theFile' /></td></tr>
                        
				            <tr class='prop'><td valign='top' class='name'><label for='title'>Title:</label></td><td valign='top' class='value ${hasErrors(bean:movie,field:'title','errors')}'><input type="text" id='title' name='title' value="${movie?.title?.encodeAsHTML()}"/></td></tr>
                        
				            <tr class='prop'><td valign='top' class='name'><label for='url'>Url:</label></td><td valign='top' class='value ${hasErrors(bean:movie,field:'url','errors')}'><input type="text" id='url' name='url' value="${movie?.url?.encodeAsHTML()}"/></td></tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
