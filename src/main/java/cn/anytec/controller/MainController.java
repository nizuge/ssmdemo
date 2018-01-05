package cn.anytec.controller;

import cn.anytec.quadrant.ws.WsMessStore;
import cn.anytec.service.inf.ICustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    ICustomerService customerService;


    @RequestMapping(value = "/cameraView")
    @ResponseBody
    public void cameraView(HttpServletRequest request, HttpServletResponse response){

    }
    @RequestMapping(value = "/closeCameraView")
    @ResponseBody
    public void closeCameraView(HttpServletRequest request, HttpServletResponse response){
    }
}
