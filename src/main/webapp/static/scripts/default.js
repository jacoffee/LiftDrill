var Helper = {
	isEmpty: function (str) {
		// === strictly equation str.length has to be number
		return !str || str.length === 0;
	},
	isNullable: function(value) {
		return value === null || value === undefined;
	},
	PageAlert: function() {
		var $msg = $("#page_alert").find("[class$=alert]");

		// dynamical add a div in the top and then fade away
		$(body).append(
		$("<div class='msg_container'></div>").html($msg.text()).fadeIn(3000).fadeOut("slow")
		);
	}
},
BasePopup = {
},
blackOverLayPop = $.extend(BasePopup,
	{
		show: function(title, content, action) {}
	}
)

var innerMailPopup = function (title, content, actionObj) {
	var titleElem = function() {
		if (Helper.isEmpty(title)) {
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

