/*
* This js file defines all kinds of utility method
* */

PageScroll = $.extend(
	{}, {
		getScrollOffsets: function() {
//			if (w.pageXOffset) {
//				console.log(" pageXOffset  ");
//				return { 'x': w.pageXOffset, 'y': w.pageYOffset };
//			}

			// if In IE CompatMode
//			var d = w.document;
//			if (document.compatMode === 'CSS1Compat') {
//				console.log(" CSS1Compat ");
//				return { 'x': d.documentElement.scrollLeft, 'y': d.documentElement.scrollTop };
//			}
//
//			// If in Quirk Mode
//			return { 'x': d.body.scrollLeft, 'y': d.body.scrollTop };

			return { 'x': $(document).scrollLeft(), 'y': $(document).scrollTop() };

		},
		drag: function(elementToDrag, event) {
			// get scroll distance
			var _this = this;

			// The initial mouse position, converted to document coordinates
			// equals to the scroll Distance + current event postition
			// 指针的初始位置
			var scroll = _this.getScrollOffsets();

			var startX = event.clientX + scroll.x; // clientX stands for the 鼠标点击点的相对于 viewport 的距离
			var startY = event.clientY + scroll.y;

			// The original position (in document coordinates) of the element
			// that is going to be dragged. Since elementToDrag is absolutely
			// positioned, we assume that its offsetParent is the document body
			// 元素在文档中的位置
			var origOffSet =  elementToDrag.offset(); // relative to document
			var origX = origOffSet.left;
			var origY = origOffSet.top;

			// Compute the distance between the mouse down event and the upper-left
			// corner of the element. We'll maintain this distance as the mouse moves.
			var deltaX = startX - origX;
			var deltaY = startY - origY;

			// register event except IE
			// true means capturing event cause mouse move happens so fast  more than document element can follow it
			document.addEventListener('mousemove', moveHandler, true);
			document.addEventListener('mouseup', upHandler, true);

			event.stopPropagation();
			event.preventDefault();

			function moveHandler(e) {
				var e = e  || window.event;
				var scoll = _this.getScrollOffsets();
				// elementToDrag.left = (e.clientX + scroll.x - deltaX) + "px";
				// elementToDrag.top = (e.clientY + scroll.y- deltaY) + "px";
				elementToDrag.offset(
					{
						'left': (e.clientX + scroll.x - deltaX), // -deltaX 是为了让 left能够以元素的上边界为准
						'top': (e.clientY + scroll.y - deltaY)
					}
				);
				// elementToDrag.css('left', (e.clientX + scroll.x - deltaX) + "px");
				// elementToDrag.css('top', (e.clientY + scroll.y - deltaY) + "px"); has some problem
				e.stopPropagation();
			}
			function upHandler(e) {
				document.removeEventListener('mouseup', upHandler, true);
				document.removeEventListener('mousemove', moveHandler, true);
				e.stopPropagation();
			}
		}
	}
)