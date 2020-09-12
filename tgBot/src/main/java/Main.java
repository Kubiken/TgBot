import DBControllers.DbController;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class Main
{
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        Bot instance = new Bot();

        try {
            telegramBotsApi.registerBot(instance);
            System.out.println("Bot started succesfully");
            instance.refreshUserList();
            System.out.println("Userlist refreshed succesfully");

        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
}
