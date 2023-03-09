package com.mall.order.services;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.order.OrderCoreService;
import com.mall.order.biz.TransOutboundInvoker;
import com.mall.order.biz.context.AbsTransHandlerContext;
import com.mall.order.biz.factory.OrderProcessPipelineFactory;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.constants.OrderConstants;
import com.mall.order.converter.OrderConverter;
import com.mall.order.dal.entitys.Order;
import com.mall.order.dal.entitys.OrderItem;
import com.mall.order.dal.entitys.Stock;
import com.mall.order.dal.entitys.OrderShipping;
import com.mall.order.dal.persistence.OrderItemMapper;
import com.mall.order.dal.persistence.OrderMapper;
import com.mall.order.dal.persistence.OrderShippingMapper;
import com.mall.order.dal.persistence.StockMapper;
import com.mall.order.dto.*;
import com.mall.order.utils.ExceptionProcessorUtils;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.DeleteAddressResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * cskaoyan
 * create-date: 2019/7/30-上午10:05
 */
@Slf4j
@Component
@Service(cluster = "failfast")
public class OrderCoreServiceImpl implements OrderCoreService {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderItemMapper orderItemMapper;

    @Autowired
    OrderShippingMapper orderShippingMapper;

    @Autowired
    OrderProcessPipelineFactory orderProcessPipelineFactory;

    @Autowired
    StockMapper stockMapper;

	@Autowired
	OrderConverter orderConverter;



    /**
     * 创建订单的处理流程
     *
     * @param request
     * @return
     */
    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        CreateOrderResponse response = new CreateOrderResponse();
        try {
            //创建pipeline对象
            TransOutboundInvoker invoker = orderProcessPipelineFactory.build(request);

            //启动pipeline
            invoker.start(); //启动流程（pipeline来处理）

            //获取处理结果
            AbsTransHandlerContext context = invoker.getContext();

            //把处理结果转换为response
            response = (CreateOrderResponse) context.getConvert().convertCtx2Respond(context);
        } catch (Exception e) {
            log.error("OrderCoreServiceImpl.createOrder Occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(response, e);
        }
        return response;
    }

