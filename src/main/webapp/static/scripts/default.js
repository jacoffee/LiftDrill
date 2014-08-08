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
		// == will automatically trigger conversion string  to int
		return !str || str.length === 0;
	},
	isNullable: function(value) {
		return value === null || value === undefined;
	}
},
BasePopup = {
	classNames: ['model-dialog'],

	titleClassNames: ["title"],

	contentClassNames: ["content-wrapper"],

	actionClassNames: ["action-container"],

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
				top: $(document).scrollTop + top + "px"
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
	renderActionElement: function(actionElem) {
		this.getElement().find("." +this.actionClassNames[0]).html(actionElem);
		return this;
	},
	getElement: function() {
		// if the current object has no element attr
		if (Helper.isNullable(this.element)) {
			// dynamic add attribute for JavaScript
			// just add empty the rest will be filled with othe method
			var _this = this;

			$(document).keydown(function(event){
				if (event.keyCode === 27) {
					_this.hide();
				}
			});

			this.element = $("<div></div>").addClass(this.classNames.join(" ")).css(
				{
					"z-index": this.zIndex
				}
			).append(
				$("<div></div>").addClass(this.titleClassNames.join(" "))
			).append(
				$("<div></div>").addClass(this.contentClassNames.join(" "))
			).append(
				$("<div></div>").addClass(this.actionClassNames.join(" "))
			);

		}
		return this.element;
	},
	hide: function() {
		this.getElement().remove();
	},
	show: function() {
		this.setPosition();
		this.getElement().appendTo($("body")).show();
		$(document).delegate('div.'+this.classNames[0], 'mousedown', function(e) {
			PageScroll.drag($(this), e);
		});
	}
}
BlackOverlayPop = $.extend(
	BasePopup,
	{
		overlayClassName: ['black-overlay'],
		renderblackOverLay: function() {
			return $("<div></div>").addClass(this.overlayClassName.join(" "));
		}
	}
)

// First, define two simple functions
var sum = function(x,y) { return x+y; };
var square = function(x) { return x*x; };
// Then use those functions with Array methods to compute mean and stddev
var data = [1,1,3,5,5];
var mean = data.reduce(sum)/data.length;
var deviations = data.map(function(x) {return x-mean;});
var stddev = Math.sqrt(deviations.map(square).reduce(sum)/(data.length-1));

// Array Constructor to define
//---- []
var a = new Array();
//---- length is 10 b[0] = undefined
var b = new Array(10);

// iterate array
var obj = {x: 1, y: 2, z: 3}
var arr = Object.keys(obj)  // [x, y, z]
arr.reduce(function(x, y) {return x+y; })
var objValues = [];
for (var i in arr) { // this is used to converse object rather array
	console.log(i); // key is  0 1 2
	var key= arr[i];
	var objValues = [];
	//objValues.push(obj[key]);  // put the current value in the Array so in this way it only returns the last value
	objValues[i] = obj[key]
}
console.log(objValues);
