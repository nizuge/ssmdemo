$(document).ready(function() {

    /*=======================自定义变量=========================*/
    var playCameraMac = null;
    var first, second, third;
    var identify_show_timeout;
    var video_websocket;
    var identify_websocket;

	/*================视频回显Websocket=======================*/

    if ('WebSocket' in window) {
        video_websocket = new WebSocket("ws://"+window.location.hostname+":8080/welcome/webSocketServer");
    } else if ('MozWebSocket' in window) {
        video_websocket = new MozWebSocket("ws://\""+window.location.hostname+":8080/welcome/webSocketServer");
    } else {
        video_websocket = new SockJS("http://localhost:8080/welcome/sockjs/webSocketServer");
    }

    video_websocket.onopen = function (evnt) {
        console.log("WebSocket连接成功");
      //playCameraMac = "38:A2:8C:DE:E7:51";
        //playCameraMac = "38:A2:8C:DE:EE:1F";
        playCameraMac = "D4:12:BB:12:F3:FB"
        video_websocket.send("mac:"+playCameraMac);

        // $("#msgcount").append("WebSocket链接开始！<br/>");
    };
    video_websocket.onmessage = function (evnt) {
        var data = evnt.data;
        if(data.indexOf("identify_response") != -1){
            var data = JSON.parse(data);
            // var base64Pic = "data:image;base64,"+pic;
            // var base64Pic = "data:image;base64,"+data['pic'];
            //
            // resultFace = base64Pic;
            if (second) {
                third = second;
                second = first;
            } else if (first) {
                second = first;
            }
            first = data;
            renderPic();
            console.log(base64Pic.length);
            $("#identify_response").attr("src",base64Pic);
		}else {
            var pic = "data:image;base64,"+data;
            $("#camera_show").attr("src",pic)
		}

    };
    video_websocket.onerror = function (evnt) {
        console.log("WebSocket连接出错");
        //$("#msgcount").append("WebSocket链接出错！<br/>");
    };
    video_websocket.onclose = function (evnt) {

        console.log("WebSocket连接关闭");
        //$("#msgcount").append("WebSocket链接关闭！<br/>");
    };

    /*================识别推送Websocket=======================*/
    if ('WebSocket' in window) {
        identify_websocket = new WebSocket("ws://"+window.location.hostname+":8080/welcome/ffWebSocket");
    } else if ('MozWebSocket' in window) {
        identify_websocket = new MozWebSocket("ws://\""+window.location.hostname+":8080/welcome/ffWebSocket");
    } else {
        identify_websocket = new SockJS("http://localhost:8080/welcome/sockjs/ffWebSocket");
    }

    identify_websocket.onopen = function (evnt) {
        console.log("WebSocket连接成功");
        identify_websocket.send("mac:"+playCameraMac);

    };
    identify_websocket.onmessage = function (evnt) {
        var data = evnt.data;
        if(data.indexOf("identify_response") != -1){
            var data = JSON.parse(data);
            // var base64Pic = "data:image;base64,"+pic;
            // var base64Pic = "data:image;base64,"+data['pic'];
            //
            // resultFace = base64Pic;
            if (second) {
                third = second;
                second = first;
            } else if (first) {
                second = first;
            }
            first = data;
            renderPic();
            console.log(base64Pic.length);
            $("#identify_response").attr("src",base64Pic);
        }else {
            var pic = "data:image;base64,"+data;
            $("#camera_show").attr("src",pic)
        }

    };
    identify_websocket.onerror = function (evnt) {
        console.log("WebSocket连接出错");
        //$("#msgcount").append("WebSocket链接出错！<br/>");
    };
    identify_websocket.onclose = function (evnt) {
        console.log("WebSocket连接关闭");
        //$("#msgcount").append("WebSocket链接关闭！<br/>");
    };

    /*=======================按钮事件===========================*/
    $("#select").click(function () {
        selectCamera();
    })
    $("#closeCamera").click(function () {
        $.ajax({
            url: "/welcome/closeCameraView",
            type: "GET",
            data: null,
            dataType: "text",
            success: function (data) {
                console.log("camera_close_success");
            },
            error: function () {
                console.log("camera_close_error");
            },
            processData: false,
            contentType: false
        });
    })
    /*=======================自定义方法=========================*/
    //选择摄像头
    function selectCamera(){
        if(playCameraMac == "D4:12:BB:12:F3:FA"){
            playCameraMac = "D4:12:BB:12:F3:FB";
            video_websocket.send("mac:"+playCameraMac);
            identify_websocket.send("mac:"+playCameraMac);
        }else if(playCameraMac == "D4:12:BB:12:F3:FB"){
            playCameraMac = "D4:12:BB:12:F3:FD";
            video_websocket.send("mac:"+playCameraMac);
            identify_websocket.send("mac:"+playCameraMac);
        }else {
            playCameraMac = "D4:12:BB:12:F3:FA";
            video_websocket.send("mac:"+playCameraMac);
            identify_websocket.send("mac:"+playCameraMac);
        }

    }
    //控制播放哪个摄像头的视频
    function cameraView(){
        $.ajax({
            url: "/welcome/cameraView?cameraMac="+playCameraMac,
            type: "GET",
            data: null,
            dataType: "text",
            success: function (data) {
                console.log("camera_select_success");
            },
            error: function () {
                console.log("camera_select_error");
            },
            processData: false,
            contentType: false
        });
    }
	//=======识别显示
    function renderPic() {
        if(identify_show_timeout){
            clearTimeout(identify_show_timeout);
        }
        if (first) {
            var firstHtml = '<div class="portrait">' +
                '<img src="' + "data:image;base64,"+first["pic"] + '"/></div>' +
                '<div class="t_text">' +
                '<h2>身份:<span>' + first["meta"] + '</span></h2>' +
                ' <h2>访问数量:<span>11</span></h2></div>';
            $('#first').html(firstHtml);
        }
        if (second) {
            var secondHtml = '<div class="portrait">' +
                '<img src="' + "data:image;base64,"+second["pic"] + '"/></div>' +
                '<div class="t_text">' +
                '<h2>身份:<span>' + second["meta"] + '</span></h2>' +
                ' <h2>访问数量:<span>11</span></h2></div>';
            $('#second').html(secondHtml);
        }
        if (third) {
            var thirdHtml = '<div class="portrait">' +
                '<img src="' + "data:image;base64,"+third["pic"] + '"/></div>' +
                '<div class="t_text">' +
                '<h2>身份:<span>' + third["meta"] + '</span></h2>' +
                ' <h2>访问数量:<span>11</span></h2></div>';
            $('#third').html(thirdHtml);
        }
        $('#faceFind').show();
        var e = jQuery.Event("resize");
        $('#faceFind').trigger(e);
        identify_show_timeout = setTimeout(function () {
            $('#faceFind').hide();
        }, 2500);
    }
//===============================================================================================
	function time() {
		var date = new Date();
		var n = date.getFullYear();
		var y = date.getMonth()+1;
		var t = date.getDate();
		var h = date.getHours();
		var m = date.getMinutes();
		var s = date.getSeconds();

		$('.ymd span').eq(0).html(n);
		$('.ymd span').eq(1).html(y);
		$('.ymd span').eq(2).html(t);
		$('.hms span').eq(0).html(h);
		$('.hms span').eq(1).html(m);
		$('.hms span').eq(2).html(s);
		for (var i = 0; i < $('div').length; i++) {
			if ($('div').eq(i).text().length == 1) {
				$('div').eq(i).html(function(index, html) {
					return 0 + html;
				});
			}
		}
		var mydate = new Date();
		var datetext=["星期天","星期一","星期二","星期三","星期四","星期五","星期六"]
		var xq=mydate.getDay();
		$(".xq").text(datetext[xq]);

		$(".cc-decoration").remove();
	}
	time();
	setInterval(time, 1000);

	$(".flip-item img ").css("height",$(".flip-item img").width());
	$(".portrait").css("height",$(".flip-item img").width());

	$(function() {
		$(".flipster").flipster({
			itemContainer: 'ul', // Container for the flippin' items.
			itemSelector: 'li', // Selector for children of itemContainer to flip
			style: 'coverflow', // Switch between 'coverflow' or 'carousel' display styles
			start: 'center', // Starting item. Set to 0 to start at the first, 'center' to start in the middle or the index of the item you want to start with.
			enableKeyboard: true, // Enable left/right arrow navigation
			enableMousewheel: true, // Enable scrollwheel navigation (up = left, down = right)
			enableTouch: true, // Enable swipe navigation for touch devices
			enableNav: true, // If true, flipster will insert an unordered list of the slides
			enableNavButtons: true, // If true, flipster will insert Previous / Next buttons
			onItemSwitch: function() {}, // Callback function when items are switches
		});
	});

});
