package me.chrommob.minestore.api.web.payment;

import me.chrommob.minestore.api.generic.ParamBuilder;
import me.chrommob.minestore.api.web.*;

import java.util.function.Function;

public class PaymentManager extends FeatureManager {
    public PaymentManager(Wrapper<Function<WebRequest<?>, Result<?, WebContext>>> requestHandler) {
        super(requestHandler);
    }

    public boolean markPaymentAsPaid(String paymentId, boolean executeCommands, String note) {
        ParamBuilder paramBuilder = new ParamBuilder();
        paramBuilder.append("execute_commands", String.valueOf(executeCommands));
        paramBuilder.append("note", note);
        Result<Void, WebContext> result = request(new WebRequest.Builder<>(Void.class).path("payment/markAsPaid/" + paymentId).requiresApiKey(true).type(WebRequest.Type.POST).paramBuilder(paramBuilder).build());
        return !result.isError();
    }
}
