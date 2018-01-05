<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>迎宾系统</title>
    <link rel="stylesheet" href="./static/css/index.css"/>
    <link rel="stylesheet" href="./static/css/jquery.flipster.css"/>
    <script type="text/javascript" src="./static/js/jquery.min.js"></script>
    <script type="text/javascript" src="./static/js/index.js"></script>
    <script type="text/javascript" src="./static/js/jquery.flipster.js"></script>
    <%--<script type="text/javascript" src="ckplayer/ckplayer.js" charset="utf-8"></script>--%>
    <%--<script src="//cdn.bootcss.com/sockjs-client/1.0.3/sockjs.js"></script>--%>

</head>
<body>

<div id="faceFind" style="display:none" class="zzsc-container">
    <div class="zzsc-content bgcolor-3">
        <div id="Main-Content">
            <div class="Container">
                <div class="flipster">
                    <ul class="flip-items">
                        <li title="Cricket" data-flip-category="Fun Sports">
                            <div class="kc-item">
                                <div class="t_box" id="second">
                                    <div class="portrait">
                                    <img src=""/>
                                    </div>
                                    <div class="t_text">
                                    <h2>身份:<span>访客</span></h2>
                                    <h2>访问数量:<span>11</span></h2>
                                    </div>
                                </div>
                            </div>
                        </li>
                        <li>
                            <div class="kc-item">
                                <div class="t_box" id="first">
                                    <div class="portrait">
                                    <img src=""/>
                                    </div>
                                    <div class="t_text">
                                    <h2>身份:<span class="userMeta">访客</span></h2>
                                    <h2>访问数量:<span>11</span></h2>
                                    </div>

                                </div>
                            </div>
                        </li>
                        <li title="Baseball" data-flip-category="Boring Sports" >
                            <div class="kc-item">
                                <div class="t_box" id="third">
                                    <div class="portrait" >
                                    <img src=""/>
                                    </div>
                                    <div class="t_text">
                                    <h2>身份:<span class="userMeta">访客</span></h2>
                                    <h2>访问数量:<span>11</span></h2>
                                    </div>
                                </div>
                            </div>
                        </li>

                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="main">
    <div class="zzc"></div>
    <div class="content">
        <div class="top_box">
            <div class="logo"><img src="./static/img/logo.png"/></div>
        </div>
        <div class="middle_box">
            <div class="video_box">
                <%--<div class="video_text">视频区域</div>--%>
                <%--<div class="video_text"></div>--%>
                <img id="camera_show" src="" height="100%" width="100%"/>

                <%--<script type="text/javascript">
                    var rtmpAddress="rtmp://"+window.location.hostname+":1935/livecam"
                    var flashvars={
                        f:rtmpAddress,
                        c:0
                    };
                    var video=['http://localhost/live.m3u8'];
                    CKobject.embed('ckplayer/ckplayer.swf','a1','ckplayer_a1','600','400',false,flashvars,video);
                </script>--%>

            </div>
            <div class="time_box">
                <div class="mintime_box">
                    <div class="ymd">
                        <span></span> /
                        <span></span> /
                        <span></span>
                    </div>
                    <div class="img_box"><img src="./static/img/plate.png"/></div>
                </div>
                <div class="mintime_box">
                    <div class="hms">
                        <span></span> :
                        <span></span> :
                        <span></span>
                    </div>
                    <div class="img_box"><img src="./static/img/plate.png"/></div>
                </div>
                <div class="mintime_box">
                    <div class="xq"></div>
                    <div class="img_box"><img src="./static/img/plate.png"/></div>
                </div>
                <div class="four">
                    <div class="coordinate">深圳</div>
                    <div class="img_box"><img src="./static/img/plate.png"/></div>
                </div>
            </div>
        </div>
        <div class="bottom_box">
            <button id="select">切换</button>
            深圳市恩钛控股有限公司
        </div>
    </div>
</div>
</body>
<%--<script>
    var first, second, third
    var websocket;
    if ('WebSocket' in window) {
        websocket = new WebSocket("ws://" + window.location.hostname + ":8080/rtspWebsocket");
    } else if ('MozWebSocket' in window) {
        websocket = new MozWebSocket("ws://" + window.location.hostname + ":8080/rtspWebsocket");
    } else {
        websocket = new SockJS("http://" + window.location.hostname + ":8080/sockjs/rtspWebsocket");
    }

    websocket.onopen = function (evnt) {
        $("#msgcount").append("WebSocket链接开始！<br/>");
//        keepAlive()
    };
    websocket.onmessage = function (evnt) {
        console.log(evnt.data);
        $("#msgcount").html(evnt.data);
        if (evnt.data.indexOf("results") == -1) {
            return false;
        }
        if (evnt.data == "unregistered") {
            console.log("unregisted face");
            return
//            $(".userMeta").html("未注册人脸");
        }
        data = JSON.parse(evnt.data)["results"];
        for (face in data) {
            console.log(data[face])
            if (data[face].length == 0) {
                return false;
            }
            resultFace = data[face][0]["face"];
            if (second) {
                third = second;
                second = first;
            } else if (first) {
                second = first;
            }
            first = resultFace;
            renderPic();
//            $("#npcImg").attr("src", resultFace["normalized"]);
//            $(".userMeta").html(resultFace["meta"]);
            return;
        }

    };
    websocket.onerror = function (evnt) {
        $("#msgcount").append("WebSocket链接出错！<br/>");
        cancelKeepAlive()
    };
    websocket.onclose = function (evnt) {
        $("#msgcount").append("WebSocket链接关闭！<br/>");
        cancelKeepAlive()
    };

    var timerId = 0;

    function keepAlive() {
        var timeout = 20000;
        if (webSocket.readyState == webSocket.OPEN) {
            webSocket.send('');
        }
        timerId = setTimeout(keepAlive, timeout);
    }

    function cancelKeepAlive() {
        if (timerId) {
            clearTimeout(timerId);
        }
    }




</script>--%>
</html>