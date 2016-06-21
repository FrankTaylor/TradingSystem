//package com.huboyi.trader.service.impl;
//
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//
//import com.huboyi.trader.entity.po.OrderInfoPO;
//import com.huboyi.trader.repository.OrderInfoRepository;
//import com.huboyi.trader.service.OrderInfoService;
//
///**
// * 订单信息Service实现类。
// * 
// * @author FrankTaylor <mailto:franktaylor@163.com>
// * @since 1.1
// */
//public class OrderInfoServiceImpl implements OrderInfoService {
//
//	@Autowired
//	@Qualifier("orderInfoRepository")
//	private OrderInfoRepository orderInfoRepository;
//	
//	@Override
//	public void deleteAllRecords() {
//		orderInfoRepository.truncate();
//	}
//
//	@Override
//	public void deleteRecords(String stockholder) {
//		orderInfoRepository.delete(stockholder);
//	}
//
//	@Override
//	public List<OrderInfoPO> findRecords(String stockholder) {
//		return orderInfoRepository.findAll(stockholder);
//	}
//	
//}