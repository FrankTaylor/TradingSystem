package com.huboyi.trader.service.impl;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.huboyi.trader.entity.po.FundsFlowPO;
import com.huboyi.trader.repository.FundsFlowRepository;
import com.huboyi.trader.service.FundsFlowService;

/**
 * 资金流水Service实现类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.1
 */
@Service("fundsFlowService")
public class FundsFlowServiceImpl implements FundsFlowService {
	
	/** 日志。*/
	private static final Logger log = LogManager.getLogger(FundsFlowServiceImpl.class);
	
	/** 日期格式处理类。*/
	private final DateFormat dataFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS");
	
	@Autowired
	@Qualifier("fundsFlowRepository")
	private FundsFlowRepository fundsFlowRepository;
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.REPEATABLE_READ, readOnly=false, rollbackForClassName="{*Exception}")
	@Override
	public void transferInto(Long tradeDate, BigDecimal tradeMoney, String stockholder) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用  [转入资金] 方法").append("\n");
		logMsg.append("@param [tradeDate = " + tradeDate + "]\n");
		logMsg.append("@param [tradeMoney = " + tradeMoney + "]\n");
		logMsg.append("@param [stockholder = " + stockholder + "]\n");
		log.info(logMsg.toString());
		
		// --- 参数完整性验证 ---
		if (tradeDate == null) {
			throw new RuntimeException(
					"转入日期不能为空！ " +
					"[转入日期 = " + tradeDate + "]");
		}
		
		if (tradeMoney == null) {
			throw new RuntimeException(
					"转入金额不能为空！ " +
					"[转入金额= " + tradeMoney + "]");
		}
		
		if (StringUtils.isBlank(stockholder)) {
			throw new RuntimeException(
					"股东代码不能为空！ " +
					"[股东代码= " + stockholder + "]");
		}
		
		// --- 业务逻辑正确性验证 ---
		try {
			dataFormat.format(new Date(tradeDate));
		} catch(Exception e) {
			throw new RuntimeException(
					"转入日期格式有误！ " +
					"[转入日期 = " + tradeDate + "]");
		}
		
		if (tradeMoney.doubleValue() < 0) {
			throw new RuntimeException(
					"转入金额不能为负数！ " +
					"[转入金额 = " + tradeMoney + "]");
	    }
		
		FundsFlowPO lastPo = fundsFlowRepository.findLastOne(stockholder);     // 查询出最后一条资金流水记录。
		if (lastPo != null && tradeDate < lastPo.getTradeDate()) {
			throw new RuntimeException(
					"新插入资金流水的交易日期不能小于最后一条资金流水的交易日期！" +
					"[新插入资金流水的交易日期 = " + tradeDate + "] | " +
					"[最后一条资金流水的交易日期 = " + lastPo.getTradeDate() + "]");
    	}
		
		// 1、构造初步的资金流入记录。
		FundsFlowPO savePo = new FundsFlowPO();
		savePo.setContractCode(UUID.randomUUID().toString());                  // 合同编号。
		savePo.setCurrency("人民币");                                            // 币种。
		savePo.setTradeDate(tradeDate);                                        // 成交日期（格式：yyyyMMddhhmmssSSS）。
		savePo.setTradeMoney(tradeMoney);                                      // 成交金额。
		savePo.setFundsBalance(tradeMoney);                                    // 资金余额。
		savePo.setBusinessType(FundsFlowPO.Business.ROLL_IN.getType());        // 业务类型（在数据库中实际记录的值，主要用于查询）。
		savePo.setStockholder(stockholder);                                    // 股东代码。
		
		// 2、累加资金余额。
		if (lastPo != null && lastPo.getFundsBalance() != null) {
			savePo.setFundsBalance(
					lastPo.getFundsBalance().add(savePo.getFundsBalance()));   // 累加资金余额。
		}
		
		fundsFlowRepository.insert(savePo);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.REPEATABLE_READ, readOnly=false, rollbackForClassName="{*Exception}")
	@Override
	public void deleteAllRecords() {
		fundsFlowRepository.truncate();
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.REPEATABLE_READ, readOnly=false, rollbackForClassName="{*Exception}")
	@Override
	public void deleteRecords(String stockholder) {
		fundsFlowRepository.delete(stockholder);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.REPEATABLE_READ, readOnly=true, rollbackForClassName="{*Exception}")
	@Override
	public FundsFlowPO findNewRecord(String stockholder) {
		return fundsFlowRepository.findLastOne(stockholder);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.REPEATABLE_READ, readOnly=true, rollbackForClassName="{*Exception}")
	@Override
	public List<FundsFlowPO> findRecords(String stockholder) {
		return fundsFlowRepository.findAll(stockholder);
	}
	
}