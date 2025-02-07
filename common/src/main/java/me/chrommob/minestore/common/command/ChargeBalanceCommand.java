package me.chrommob.minestore.common.command;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.generic.ParamBuilder;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public class ChargeBalanceCommand {

    private final Gson gson = new Gson();
    private final URL url;
    private final MineStoreCommon plugin;
    public ChargeBalanceCommand(MineStoreCommon plugin) {
        this.plugin = plugin;

        String storeUrl = plugin.pluginConfig().getKey("store-url").getAsString();
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        storeUrl = storeUrl + "/api/payments/handle/";
        if (plugin.pluginConfig().getKey("api").getKey("key-enabled").getAsBoolean()) {
            storeUrl += plugin.pluginConfig().getKey("api").getKey("key").getAsString() + "/virtualcurrency";
        } else {
            storeUrl += "virtualcurrency";
        }
        URL tempUrl = null;
        try {
            tempUrl = new URL(storeUrl);
        } catch (Exception e) {
            plugin.log("ERROR SETTING UP INTERNAL PAYMENT HANDLER!");
        }
        url = tempUrl;
    }

    @Permission("minestore.admin.chargeBalance")
    @Command("minestore|ms chargeBalance <username> <amount> <payment_internal_id> <signature>")
    public void onCharge(AbstractUser user, @Argument("username") String username, @Argument("amount") String amount, @Argument("payment_internal_id") String paymentInternalId, @Argument("signature") String signature) {
        String secretKey = plugin.pluginConfig().getKey("api").getKey("key").getAsString();
        String generatedSignature = getSignature(amount, paymentInternalId, username, secretKey);
        boolean verifySignature = generatedSignature.equals(signature);
        if (!verifySignature) {
            plugin.log("Failed to verify signature: " + generatedSignature + " != " + signature);
        }

        ResponseData responseData = getResponseData(username, amount, paymentInternalId, generatedSignature);
        plugin.paymentHandler().handlePayment(responseData);

        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
            String json = gson.toJson(responseData);
            plugin.debug(this.getClass(), "Sending payment: " + json);
            writer.write(json);
            writer.flush();
            writer.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder responseString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseString.append(line);
            }

            plugin.debug(this.getClass(), "Received: " + responseString);
            urlConnection.disconnect();

            Received received = gson.fromJson(responseString.toString(), Received.class);

            if (!received.status) {
                plugin.log("Failed to charge balance!");
                plugin.log(received.message);
            }

        } catch (IOException | JsonSyntaxException e) {
            plugin.debug(this.getClass(), e);
        }
    }

    private ResponseData getResponseData(String username, String amount, String paymentInternalId, String signature) {
        ResponseData responseData = new ResponseData();
        responseData.data = new ResponseData.Data();
        responseData.data.username = username;
        responseData.data.price = Double.parseDouble(amount);
        responseData.data.payment_internal_id = paymentInternalId;
        responseData.data.signature = signature;
        CommonUser user = Registries.USER_GETTER.get().get(username);

        boolean hasEnoughMoney = user.takeMoney(Double.parseDouble(amount));
        if (!hasEnoughMoney) {
            responseData.status = "failure";
            responseData.data.remaining_balance = user.getBalance();
            return responseData;
        }
        responseData.data.remaining_balance = user.getBalance();
        responseData.status = "success";
        return responseData;
    }

    public String getSignature(String price, String paymentInternalId, String username, String secretKey) {
        ParamBuilder paramBuilder = new ParamBuilder();
        paramBuilder.append("payment_internal_id", paymentInternalId);
        paramBuilder.append("price", price);
        paramBuilder.append("username", username);
        String text = paramBuilder.build().substring(1);

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] hmacBytes = mac.doFinal(text.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            plugin.debug(this.getClass(), e);
        }
        return null;
    }

    public static class ResponseData {
        public String status;
        public Data data;

        public static class Data {
            public String username;
            public double price;
            public String payment_internal_id;
            public double remaining_balance;
            public String signature;
        }
    }

    static class Received {
        @SerializedName("success")
        public boolean status;
        public String message;
    }
}
