package org.squirrelnest.fairies.procedure;

import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.procedure.interfaces.DHTProcedure;
import org.squirrelnest.fairies.router.RouterTable;
import org.squirrelnest.fairies.service.RequestSendService;

/**
 * Created by Inoria on 2019/3/17.
 */
abstract class AbstractProcedure<T> implements DHTProcedure<T> {

    protected final HashCode160 targetId;
    protected final int k;
    protected final int requestTimeoutMs;
    protected final RouterTable routerTable;
    protected final RequestSendService requestSendService;
    protected final int alpha;

    protected T result = null;

    protected AbstractProcedure(HashCode160 targetId, int k, int alpha, int requestTimeoutMs,
                                RouterTable routerTable, RequestSendService sendService) {
        this.targetId = targetId;
        this.k = k;
        this.alpha = alpha;
        this.requestTimeoutMs = requestTimeoutMs;
        this.routerTable = routerTable;
        this.requestSendService = sendService;
    }

    @Override
    public T getResult() {
        return result;
    }

    @Override
    public abstract T execute();
}
