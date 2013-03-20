<%@ page import="com.cantina.lab.Movie" %>



<div class="fieldcontain ${hasErrors(bean: movieInstance, field: 'url', 'error')} ">
    <label for="url">
        <g:message code="movie.url.label" default="Url"/>

    </label>
    <g:textField name="url" value="${movieInstance?.url}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: movieInstance, field: 'title', 'error')} ">
    <label for="title">
        <g:message code="movie.title.label" default="Title"/>

    </label>
    <g:textField name="title" value="${movieInstance?.title}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: movieInstance, field: 'fileName', 'error')} ">
    <label for="fileName">
        <g:message code="movie.fileName.label" default="File Name"/>

    </label>
    <g:textField name="fileName" value="${movieInstance?.fileName}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: movieInstance, field: 'description', 'error')} ">
    <label for="description">
        <g:message code="movie.description.label" default="Description"/>

    </label>
    <g:textField name="description" value="${movieInstance?.description}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: movieInstance, field: 'contentType', 'error')} ">
    <label for="contentType">
        <g:message code="movie.contentType.label" default="Content Type"/>

    </label>
    <g:textField name="contentType" value="${movieInstance?.contentType}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: movieInstance, field: 'contentTypeMaster', 'error')} ">
    <label for="contentTypeMaster">
        <g:message code="movie.contentTypeMaster.label" default="Content Type Master"/>

    </label>
    <g:textField name="contentTypeMaster" value="${movieInstance?.contentTypeMaster}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: movieInstance, field: 'pathFlv', 'error')} ">
    <label for="pathFlv">
        <g:message code="movie.pathFlv.label" default="Path Flv"/>

    </label>
    <g:textField name="pathFlv" value="${movieInstance?.pathFlv}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: movieInstance, field: 'pathMaster', 'error')} ">
    <label for="pathMaster">
        <g:message code="movie.pathMaster.label" default="Path Master"/>

    </label>
    <g:textField name="pathMaster" value="${movieInstance?.pathMaster}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: movieInstance, field: 'pathThumb', 'error')} ">
    <label for="pathThumb">
        <g:message code="movie.pathThumb.label" default="Path Thumb"/>

    </label>
    <g:textField name="pathThumb" value="${movieInstance?.pathThumb}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: movieInstance, field: 'fileSize', 'error')} ">
    <label for="size">
        <g:message code="movie.size.label" default="Size"/>

    </label>
    <g:field type="number" name="size" value="${movieInstance.size}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: movieInstance, field: 'createDate', 'error')} ">
    <label for="createDate">
        <g:message code="movie.createDate.label" default="Create Date"/>

    </label>
    <g:datePicker name="createDate" precision="day" value="${movieInstance?.createDate}" default="none"
                  noSelection="['': '']"/>
</div>

<div class="fieldcontain ${hasErrors(bean: movieInstance, field: 'createdBy', 'error')} ">
    <label for="createdBy">
        <g:message code="movie.createdBy.label" default="Created By"/>

    </label>
    <g:textField name="createdBy" value="${movieInstance?.createdBy}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: movieInstance, field: 'playTime', 'error')} ">
    <label for="playTime">
        <g:message code="movie.playTime.label" default="Play Time"/>

    </label>
    <g:field type="number" name="playTime" value="${movieInstance.playTime}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: movieInstance, field: 'key', 'error')} ">
    <label for="key">
        <g:message code="movie.key.label" default="Key"/>

    </label>
    <g:textField name="key" value="${movieInstance?.key}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: movieInstance, field: 'status', 'error')} ">
    <label for="status">
        <g:message code="movie.status.label" default="Status"/>

    </label>
    <g:textField name="status" value="${movieInstance?.status}"/>
</div>

