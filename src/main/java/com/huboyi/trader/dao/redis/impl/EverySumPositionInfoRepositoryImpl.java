//package com.huboyi.position.dao.redis.impl;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//import java.util.UUID;
//
//import org.apache.log4j.Logger;
//import org.springframework.dao.DataAccessException;
//import org.springframework.data.redis.core.RedisOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.SessionCallback;
//
//import com.huboyi.position.dao.EverySumPositionInfoRepository;
//import com.huboyi.position.po.EverySumPositionInfoPO;
//
///**
// * 每一笔持仓信息DAO实现类 。
// * 
// * @author FrankTaylor <mailto:franktaylor@163.com>
// * @since 1.0
// */
//public class EverySumPositionInfoRepositoryImpl extends RedisTemplate<String, EverySumPositionInfoPO> implements EverySumPositionInfoRepository {
//	
//	/** 日志。*/
//	private final Logger log = Logger.getLogger(EverySumPositionInfoRepositoryImpl.class);
//
//	@Override
//	@Deprecated
//	public void createIndex(String stockCode) {
//		log.warn("当前数据库是Redis不用创建索引。");
//	}
//	
//	@Override
//	public void insert(EverySumPositionInfoPO po) {
//		StringBuilder logMsg = new StringBuilder();
//		logMsg.append("invoke insert method").append("\n");
//		logMsg.append("@param [po = " + po + "]");
//		log.info(logMsg.toString());
//		
//		try {
//
//			// --- 把记录保存到Redis。
//			po.setId(UUID.randomUUID().toString() + "-" + getClass().getName());
//			opsForList().rightPush(getListName(po.getStockCode()), po);
//			
//			log.info("插入 [证券代码 = " + po.getStockCode() + "] 的每一笔持仓记录成功。");
//		} catch (Throwable e) {
//			String errorMsg = "插入 [证券代码 = " + po.getStockCode() + "] 的每一笔持仓记录失败!";
//			log.error(errorMsg, e);
//			throw new RuntimeException(errorMsg, e);
//		}
//		
//	}
//	
//	@Override
//	public EverySumPositionInfoPO findNewOne(String stockCode) {
//		StringBuilder logMsg = new StringBuilder();
//		logMsg.append("invoke findNewOne method").append("\n");
//		logMsg.append("@param [stockCode = " + stockCode + "]");
//		log.info(logMsg.toString());
//		
//		try {
//			
//			List<EverySumPositionInfoPO> poList = findEverySumPositionInfoList(stockCode, null, null, null, null, null, null);
//			if (poList == null || poList.isEmpty()) {
//				return null;
//			}
//			
//			log.info("查询  [证券代码 = " + stockCode + "] 最近的每一笔持仓记录成功。");
//			return poList.get(poList.size() - 1);
//		} catch (Throwable e) {
//			String errorMsg = "查询 [证券代码 = " + stockCode + "] 最近的每一笔持仓记录失败!";
//			log.error(errorMsg, e);
//			throw new RuntimeException(errorMsg, e);
//		}
//	}
//	
//	@Override
//	public List<EverySumPositionInfoPO> findEverySumPositionInfoList(String stockCode, String openContractCode, Integer beginOpenDate, Integer endOpenDate, String isClose, Integer beginPage, Integer endPage) {
//		StringBuilder logMsg = new StringBuilder();
//		logMsg.append("invoke findEverySumPositionInfoList method").append("\n");
//		logMsg.append("@param [stockCode = " + stockCode + "]").append("\n");
//		logMsg.append("@param [openContractCode = " + openContractCode + "]").append("\n");
//		logMsg.append("@param [beginOpenDate = " + beginOpenDate + "]").append("\n");
//		logMsg.append("@param [endOpenDate = " + endOpenDate + "]").append("\n");
//		logMsg.append("@param [isClose = " + isClose + "]").append("\n");
//		logMsg.append("@param [beginPage = " + beginPage + "]").append("\n");
//		logMsg.append("@param [endPage = " + endPage + "]").append("\n");
//		log.info(logMsg.toString());
//		
//		try {
//
//			// --- 查询Redis。
//			List<EverySumPositionInfoPO> poList = opsForList().range(getListName(stockCode), 0, -1);
//			
//			// --- 由于Redis没有其他数据库中的排序功能，这里需要自己实现按照open_date升序。
//			Collections.sort(poList, new Comparator<EverySumPositionInfoPO>() {
//				@Override
//				public int compare(EverySumPositionInfoPO o1, EverySumPositionInfoPO o2) {
//					return (o1.getOpenDate() > o2.getOpenDate()) ? 1    :
//						   (o1.getOpenDate() < o2.getOpenDate()) ? -1   :
//				           0;
//				}
//			});
//			
//			// --- 如果有建仓合同编号，就直接从原集合中找出。
//			if (openContractCode != null) {
//				for (int i = 0; i < poList.size(); i++) {
//					if (poList.get(i).getOpenContractCode().equals(openContractCode)) {
//						List<EverySumPositionInfoPO> list = new ArrayList<EverySumPositionInfoPO>();
//						list.add(poList.get(i));
//						return list;
//					}
//				}
//			}
//			
//			// --- 把符合时间范围的记录载入集合。
//			List<EverySumPositionInfoPO> resultList = new ArrayList<EverySumPositionInfoPO>();
//			for (EverySumPositionInfoPO po : poList) {
//				
//				if (beginOpenDate != null && po.getOpenDate() < beginOpenDate) {
//					continue;
//				}
//				if (endOpenDate != null && po.getOpenDate() > endOpenDate) {
//					continue;
//				}
//				
//				resultList.add(po);
//			}
//			
//			// --- 把不符合是否已经平仓的记录从结果集合中删除。
//			if (isClose != null) {
//				for (int i = 0; i < resultList.size(); i++) {
//					EverySumPositionInfoPO po = resultList.get(i);
//					if (isClose.equals("0") && !po.getCloseContractCode().equals("no")) {
//						resultList.remove(i);
//						i--;
//					}
//					
//					if (isClose.equals("1") && po.getCloseContractCode().equals("no")) {
//						resultList.remove(i);
//						i--;
//					}
//				}
//			}
//			
//			// --- 对集合进行截取范围操作。
//			int fromIndex = (beginPage != null && beginPage > 0 && resultList.size() > beginPage) ? beginPage : 0;
//			int toIndex = (endPage != null && endPage > 0 && (resultList.size() - fromIndex) >= endPage) ? (endPage + fromIndex) : resultList.size();
//
//			log.info("按照条件查询 [证券代码 = " + stockCode + "] 的每一笔持仓记录成功。");
//			return resultList.subList(fromIndex, toIndex);
//		} catch (Throwable e) {
//			String errorMsg = "按照条件查询 [证券代码 = " + stockCode + "] 的每一笔持仓记录失败!";
//			log.error(errorMsg, e);
//			throw new RuntimeException(errorMsg, e);
//		}
//	}
//
//	@Override
//	public void update(final EverySumPositionInfoPO po) {
//		StringBuilder logMsg = new StringBuilder();
//		logMsg.append("invoke update method").append("\n");
//		logMsg.append("@param [po = " + po + "]");
//		log.info(logMsg.toString());
//		
//		try {
//			
//			execute(new SessionCallback<String>() {
//				
//				@SuppressWarnings({ "rawtypes", "unchecked" })
//				@Override
//				public String execute(RedisOperations operation) throws DataAccessException {
//					
//					// --- 查询Redis。
//					List<EverySumPositionInfoPO> poList = operation.opsForList().range(getListName(po.getStockCode()), 0, -1);
//					
//					// --- 从原始集合中找出将要修改的记录和其在Redis集合中的索引。
//					int indexOfReidsList = 0;
//					EverySumPositionInfoPO updatePo = null;
//					for (; indexOfReidsList < poList.size(); indexOfReidsList++) {
//						if (poList.get(indexOfReidsList).getId().equals(po.getId())) {
//							updatePo = poList.get(indexOfReidsList);
//							break;
//						}
//					}
//					
//					// --- 修改Redis集合中的记录。
//					if (po.getCanCloseNumber() != null) { updatePo.setCanCloseNumber(po.getCanCloseNumber()); }             // 可平仓数量。
//					if (po.getStopPrice() != null) { updatePo.setStopPrice(po.getStopPrice()); }                            // 止损价格。
//					if (po.getCloseContractCode() != null) { updatePo.setCloseContractCode(po.getCloseContractCode()); }    // 平仓合同编号。
//					if (po.getSystemClosePoint() != null) { updatePo.setSystemClosePoint(po.getSystemClosePoint()); }       // 系统平仓点。
//					if (po.getCloseSignalDate() != null) { updatePo.setCloseSignalDate(po.getCloseSignalDate()); }          // 平仓信号发出时间（格式：yyyyMMddhhmmssSSS）。
//					if (po.getCloseDate() != null) { updatePo.setCloseDate(po.getCloseDate()); }                            // 平仓日期（格式：yyyyMMddhhmmssSSS）。
//					if (po.getClosePrice() != null) { updatePo.setClosePrice(po.getClosePrice()); }                         // 平仓价格。
//					if (po.getCloseNumber() != null) { updatePo.setCloseNumber(po.getCloseNumber()); }                      // 平仓数量。
//					if (po.getNewPrice() != null) {updatePo.setNewPrice(po.getNewPrice()); }                                // 当前价。
//					if (po.getNewMarketValue() != null) { updatePo.setNewMarketValue(po.getNewMarketValue()); }             // 最新市值。
//					if (po.getFloatProfitAndLoss() != null) {updatePo.setFloatProfitAndLoss(po.getFloatProfitAndLoss()); }  // 浮动盈亏。
//					if (po.getProfitAndLossRatio() != null) { updatePo.setProfitAndLossRatio(po.getProfitAndLossRatio()); } // 盈亏比例。
//					
//					operation.watch(getListName(po.getStockCode()));
//					operation.multi();
//					
//					opsForList().set(getListName(po.getStockCode()), indexOfReidsList, updatePo);
//					
//					operation.exec();
//					return null;
//				}
//				
//			});
//
//			log.info("修改 [证券代码 = " + po.getStockCode() + "] 的每一笔持仓记录成功。");
//		} catch (Throwable e) {
//			String errorMsg = "修改 [证券代码 = " + po.getStockCode() + "] 的每一笔持仓记录失败!";
//			log.error(errorMsg, e);
//			throw new RuntimeException(errorMsg, e);
//		}
//	}
//	
//	@Override
//	public void dropCollection(String stockCode) {
//		StringBuilder logMsg = new StringBuilder();
//		logMsg.append("invoke dropCollection method").append("\n");
//		logMsg.append("@param [stockCode = " + stockCode + "]");
//		log.info(logMsg.toString());
//		
//		try {
//			// --- 把记录从Redis中删除。
//			delete(getListName(stockCode));
//			log.info("删除  [证券代码 = " + stockCode + "] 用于每一笔持仓记录的集合成功。");
//		} catch (Throwable e) {
//			String errorMsg = "删除  [证券代码 = " + stockCode + "] 用于每一笔持仓记录的集合失败!";
//			log.error(errorMsg, e);
//			throw new RuntimeException(errorMsg, e);
//		}
//	}
//	
//	/**
//	 * 得到集合名称。
//	 * 
//	 * @param stockCode 证券代码
//	 * @return String
//	 */
//	private String getListName(String stockCode) {
//		return "everySumPositionInfo" + ":" + stockCode;
//	}
//}