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
import org.squirrelnest.fairies.dto.FindValueResult;
import org.squirrelnest.fairies.dto.PingResult;
import org.squirrelnest.fairies.dto.StoreResult;
import org.squirrelnest.fairies.service.ResponseService;
import org.squirrelnest.fairies.utils.RequestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * DHT系统所有可能接收的请求：ping, find_node, join_node(新节点加入), find_value(file or keyword),
 * ping, add_file_for_keyword, store_keyword(for refresh only), store_file
 *
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
            LOGGER.error("Announced ip mismatch with fetched ip , first is " + announceIp + " and second is " + fetchedIp);
            return null;
        }
        return fetchedIp;
    }

    @GetMapping("/findNode")
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

    @GetMapping("/findValue")
    public String handleFindValue(@RequestParam String nodeId,
                                  @RequestParam String nodeIp,
                                  @RequestParam String nodePort,
                                  @RequestParam String targetId,
                                  @RequestParam String type,
                                  HttpServletRequest request) {
        HashCode160 client = HashCode160.parseString(nodeId);
        HashCode160 target = HashCode160.parseString(targetId);

        String checkedIp = checkAndGetIp(nodeIp, request);
        if (StringUtils.isBlank(checkedIp)) {
            return null;
        }

        FindValueResult result = responseService.findValue(client, nodeIp, nodePort, target, type);
        return JSON.toJSONString(result);
    }

    @GetMapping("/store")
    public String handleFindValue(@RequestParam String nodeId,
                                  @RequestParam String nodeIp,
                                  @RequestParam String nodePort,
                                  @RequestParam String key,
                                  @RequestParam String value,
                                  @RequestParam String type,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String expireTime,
                                  HttpServletRequest request) {
        HashCode160 client = HashCode160.parseString(nodeId);
        HashCode160 keyId = HashCode160.parseString(key);

        String checkedIp = checkAndGetIp(nodeIp, request);
        if (StringUtils.isBlank(checkedIp)) {
            return null;
        }

        StoreResult result = responseService.store(client, nodeIp, nodePort, keyId, value, type, keyword, expireTime);
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
