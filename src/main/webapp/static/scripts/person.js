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
			var $actionContainer = $("<div class='process' />");
			$.each(actionObj, function(val, action){
				var btn = $("<button type='button' />").html(val).addClass("send").unbind("click").click(function(){
					action();
				});
				var alink = $("<a href='javascript:;' />").text("取消").addClass("cancel-send").unbind("click").click(function(){
					// parent 只会匹配单一层级 而 parents则会匹配多个层级
					$(this).parents(".model-dialog").hide();
					$(this).parents(".model-dialog").siblings(".black-overlay").hide();
				});
				$actionContainer.append(btn).append(alink);
			});
			console.log(typeof  content);

			this.getElement();
			function appendedAction() {
				console.log(this === undefined); // in non-strict model, this refers to global varible
				//return this.getElement().find(".process") ? $() : $actionContainer;
			}
			$("div").remove(".process");
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
)
