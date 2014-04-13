var PageAlert = function() {
	var $msg = $("#page_alert").find("[class$=alert]");

	// dynamical add a div in the top and then fade away
	$(body).append(
			$("<div class='msg_container'></div").html($msg.text()).fadeIn(3000).fadeOut("slow")
	);
}

var blackOverLayPop = function(title, content, action) {




}