PageAlert = function() {
	var $msg = $("#page_alert").find("[class$=alert]");
	// dynamical add a div in the top and then fade away
	$("body").append(
		$("<div class='msg_container'></div>").html($msg.text()).fadeIn(3000).fadeOut("slow")
	);
}

var Helper = {
	isEmpty: function (str) {
		// === strictly equation str.length has to be number
		return !str || str.length === 0;
	},
	isNullable: function(value) {
		return value === null || value === undefined;
	},
},
BasePopup = {
	classNames: ['model-dialog'],

	titleClassNames: ["title"],

	contentClassNames: ["content-wrapper"],

	zIndex: 99999,

	setPosition: function() {
		var top = ($(window).height - this.getElement().height()) / 3;

		if (top < 0 ) {
			top = 50
		}
		return this.getElement().css(
			{
				/* 第二次出现的时候  由于CSS添加了 宽度所以起作用了 to be done */
				left: ($(window).width() - this.getElement().width()) / 2 + "px",
				top: $(document).scrollTop() + top + "px"
			}
		);
	},
	renderTitleElement: function(titleHtml) {
		this.getElement().find("." +this.titleClassNames[0]).html(titleHtml);
		return this;
	},
	renderContentElement: function(contentHtml) {
		this.getElement().find("." +this.contentClassNames[0]).html(contentHtml);
		return this;
	},
	getElement: function() {
		// if the current object has no element attr
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
			);
		}
		return this.element;
	},
	show: function(){
		this.setPosition();
		this.getElement().appendTo($("body")).show();
	}
},
BlackOverlayPop = $.extend(
	BasePopup,
	{
		overlayClassName: ['black-overlay'],
		renderblackOverLay: function() {
			return $("<div></div>").addClass(this.overlayClassName.join(" "));
		},
		show: function() {
			this.setPosition();
			this.getElement().before(this.renderblackOverLay()).appendTo($("body")).show();
		}
	}
)

