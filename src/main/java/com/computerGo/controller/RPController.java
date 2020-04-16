package com.computerGo.controller;


import com.computerGo.DTO.RepertoryDTO;
import com.computerGo.base.ResultUtil;
import com.computerGo.base.dto.ResultDTO;
import com.computerGo.base.redis.RedisUtil;
import com.computerGo.pojo.Package;
import com.computerGo.pojo.RP;
import com.computerGo.pojo.Repertory;
import com.computerGo.service.PackageService;
import com.computerGo.service.RPService;
import com.computerGo.service.RepertoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName RPController
 * @Description TODO
 * @Author QQ163
 * @Date 2020/4/15 18:23
 **/
@Controller
@RequestMapping("/RPController")
@Api(value = "RPController")
public class RPController {
    @Autowired
    private RPService rpService;
    @Autowired
    private RepertoryService repertoryService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private PackageService packageService;


    @GetMapping("/getrepertory")
    @ResponseBody
    @ApiOperation(value = "根据类型获取库存",notes = "500报错")
    public ResultDTO getrepertory(
            @ApiParam(value = "库存类型 0电脑 1cpu 2主板 3显卡 4散热器 5内存 ",required = true)
            @RequestParam(value = "type",required = true) Integer type,
            @ApiParam(value = "起始位置",required = true)@RequestParam(value = "offset",required = true) int offset,
            @ApiParam(value = "数据条数",required = true)@RequestParam(value = "limit",required = true) int limit){
        try {
            List<RP> rpList = rpService.selectByType(type,offset,limit);
            List<RepertoryDTO> repertoryDTOS = new ArrayList<>();
            for (RP rp : rpList){
                RepertoryDTO repertoryDTO = new RepertoryDTO();
                try {
                    repertoryDTO.setRepertoryDTO(repertoryService.selectByRid(rp.getRid()));
                    List<Package> packageList = new ArrayList<>();
                    for (RP newrp : rpService.selectByRid(rp.getRid())){
                        packageList.add(packageService.selectByPid(newrp.getPid()));
                    }
                    repertoryDTO.setPackageList(packageList);
                    repertoryDTO.setWatched(redisUtil.get(rp.getRid().toString()).toString());
                }catch (Exception e){
                    continue;
                }
                repertoryDTOS.add(repertoryDTO);
            }
            return new ResultUtil().Success(repertoryDTOS);
        }catch (Exception e){
            return new ResultUtil().Error("500",e.toString());
        }
    }

    @GetMapping("/getRepertoryByTitle")
    @ResponseBody
    @ApiOperation(value = "根据标题获取库存",notes = "500报错")
    public ResultDTO getRepertoryByTitle(
            @ApiParam(value = "标题",required = true) @RequestParam(value = "title",required = true) String title,
            @ApiParam(value = "起始位置",required = true)@RequestParam(value = "offset",required = true) int offset,
            @ApiParam(value = "数据条数",required = true)@RequestParam(value = "limit",required = true) int limit){
        try {
            List<Repertory> repertoryList = repertoryService.selectByTitle(title,offset,limit);
            List<RepertoryDTO> repertoryDTOS = new ArrayList<>();
            for (Repertory repertory : repertoryList){
                try {
                    RepertoryDTO repertoryDTO = new RepertoryDTO();
                    repertoryDTO.setRepertoryDTO(repertory);
                    List<Package> packages = new ArrayList<>();
                    boolean b=true;
                    for (RP rp : rpService.selectByRid(repertory.getRid())){
                        if (rp.getType() != 0){
                            b=false;
                            break;
                        }
                        packages.add(packageService.selectByPid(rp.getPid()));
                    }
                    repertoryDTO.setPackageList(packages);
                    repertoryDTO.setWatched(redisUtil.get(repertory.getRid().toString()).toString());
                    if (b){
                        repertoryDTOS.add(repertoryDTO);
                    }
                }catch (Exception e){
                    continue;
                }
            }
            return new ResultUtil().Success(repertoryDTOS);
        }catch (Exception e){
            return new ResultUtil().Error("500",e.toString());
        }
    }
}
