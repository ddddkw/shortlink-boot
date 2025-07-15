package org.example.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.config.RabbitMQConfig;
import org.example.constant.RedisKey;
import org.example.entity.TrafficDO;
import org.example.entity.TrafficTaskDO;
import org.example.enums.*;
import org.example.exception.BizException;
import org.example.feign.ProductFeignService;
import org.example.feign.ShortLinkFeignService;
import org.example.interceptor.LoginInterceptor;
import org.example.mapper.TrafficMapper;
import org.example.model.EventMessage;
import org.example.params.TrafficPageParam;
import org.example.params.UseTrafficParam;
import org.example.service.TrafficService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.service.TrafficTaskService;
import org.example.utils.JsonData;
import org.example.utils.JsonUtil;
import org.example.utils.TimeUtil;
import org.example.vo.ProductVO;
import org.example.vo.UserTrafficVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dkw
 * @since 2025-05-20
 */
@Service
@Slf4j
public class TrafficServiceImpl extends ServiceImpl<TrafficMapper, TrafficDO> implements TrafficService {



    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ShortLinkFeignService shortLinkFeignService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TrafficTaskService trafficTaskService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    /**
     * 新增流量包
     * @param trafficDO
     * @return
     */
    public int add(TrafficDO trafficDO){
        return this.baseMapper.insert(trafficDO);
    }

    /**
     * 分页查询可用的流量包
     * @return
     */
    public IPage<TrafficDO> pageAvailable(TrafficPageParam pageParam){
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        Page<TrafficDO> pageInfo = new Page<>(pageParam.getPage(),pageParam.getSize());
        String today = TimeUtil.format(new Date(),"yyyy-MM-dd");
        Page<TrafficDO> trafficPage = this.baseMapper.selectPage(pageInfo, new QueryWrapper<TrafficDO>()
                .eq("account_no", accountNo)
                .ge("expired_date", today)
                .orderByDesc("gmt_create"));
        return trafficPage;
    }

    /**
     * 查找详情
     * @param trafficId
     * @return
     */
    public TrafficDO findByIdAndAccountNo(Long trafficId){
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        return this.baseMapper.selectOne(new QueryWrapper<TrafficDO>().eq("id",trafficId).eq("account_no",accountNo));
    }

    /**
     * 增加流量包天使用次数
     * @param trafficId
     * @param accountNo
     * @param usedTimes
     * @return
     */
    public int addDayUsedTimes(long trafficId, long accountNo,int usedTimes){
        return this.baseMapper.addDayUsedTimes(trafficId,accountNo,usedTimes);
    }

    /**
     * 查找可使用的流量包（未过期）,包括免费流量包
     * @return
     */
    public List<TrafficDO> selectAvailableTraffics(long accountNo){
        String today = TimeUtil.format(new Date(),"yyyy-MM-dd");
        QueryWrapper<TrafficDO> queryWrapper = new QueryWrapper<TrafficDO>();
        queryWrapper.eq("account_no",accountNo);
        queryWrapper.and(wrapper->wrapper.ge("expired_date",today).or().eq("out_trade_no","free_init"));
        return this.baseMapper.selectList(queryWrapper);
    }

    /**
     * 恢复流量包使用次数，如某个短链创建失败，回滚次数
     * @return
     */
    public int initUsedTimes(long accountNo,long trafficId, int usedTimes){
        return this.baseMapper.initUsedTimes(trafficId,accountNo,usedTimes);
    }

    /**
     * 批量更新流量包使用次数为0
     * @return
     */
    public int batchUpdateUsedTimes(long accountNo, List<Long> unUpdatedTrafficIds){
        int row = this.baseMapper.update(null, new UpdateWrapper<TrafficDO>()
                .eq("account_no", accountNo)
                .in("id", unUpdatedTrafficIds)
                .set("day_used", 0));
        return row;
    }



