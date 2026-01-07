package me.chrommob.minestore.common.subsription;

import me.chrommob.minestore.api.web.Result;
import me.chrommob.minestore.api.web.WebApiAccessor;
import me.chrommob.minestore.api.web.WebContext;
import me.chrommob.minestore.api.web.WebRequest;
import me.chrommob.minestore.common.subsription.json.ReturnSubscriptionObject;

public class SubscriptionUtil {
    public static ReturnSubscriptionObject getSubscription(String username) {
        String body = "username=" + username;
        WebRequest<ReturnSubscriptionObject> request = new WebRequest.Builder<>(ReturnSubscriptionObject.class).path("in-game/manageSubscriptions/").requiresApiKey(true).type(WebRequest.Type.POST).body(body.getBytes()).build();
        Result<ReturnSubscriptionObject, WebContext> res = WebApiAccessor.request(request);
        if (res.isError()) {
            return null;
        }
        return res.value();
    }
}
