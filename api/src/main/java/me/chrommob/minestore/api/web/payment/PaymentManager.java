package me.chrommob.minestore.api.web.payment;

import me.chrommob.minestore.api.generic.ParamBuilder;
import me.chrommob.minestore.api.web.FeatureManager;
import me.chrommob.minestore.api.web.Result;
import me.chrommob.minestore.api.web.WebApiRequest;
import me.chrommob.minestore.api.web.Wrapper;

import java.util.function.Function;

public class PaymentManager extends FeatureManager {
    public PaymentManager(Wrapper<Function<WebApiRequest<?>, Result<?, ? extends Exception>>> requestHandler) {
        super(requestHandler);
    }

    public boolean markPaymentAsPaid(String paymentId, boolean executeCommands, String note) {
        ParamBuilder paramBuilder = new ParamBuilder();
        paramBuilder.append("execute_commands", String.valueOf(executeCommands));
        paramBuilder.append("note", note);
        Result<Void, Exception> result = request(new WebApiRequest<>("payment/markAsPaid/" + paymentId, WebApiRequest.Type.POST, paramBuilder, Void.class, true));
        return result.error() == null;
    }
}
