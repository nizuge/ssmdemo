<%--
  Created by IntelliJ IDEA.
  User: anytec-z
  Date: 17-12-19
  Time: 下午6:14
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Welcome</title>
    <script src="static/js/jQuery-3.2.1.src.js" type="text/javascript"></script>
    <script type="text/javascript">
        $(document).ready(function () {
            /*===================Websocket=====================*/
            var websocket;
            if ('WebSocket' in window) {
                websocket = new WebSocket("ws://"+window.location.hostname+":8080/welcome/webSocketServer");
            } else if ('MozWebSocket' in window) {
                websocket = new MozWebSocket("ws://\""+window.location.hostname+":8080/welcome/webSocketServer");
            } else {
                websocket = new SockJS("http://localhost:8080/welcome/sockjs/webSocketServer");
            }

            websocket.onopen = function (evnt) {
                document.getElementById("message").innerHTML="<h3>websocket连接成功</h3>";
                console.log("WebSocket连接成功");
                // $("#msgcount").append("WebSocket链接开始！<br/>");
            };
            websocket.onmessage = function (evnt) {
                var pic = evnt.data;
                var pic2 = "data:image;base64,"+pic;
                var demo = '<div class="img_border"><img src="' + pic2 + '"/></div>';
                $("#camera").html(demo);
            };
            websocket.onerror = function (evnt) {
                console.log("WebSocket连接出错");
                //$("#msgcount").append("WebSocket链接出错！<br/>");
            };
            websocket.onclose = function (evnt) {
                alert("websocket连接断开！请按ctrl+F5刷新网页");
                console.log("WebSocket连接关闭");
                //$("#msgcount").append("WebSocket链接关闭！<br/>");
            };
            /*=======================自定义变量=========================*/
            var playCameraMac = null;
            /*=======================按钮事件===========================*/
            $("#btnCam0").click(function () {
                playCameraMac = "38:A2:8C:DE:EE:1F";
                cameraView();
            })
            $("#closeCamera").click(function () {
                $.ajax({
                    url: "/welcome/closeCameraView",
                    type: "GET",
                    data: null,
                    dataType: "text",
                    success: function (data) {
                        alert("success");
                    },
                    error: function () {
                        alert("error");
                    },
                    processData: false,
                    contentType: false
                });
            })
            /*=======================自定义方法=========================*/
            function cameraView(){
                $.ajax({
                    url: "/welcome/cameraView?cameraMac="+playCameraMac,
                    type: "GET",
                    data: null,
                    dataType: "text",
                    success: function (data) {
                        alert("success");
                    },
                    error: function () {
                        alert("error");
                    },
                    processData: false,
                    contentType: false
                });
            }
        });

    </script>
</head>
<button id="btnCam0">btnCam0</button>
<button id="closeCamera" >close</button>
<div id="camera"></div>
<div id="message"></div>
</body>
</html>