    /**
     * 主要是执行流量包发放操作以及流量包回滚操作
     * @param eventMessage
     */
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public void handlerTrafficMessage(EventMessage eventMessage){
        String messageType = eventMessage.getEventMessageType();
        Long accountNo = eventMessage.getAccountNo();
        if (EventMessageType.PRODUCT_ORDER_PAY.name().equalsIgnoreCase(messageType)) {
            // 订单已经支付，新增流量包
            String content = eventMessage.getContent();
            Map orderInfoMap = JsonUtil.json2Obj(content, Map.class);
            // 还原商品信息
            String outTradeNo = (String) orderInfoMap.get("out_trade_no");
            String tradeState = (String) orderInfoMap.get("trade_state");
            int buyNum = (int) orderInfoMap.get("buyNum");
            String productStr = (String) orderInfoMap.get("product");
            ProductVO productVO = JsonUtil.json2Obj(productStr, ProductVO.class);
            log.info("商品信息：{}",productVO);

            // 流量表有效期
            LocalDateTime expiredDateTime = LocalDateTime.now().plusDays(productVO.getValidDay());
            Date date = Date.from(expiredDateTime.atZone(ZoneId.systemDefault()).toInstant());
            TrafficDO trafficDO = TrafficDO.builder()
                    .accountNo(accountNo)
                    .dayLimit(productVO.getDayTimes()*buyNum)
                    .dayUsed(0)
                    .totalLimit(productVO.getTotalTimes())
                    .pluginType(productVO.getPluginType())
                    .level(productVO.getLevel())
                    .productId(productVO.getId())
                    .outTradeNo(outTradeNo)
                    .expiredDate(date)
                    .build();
            int rows = this.add(trafficDO);
            log.info("消费消息新增流量包：{}",rows);

            // 新增流量包应该删除redis中的缓存key
            String totalTrafficTimesKey = String.format(RedisKey.DAY_TOTAL_TRAFFIC,accountNo);
            redisTemplate.delete(totalTrafficTimesKey);
        } else if (EventMessageType.TRAFFIC_FREE_INIT.name().equalsIgnoreCase(messageType)) {
            // 免费流量包发放
            Long productId = Long.valueOf(eventMessage.getBizId());
            JsonData jsonData = productFeignService.detail(productId);
            ProductVO productVO = jsonData.getData(new TypeReference<ProductVO>(){});
            TrafficDO trafficDO = TrafficDO.builder()
                    .accountNo(accountNo)
                    .dayLimit(productVO.getDayTimes())
                    .dayUsed(0)
                    .totalLimit(productVO.getTotalTimes())
                    .pluginType(productVO.getPluginType())
                    .level(productVO.getLevel())
                    .productId(productVO.getId())
                    .outTradeNo("free_init")
                    .expiredDate(new Date())
                    .build();
            int rows = this.add(trafficDO);
        } else if (EventMessageType.TRAFFIC_USED.name().equalsIgnoreCase(messageType)) {
            // 检查task是否存在
            // 检查短链码是否成功生成
            Long trafficTaskId = Long.valueOf(eventMessage.getBizId());
            TrafficTaskDO trafficTaskDo = trafficTaskService.findByIdAndAccountNo(trafficTaskId, eventMessage.getAccountNo());
            // 非空且是锁定
            if (trafficTaskDo!=null && trafficTaskDo.getLockState().equalsIgnoreCase(TaskStateEnum.LOCK.name())) {
                JsonData jsonData = shortLinkFeignService.check(eventMessage.getBizId());
                if (jsonData.getCode()!=0) {
                    log.error("创建短链失败，流量包回滚");
                    initUsedTimes(trafficTaskDo.getTrafficId(),accountNo,1);
                }
                trafficTaskService.deleteByIdAndAccountNo(trafficTaskDo.getId(),accountNo);
            }

        }
    }

    public boolean deleteExpireTraffic(){

        int rows = this.baseMapper.delete(new QueryWrapper<TrafficDO>().le("expired_date",new Date()));
        log.info("删除过期；流量包行数：{}",rows);

        return true;
    }

