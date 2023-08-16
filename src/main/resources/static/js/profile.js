$(function(){
	$(".follow-btn").click(follow);
});
function follow() {
	var btn = this;
	if($(btn).hasClass("btn-info")) {
		// 关注TA
		$.post(
		    CONTEXT_PATH + "/follow",
		    {"entityType":3,"entityId":$(btn).prev().val()},
		    function(data) {
		        data = $.parseJSON(data);
		        if(data.code == 0) {
                    window.location.reload();
		        } else {
                    alert(data.msg);
		        }
		    }
		);
		//本来异步请求应该让发生改变的数据进行更新
		//但是要更新的太多，懒得写，就用window.location.reload();重新加载页面
		//实际上不能这么写
		// $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
	} else {
		// 取消关注
		$.post(
		    CONTEXT_PATH + "/unfollow",
		    {"entityType":3,"entityId":$(btn).prev().val()},
		    function(data) {
		        data = $.parseJSON(data);
		        if(data.code == 0) {
                    window.location.reload();
		        } else {
                    alert(data.msg);
		        }
		    }
		);
		//$(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
	}
}