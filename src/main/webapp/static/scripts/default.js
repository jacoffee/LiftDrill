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
	classNames: ['model-dialog'],

	titleClassNames: ["title"],

	contentClassNames: ["content-wrapper"],

	zIndex: 99999,

	setPosition: function() {

	},
	renderTitleElement: function(titleHtml) {
		this.getElement().find("." +this.titleClassNames[0]).html(titleHtml)
		this
	},
	renderContentElement: function(contentHtml) {
		this.getElement().find("." +this.contentClassNames[0]).html(contentHtml)
		this
	},
	getElement: function() {
		// if the current object has no element attr
		var self = this;
		if (Helper.isNullable(this.element)) {
			// dynamic add attribute for JavaScript
			// just add empty the rest will be filled with othe method
			this.element = $("<div></div>").addClass(this.classNames.join(" ")).css(
				{
					"z-index": this.zIndex
				}
			).append(
				$("<div></div>").addClass(this.titleClassNames.join(" "))
			).append(
				$("<div></div>").addClass(this.contentClassNames.join(" "))
			)
		}
		this.element
	},
	show: function(){
		this.getElement().appendTo($("body")).show();
	}
},
BlackOverlayPop = $.extend(
	BasePopup,
	{
		overlayClassName: ['black-overlay'],
		renderblackOverLay: function() {
			$("<div></div>").addClass(this.overlayClassName.join(" "))
		},
		show: function() {
			this.getElement().before(this.renderblackOverLay()).appendTo($("body")).show();
		}
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

