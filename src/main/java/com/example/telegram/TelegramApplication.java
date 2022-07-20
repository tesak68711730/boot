package com.example.telegram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class TelegramApplication {

    public static void main(String[] args) {

        try {
            MyTelegramBot myTelegramBot = new MyTelegramBot(new DefaultBotOptions()); // botOptions
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(myTelegramBot);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SpringApplication.run(TelegramApplication.class, args);

//        DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
//        botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
//        botOptions.setProxyHost("localhost");
//        botOptions.setProxyPort(9050);

//        System.getProperties().put("proxySet", true);
//        System.getProperties().put("socksProxyHost", "127.0.0.1");
//        System.getProperties().put("socksProxyPort", "9050");


    }

}