    /**
     * 扣减流量包
     * @param param
     * @return
     */
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public JsonData reduce(UseTrafficParam param){

        Long accountNo = param.getAccountNo();
        // 筛选出未更新流量包，当前使用的流量包
        UserTrafficVo userTrafficVo = processTrafficList(accountNo);

        log.info("今日可用总次数:{},当前使用流量包:{}",userTrafficVo.getDayTotalLeftTimes(),userTrafficVo.getCurrentTrafficDO());
        if (userTrafficVo.getCurrentTrafficDO() == null) {
            return JsonData.buildResult(BizCodeEnum.TRAFFIC_REDUCE_FAIL);
        }
        log.info("待更新流量包列表：{}",userTrafficVo.getUnUpdatedTrafficIds());

        if (userTrafficVo.getUnUpdatedTrafficIds().size()>0) {
            // 更新今日流量包
            batchUpdateUsedTimes(accountNo,userTrafficVo.getUnUpdatedTrafficIds());
        }
        // 扣减使用次数
        int rows = addDayUsedTimes(userTrafficVo.getCurrentTrafficDO().getId(),accountNo,1);
        // 生成traffic_task并插入到数据库
        TrafficTaskDO trafficTaskDO = new TrafficTaskDO().builder().accountNo(accountNo)
                .bizId(param.getBizId())
                .useTimes(1)
                .trafficId(userTrafficVo.getCurrentTrafficDO().getId())
                .lockState(TaskStateEnum.LOCK.name())
                .build();
        //ORM 框架会在插入前自动为 id 字段生成雪花算法 ID，并赋值给 trafficTaskDO，所以下方在eventMessage使用trafficTaskDO.getId()时是可以获取到id字段的值的
        trafficTaskService.add(trafficTaskDO);
        if (rows!=1) {
            throw new BizException(BizCodeEnum.TRAFFIC_REDUCE_FAIL);
        }

        // 向redis中设置总流量包次数，短链服务那边递减即可；如果有新增流量包则删除这个key
        long leftSeconds = TimeUtil.getRemainSecondsOneDay(new Date());
        String totalTrafficTimesKey = String.format(RedisKey.DAY_TOTAL_TRAFFIC,accountNo);
        // 每次扣减流量包时，通过redis缓存今天剩余的可用次数
        redisTemplate.opsForValue().set(totalTrafficTimesKey,userTrafficVo.getDayTotalLeftTimes()-1,leftSeconds,TimeUnit.SECONDS);

        EventMessage eventMessage = EventMessage.builder().accountNo(accountNo).bizId(trafficTaskDO.getId() + "")
                .eventMessageType(EventMessageType.TRAFFIC_USED.name()).build();
        // 发送延迟消息，用于异常回滚
        rabbitTemplate.convertAndSend(rabbitMQConfig.getTrafficEventExchange(),rabbitMQConfig.getTrafficReleaseDelayRoutingKey(),eventMessage);
        return JsonData.buildSuccess();
    }

    public UserTrafficVo processTrafficList( Long accountNo){
        List<TrafficDO> list = selectAvailableTraffics(accountNo);
        if (list==null || list.size()==0) {
            throw new BizException(BizCodeEnum.TRAFFIC_EXCEPTION);
        }

        // 天剩余总次数
        Integer dayTotalLeftTimes = 0;
        // 当前使用的流量包
        TrafficDO currentTrafficDO = null;
        // 没过期，但是今天没更新的流量包id列表
        List<Long> unUpdateTrafficIds = new ArrayList<>();
        //今天日期
        String todayStr = TimeUtil.format(new Date(),"yyyy-MM-dd");
        for (TrafficDO trafficDO:list) {
            String trafficUpdateDate = TimeUtil.format(trafficDO.getGmtModified(),"yyyy-MM-dd");
            if (todayStr.equalsIgnoreCase(trafficUpdateDate)) {
                // 已经更新
                int dayLeftTimes = trafficDO.getDayLimit() - trafficDO.getDayUsed();
                dayTotalLeftTimes = dayTotalLeftTimes + dayLeftTimes;

                // 选取当次使用流量包
                if (dayLeftTimes>0 && currentTrafficDO==null) {
                    currentTrafficDO = trafficDO;
                }
            } else {
                // 未更新-说明今天还没使用过，所有限制的次数总和就是可用的次数，然后再对所有流量包进行更新和扣减使用次数
                dayTotalLeftTimes = dayTotalLeftTimes + trafficDO.getDayLimit();
                // 记录未更新的流量包
                unUpdateTrafficIds.add(trafficDO.getId());
                // 选取当次使用的流量包
                if (currentTrafficDO==null){
                    currentTrafficDO = trafficDO;
                }
            }
        }
        UserTrafficVo userTrafficVo = new UserTrafficVo();
        userTrafficVo.setCurrentTrafficDO(currentTrafficDO);
        userTrafficVo.setDayTotalLeftTimes(dayTotalLeftTimes);
        userTrafficVo.setUnUpdatedTrafficIds(unUpdateTrafficIds);
        return userTrafficVo;
    }

}
