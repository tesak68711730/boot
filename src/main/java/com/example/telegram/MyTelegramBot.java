package com.example.telegram;

import com.example.telegram.enums.Currency;
import com.example.telegram.service.CurrencyConversionService;
import com.example.telegram.service.CurrencyModeService;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MyTelegramBot extends TelegramLongPollingBot {

    private static final String TOKEN = "5300383978:AAFLoAzuNFtIvWWT0hn9kmrYaD0uM4CyddQ";
    private static final String USERNAME = "@uassea_xxx_bot";

    private final CurrencyModeService currencyModeService = CurrencyModeService.getInstance();
    private final CurrencyConversionService currencyConversionService = CurrencyConversionService.getInstance();

    protected MyTelegramBot(DefaultBotOptions options) {
        super(options);
    }

    @Override
    public String getBotUsername() {
        return USERNAME;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }

        if (update.hasMessage()) {
            handleMessage(update.getMessage());
            /*Long chatID = update.getMessage().getChatId();

            try {
                execute(new SendMessage(String.valueOf(chatID), "Hi " + update.getMessage().getText()));
            } catch (TelegramApiException t) {
                t.printStackTrace();
            }*/
        }
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        Message message = callbackQuery.getMessage();
        String[] params = callbackQuery.getData().split(":");

        String action = params[0].replace(" ", "");
        Currency newCurrency = Currency.valueOf(params[1].replace(" ", ""));

        switch (action) {
            case "ORIGINAL":
                currencyModeService.setOriginalCurrency(message.getChatId(), newCurrency);
                break;
            case "TARGET":
                currencyModeService.setTargetCurrency(message.getChatId(), newCurrency);
                break;
        }
        try {
            List<List<InlineKeyboardButton>> buttons = generateButtons(message);
                execute(EditMessageReplyMarkup.builder()
                    .chatId(message.getChatId())
                    .messageId(message.getMessageId())
                    .replyMarkup(InlineKeyboardMarkup.builder()
                            .keyboard(buttons)
                            .build())
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleMessage(Message message) {
        try {
            if (message.hasText() && message.hasEntities()) {
                Optional<MessageEntity> bot_command = message.getEntities().stream().filter(entity -> entity.getType().equals("bot_command")).findFirst();
                if (bot_command.isPresent()) {
                    String command = message.getText().substring(bot_command.get().getOffset(), bot_command.get().getLength());
                    switch (command) {
                        case "/set_currency":
                            List<List<InlineKeyboardButton>> buttons = generateButtons(message);

                            execute(SendMessage.builder()
                                    .chatId(message.getChatId())
                                    .text("Please choose Original and Target Currency")
                                    .replyMarkup(InlineKeyboardMarkup.builder()
                                            .keyboard(buttons)
                                            .build())
                                    .build());
                            return;
                        default:
                            throw new IllegalStateException("Unexpected value: " + command);
                    }
                }
            }
            if (message.hasText()) {
                String messageText = message.getText();
                Optional<Double> value = parseDouble(messageText);
                Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
                Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());

                double conversionRatio = currencyConversionService.getConversionRatio(originalCurrency, targetCurrency);
                if (value.isPresent()) {
                    execute(SendMessage.builder()
                            .chatId(message.getChatId())
                            .text(String.format("%4.2f %s %4.2f %s",
                                    value.get(), originalCurrency,
                                    (value.get() * conversionRatio), targetCurrency))
                            .build());
                }
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<Double> parseDouble(String messageText) {
        try {
            return Optional.of(Double.parseDouble(messageText));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private List<List<InlineKeyboardButton>> generateButtons(Message message) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
        Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());
        for (Currency currency : Currency.values()) {
            buttons.add(Arrays.asList(
                    InlineKeyboardButton.builder()
                            .text(getCurrencyButton(originalCurrency, currency))
                            .callbackData("ORIGINAL : " + currency)
                            .build(),
                    InlineKeyboardButton.builder()
                            .text(getCurrencyButton(targetCurrency, currency))
                            .callbackData("TARGET : " + currency)
                            .build()
            ));
        }
        return buttons;
    }

    private String getCurrencyButton(Currency saved, Currency current) {
        return saved == current ? current + " ✅" : current.name();
    }

    /*@Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }*/
}
