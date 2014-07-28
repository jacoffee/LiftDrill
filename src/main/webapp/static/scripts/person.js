/**
 * Created by qbt-allen on 14-5-3.
 */
var
popupDiv = $.extend(
	BlackOverlayPop,
	{
		// actions actually an object in JavaScript key-value
		// the essence of the following codes is to generate html snippets with some sort of action binding
		sendInnerMail: function(title, content, actionObj) {
			var _this = this,
				$actionContainer = $("<div class='process' />");
			$.each(actionObj, function(val, action){
				var btn = $("<button type='button' class='send' />").html(val).unbind("click").click(function(){
					action();
				});
				var alink = $("<a href='javascript:;' class='cancel-send' />").text("取消").unbind("click").click(function(){
					// parent 只会匹配单一层级 而 parents则会匹配多个层级
					_this.hide();
					_this.getElement().siblings(".black-overlay").hide();
				});
				$actionContainer.append(btn).append(alink);
			});

			this.getElement().remove(".process");
			this.renderTitleElement(title).renderContentElement(content).getElement().append($actionContainer);
			this.show();
		}
	}
),
sendAsynData =$.extend({},
	{
		person: function(username, email) {
			return {
				"username": username,
				"email": email
			};
		}
	}
);
