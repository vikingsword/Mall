package com.mall.order.biz.handler;/**
 * Created  on 2019/8/2.
 */

import com.mall.order.biz.TransOutboundInvoker;

/**
 *
 * create-date: 2019/8/2-下午9:58
 */
public interface TransPipeline extends TransOutboundInvoker {

    /**
     *
     * @param handlers
     */
    void addFirst(TransHandler... handlers);

    /**
     *
     * @param handlers
     */
    void addLast(TransHandler ... handlers);
}
