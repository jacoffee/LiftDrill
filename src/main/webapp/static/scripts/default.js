var PageAlert = function() {
	var $msg = $("#page_alert").find("[class$=alert]");

	// dynamical add a div in the top and then fade away
	$(body).append(
			$("<div class='msg_container'></div>").html($msg.text()).fadeIn(3000).fadeOut("slow")
	);
}

var blackOverLayPop = function(title, content, action) {



}

var innerMailPopup = function (title, content, actionObj) {
	var titleElem = function() {
		if (title == null || title == undefined) {
			return $();
		} else {
			return $("<div class='title'></div>");
		}
	}();
	titleElem.html(title);

	var $mailbox = $("<div class='model-dialog' />");
	$mailbox.append(titleElem).append(content);

	var $actionContainer = $("<div class='process' />");
	$.each(actionObj,  function(key, action){
		var btn = $("<button type='button' />").html(key).addClass("send").unbind("click").click(function(){
			action();
		});
		var alink = $("<a href='javascript:;' />").text("取消").addClass("cancel-send").unbind("click").click(function(){
			// parent 只会匹配单一层级 而 parents则会匹配多个层级
			$(this).parents(".model-dialog").hide();
		});
		$actionContainer.append(btn).append(alink);
	});
	$mailbox.append($actionContainer);
	$("body").append($mailbox);
}

/*  recommended jQuery Writing Style */

// If the method changes the context, an extra level of indentation must be used.
var elements = $(".send-mail").
				addClass(".active")
				.children()
					.html( "hello")
				.end()
				.appendTo( "body" );

//  Declarations that don't have an assignment must be listed together at the start of the declaration
var a, b, c,
	foo = true,
	bar = false;


function isEmpty(str) {
	return (!str || str.length === 0);   // === strictly equation str.length has to be number
}