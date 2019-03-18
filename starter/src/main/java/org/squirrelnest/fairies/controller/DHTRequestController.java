package org.squirrelnest.fairies.controller;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.dto.FindNodeResult;
import org.squirrelnest.fairies.dto.PingResult;
import org.squirrelnest.fairies.service.ResponseService;
import org.squirrelnest.fairies.utils.RequestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by Inoria on 2019/3/17.
 */
@Controller
@RequestMapping("/DHT")
public class DHTRequestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DHTRequestController.class);

    @Resource
    private ResponseService responseService;

    private String checkAndGetIp(String announceIp, HttpServletRequest request) {
        String fetchedIp = RequestUtils.getRequestIp(request);
        //客户端发来的自己ip和通过request获取到的ip不能匹配
        if(StringUtils.isNotBlank(announceIp) && !announceIp.equalsIgnoreCase(fetchedIp)) {
            return null;
        }
        return fetchedIp;
    }

    @GetMapping
    public String handleFindNode(@RequestParam String nodeId,
                                 @RequestParam String nodeIp,
                                 @RequestParam String nodePort,
                                 @RequestParam String targetId,
                                 HttpServletRequest request) {
        HashCode160 client = HashCode160.parseString(nodeId);
        HashCode160 target = HashCode160.parseString(targetId);

        String checkedIp = checkAndGetIp(nodeIp, request);
        if (StringUtils.isBlank(checkedIp)) {
            return null;
        }

        FindNodeResult result = responseService.findNode(client, checkedIp, nodePort, target);
        return JSON.toJSONString(result);
    }

    @GetMapping("/ping")
    public String handlePing(@RequestParam String nodeId,
                             @RequestParam String nodeIp,
                             @RequestParam String nodePort,
                             HttpServletRequest request) {
        HashCode160 id = HashCode160.parseString(nodeId);

        String checkedIp = checkAndGetIp(nodeIp, request);
        if (StringUtils.isBlank(checkedIp)) {
            return null;
        }

        PingResult result = responseService.ping(id, checkedIp, nodePort);
        return JSON.toJSONString(result);
    }
}
