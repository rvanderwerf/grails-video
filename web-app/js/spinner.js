 function doSomething(currentURL, actionURL,entityName){
	 var current = currentURL.substring(currentURL.toLowerCase().indexOf(entityName.toLowerCase())+entityName.length+1, currentURL.length);
	 var action = actionURL.substring(actionURL.toLowerCase().indexOf(entityName.toLowerCase())+entityName.length+1, actionURL.length);

	 var act=action.split("/");
     var cur=current.split("/");

     //More changes have to be made depending on the type of views
     if(act[0] != cur[0]){
	 document.getElementById("spinner").style.display = "";
	 }
 } 