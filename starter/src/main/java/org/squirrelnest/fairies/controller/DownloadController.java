package org.squirrelnest.fairies.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.share.service.ResponseService;


import javax.annotation.Resource;

/**
 * p2p直连可能接收的请求：FILE_BITMAP, GET_PIECE,
 *
 * Created by Inoria on 2019/3/17.
 */
@Controller
@RequestMapping("/download")
public class DownloadController {

    @Resource
    private ResponseService responseService;

    @RequestMapping("/fileData")
    public String fileData(@RequestParam String fileId) {
        HashCode160 id = HashCode160.parseString(fileId);
        return JSON.toJSONString(responseService.fileInfo(id));
    }
}
