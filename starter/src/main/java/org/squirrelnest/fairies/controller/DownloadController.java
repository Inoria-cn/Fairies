package org.squirrelnest.fairies.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.share.network.ResponseService;


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

    @RequestMapping("/piece")
    public String piece(@RequestParam String fileId,
                        @RequestParam Integer sliceIndex,
                        @RequestParam Integer pieceIndex) {
        HashCode160 id = HashCode160.parseString(fileId);
        return JSON.toJSONString(responseService.downloadPiece(id, sliceIndex, pieceIndex));
    }

    @RequestMapping("/sliceHash")
    public String sliceHash(@RequestParam String fileId,
                            @RequestParam Integer sliceIndex) {
        HashCode160 id = HashCode160.parseString(fileId);
        return JSON.toJSONString(responseService.sliceHash(id, sliceIndex));
    }
}
