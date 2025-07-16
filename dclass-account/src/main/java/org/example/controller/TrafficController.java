package org.example.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.entity.TrafficDO;
import org.example.params.TrafficPageParam;
import org.example.params.UseTrafficParam;
import org.example.service.TrafficService;
import org.example.utils.JsonData;
import org.example.vo.TrafficVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dkw
 * @since 2025-05-20
 */
@RestController
@RequestMapping("/traffic")
public class TrafficController {

    @Autowired
    private TrafficService trafficService;

    @Value("${rpc.token}")
    private String rpcToken;


    /**
     * 使用流量包API
     * @param request
     * @return
     */
    @PostMapping("/reduce")
    public JsonData useTraffic(@RequestBody UseTrafficParam param, HttpServletRequest request){
        String requestToken = request.getHeader("rpc-token");
        if (requestToken.equalsIgnoreCase(rpcToken)) {
            //具体使用流量包逻辑  TODO
            JsonData jsonData = trafficService.reduce(param);
            return jsonData;
        } else {
            return JsonData.buildError("非法访问");
        }

    }



    /**
     * 分页查询流量包列表，查看可用的流量包
     * @return
     */
    @RequestMapping("/page")
    public JsonData pageAvailable(@RequestBody TrafficPageParam pageParam){

        IPage pageMap = trafficService.pageAvailable(pageParam);

        return JsonData.buildSuccess(pageMap);

    }


    /**
     * 查找某个流量包详情
     * @param trafficId
     * @return
     */
    @GetMapping("/detail/{trafficId}")
    public JsonData detail(@PathVariable("trafficId") long trafficId){

        TrafficDO trafficDO = trafficService.findByIdAndAccountNo(trafficId);

        return JsonData.buildSuccess(trafficDO);
    }
}