    //删除订单的处理流程
    @Override
    public DeleteOrderResponse deleteOrder(DeleteOrderRequest request) {
        log.error("begin - OrderCoreServiceImpl.deleteOrder request :" + request);
        DeleteOrderResponse response = new DeleteOrderResponse();
        try {
            request.requestCheck();
            int row = orderMapper.deleteByPrimaryKey(request.getOrderId());
            if (row > 0) {
                response.setCode(SysRetCodeConstants.SUCCESS.getCode());
                response.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
            } else {
                response.setCode(SysRetCodeConstants.DATA_NOT_EXIST.getCode());
                response.setMsg(SysRetCodeConstants.DATA_NOT_EXIST.getMessage());
            }
            log.info("OrderCoreServiceImpl.deleteOrder effect row :" + row);
        } catch (Exception e) {
            log.error("OrderCoreServiceImpl.deleteOrder occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(response, e);
        }
        return response;
    }

    //取消订单的更新流程

    /**
     * 1.通过订单id更新订单状态信息status置为 5交易关闭, 更新update_time,close_time
     * 2.通过订单id更新order_item表,status置为 2库存已释放, 更新update_time
     * 3.更新stock表锁定库存加到库存数量上,锁定库存清0
     *
     * @param request
     * @return
     */
    @Override
    public CancelOrderResponse cancelOrder(CancelOrderRequest request) {
        log.error("begin - OrderCoreServiceImpl.cancelOrder request :" + request);
        CancelOrderResponse response = new CancelOrderResponse();
        Date date = new Date();
        try {
            request.requestCheck();

            //更新订单表
            Example example = new Example(Order.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("orderId", request.getOrderId());

            Order order = new Order();
            order.setStatus(OrderConstants.ORDER_STATUS_TRANSACTION_CLOSE);
            order.setUpdateTime(date);
            order.setCloseTime(date);
            int row = orderMapper.updateByExampleSelective(order, example);

            //更新order_item表
            //库存锁定状态 1库存已锁定 2库存已释放 3-库存减扣成功
            List<OrderItem> orderItems = orderItemMapper.queryByOrderId(request.getOrderId());
            //保存orderItem表里的itemId和num字段用于修改库存
            ArrayList<StockUpdate> stockUpdates = new ArrayList<>();
            for (OrderItem orderItem : orderItems) {
                orderItemMapper.updateStockStatus(2, orderItem.getOrderId());

                //ArrayList<StockUpdate>保存数量和itemId用于修改库存
                StockUpdate stockUpdate = new StockUpdate(orderItem.getItemId(), orderItem.getNum());
                stockUpdates.add(stockUpdate);
            }

            //更新stock表
            for (StockUpdate stockUpdate : stockUpdates) {
                //新写的库存更新逻辑
                stockMapper.updateStock2(stockUpdate.getNum(),stockUpdate.getItemId());
            }

            if (row > 0) {
                response.setCode(SysRetCodeConstants.SUCCESS.getCode());
                response.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
            } else {
                response.setCode(SysRetCodeConstants.DATA_NOT_EXIST.getCode());
                response.setMsg(SysRetCodeConstants.DATA_NOT_EXIST.getMessage());
            }
            log.info("OrderCoreServiceImpl.cancelOrder effect row :" + row);
        } catch (Exception e) {
            log.error("OrderCoreServiceImpl.cancelOrder occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(response, e);
        }


        return response;
    }

	/**
	 * 获得订单列表
	 * @param orderListRequest
	 * @return
	 */
	@Override
	public OrderListResponse getOrderList(OrderListRequest orderListRequest) {
		OrderListResponse orderListResponse = new OrderListResponse();

		try {
			PageHelper.startPage(orderListRequest.getPage(),orderListRequest.getSize());
			Example example = new Example(Order.class);
			example.setOrderByClause("update_Time" + " "+ "desc");
			Example.Criteria criteria = example.createCriteria();
			criteria.andEqualTo("userId",orderListRequest.getUserId());
            //使用多表查询
			//List<OrderDetailInfo> detailInfoList = orderMapper.getOrderList(orderListRequest.getUserId());
            //使用多个单表查询
			List<OrderDetailInfo> detailInfoList = new ArrayList<>();
			List<Order> orders = orderMapper.selectByExample(example);//查出所有订单
			for (Order order : orders) {
				OrderDetailInfo orderDetailInfo = orderConverter.order2detail(order);
				Example example1 = new Example(OrderItem.class);
				example1.createCriteria().andEqualTo("orderId",order.getOrderId());
				List<OrderItem> orderItems = orderItemMapper.selectByExample(example1);//获得订单对应的商品信息
				OrderShipping orderShipping = orderShippingMapper.selectByPrimaryKey(order.getOrderId());//获得订单对应的收货信息

				List<OrderItemDto> orderItemDtos = orderConverter.item2dto(orderItems);
				OrderShippingDto orderShippingDto = orderConverter.shipping2dto(orderShipping);
				orderDetailInfo.setOrderItemDto(orderItemDtos);
				orderDetailInfo.setOrderShippingDto(orderShippingDto);
				detailInfoList.add(orderDetailInfo);
			}
			PageInfo<Order> pageInfo = new PageInfo<>(orders);
			long total = pageInfo.getTotal();

			orderListResponse.setDetailInfoList(detailInfoList);
			orderListResponse.setTotal(total);
			orderListResponse.setCode(OrderRetCode.SUCCESS.getCode());
			orderListResponse.setMsg(OrderRetCode.SUCCESS.getMessage());
		} catch (Exception e) {
			log.error("OrderCoreServiceImpl.GetOrderList occur Exception :"+e);
			ExceptionProcessorUtils.wrapperHandlerException(orderListResponse,e);
		}


		return orderListResponse;
	}

	/**
	 * 获得订单信息详情
	 * @param request
	 * @return
	 */
    @Override
    public OrderDetailMyResponse getOrderDetail(OrderDetailRequest request) {
		OrderDetailMyResponse response = new OrderDetailMyResponse();
		//response = orderMapper.getOrderDetail(request.getOrderId());
		try {
			Example example = new Example(OrderItem.class);
			example.createCriteria().andEqualTo("orderId",request.getOrderId());
			List<OrderItem> orderItems = orderItemMapper.selectByExample(example);
			List<OrderItemDto> orderItemDtos = orderConverter.item2dto(orderItems);
			Order order = orderMapper.selectByPrimaryKey(request.getOrderId());
			OrderShipping orderShipping = orderShippingMapper.selectByPrimaryKey(request.getOrderId());
			response.setOrderItemDto(orderItemDtos);
			response.setOrderStatus(order.getStatus());
			response.setOrderTotal(order.getPayment());
			response.setStreetName(orderShipping.getReceiverAddress());
			response.setTel(orderShipping.getReceiverPhone());
			response.setUserId(order.getUserId());
			response.setUserName(orderShipping.getReceiverName());
			response.setCode(OrderRetCode.SUCCESS.getCode());
			response.setMsg(OrderRetCode.SUCCESS.getMessage());
		} catch (Exception e) {
			log.error("OrderCoreServiceImpl.GetOrderDetail occur Exception :"+e);
			ExceptionProcessorUtils.wrapperHandlerException(response,e);
		}

		return response;
    }

    /**
     * 修改订单状态为已支付
     *   扣减锁定库存(根据订单中的订单商品条目的数量)
     * @param orderId
     * @return
     */
    @Override
    public PayOrderSuccessResponse payOrderSuccess(String orderId) {
       // Order order = orderMapper.selectByPrimaryKey(orderId);
        PayOrderSuccessResponse response = new PayOrderSuccessResponse();
        try {
            Order order = new Order();
            order.setUpdateTime(new Date());
            order.setStatus(1);
            Example example = new Example(Order.class);
            example.createCriteria().andEqualTo("orderId",orderId);
            int i = orderMapper.updateByExampleSelective(order, example);//修改订单状态为已支付
            Example example1 = new Example(OrderItem.class);
            example1.createCriteria().andEqualTo("orderId",orderId);
            List<OrderItem> orderItems = orderItemMapper.selectByExample(example1);

            for (OrderItem orderItem : orderItems) {
                Long productId = orderItem.getItemId();
                Long productNum = orderItem.getNum().longValue();
                Stock stock = new Stock();
                stock.setItemId(productId);
                stock.setStockCount(0L);
                stock.setLockCount(-productNum.intValue());
                //stock.setStockCount(-productNum);
                stockMapper.updateStock(stock);//扣减锁定库存(根据订单中的订单商品条目的数量)
                System.out.println("after!!!");


            }
            response.setCode(OrderRetCode.SUCCESS.getCode());
            response.setMsg(OrderRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

}
