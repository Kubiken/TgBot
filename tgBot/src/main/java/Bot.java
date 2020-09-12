import DBControllers.DbController;
import Models.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Bot extends TelegramLongPollingBot
{
    private String myChatId = "-1001263955368";
    //-1001263955368
    private String adminId ="313754499";
    //313754499


    private String _help = "Справка по командам:\n" +
            "/send - Отправка своего плана к следующему митапу. Также, полностью перезаписывает план если вы его уже отправили.\n" +
            "/update - Дописывает ваше сообщение в конец вашего отправленного плана. Если хотите изменить план полностью, используйте /send\n" +
            "/history - Вам стало интересно что вы уже успели? Тогда отправьте эту комманду, и я верну вам все ваши прошлые планы\n" +
            "/unreg - Удалит вас из базы данных бота. Пожалуйста, используйте эту комманду, только тогда, когда покинете группу ММ";
    private Boolean botState = false;

    private String sadEmoji = "\uD83D\uDE13";
    private  ArrayList<User> userlist;
    /**
     * Метод для приема сообщений.
     * @param update Содержит сообщение от пользователя.
     */
    @Override
    public void onUpdateReceived(Update update) {
        Boolean isInDb = false;
        String message = update.getMessage().getText();
        //Получаем ник
       // update.getMessage().getFrom().getUserName();

        if (message.equals("/start")) {

            try {
                isInDb = DbController.isUserInDb(update.getMessage().getChatId().toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if(!isInDb)
            {
            try {
                DbController.addUserToDb(update.getMessage().getChatId().toString(),
                        update.getMessage().getFrom().getUserName());
                User u = new User();
                u.setReady(false);
                u.setUsername(update.getMessage().getFrom().getUserName());
                u.setUserTgId(update.getMessage().getChatId().toString());
                userlist.add(u);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            }
            sendMsg(update.getMessage().getChatId().toString(), "Планобот приветствует вас! " +
                    "Список доступных комманд можете посмотреть командой /help");
        }
        if (message.equals("/unreg"))
        {
            try {
                DbController.unregUser(update.getMessage().getChatId().toString());
                sendMsg(update.getMessage().getChatId().toString(), "Нам будет вас нехватать "+sadEmoji);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(message.startsWith("/send") && botState==true)
        {
            System.out.println("Start send method");
            String plan = message.replace("/send","");
            for(User u: userlist)
            {
                String uidList = u.getUserTgId().trim();
                System.out.println(uidList+"#");
                String uidDb = update.getMessage().getFrom().getId().toString();
                System.out.println(uidDb+"#");
                if(uidDb.equals(uidList))
                {
                    if(u.getReady()==false)
                    {
                        System.out.println("Not sended yet");
                        sendMsg(update.getMessage().getChatId().toString(), "Я добавил ваш план в базу," +
                                " теперь можете откинуться на спинку кресла и немного отдохнуть. Если захотите что то поменять," +
                                " всегда можно обновить план коммандой /update!");
                        sendMsg(myChatId, update.getMessage().getFrom().getUserName()+
                                " добавил свой план: "+plan);

                        try {
                            System.out.println("trying initiate send");
                            DbController.insertPlan(update.getMessage().getFrom().getId().toString(),plan);
                            System.out.println("initiate send complete");
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        u.setReady(true);
                        break;
                    }
                    else
                        {
                            System.out.println("already sended");
                            sendMsg(update.getMessage().getChatId().toString(), "Я перезаписал ваш план в базу.");
                            sendMsg(myChatId, update.getMessage().getFrom().getUserName()+
                                    " обновил свой план: "+plan);

                            try {
                                System.out.println("trying rewrite");
                                DbController.rewritePlan(update.getMessage().getFrom().getId().toString(),plan);
                                System.out.println("rewrite complete");
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                }
                System.out.println("user not found");
            }
        }
        else if(message.startsWith("/send"))
        {sendMsg(update.getMessage().getChatId().toString(), "Еще рано, подожди пока начнется сбор планов.");
           }

        if(message.startsWith("/update") && botState==true)
        {
            for(User u: userlist)
            {
                String uidList = u.getUserTgId().trim();
                String uidDb = update.getMessage().getFrom().getId().toString();
                if(uidDb.equals(uidList)) {
                    if(u.getReady()==true)
                    {
                        String plan = message.replace("/update", "");

                        sendMsg(update.getMessage().getChatId().toString(), "Ваш план обновлен!");
                        sendMsg(myChatId, update.getMessage().getFrom().getUserName() +
                               " обновил свой план: " + plan);

                        try {
                            DbController.updatePlan(update.getMessage().getFrom().getId().toString(), plan);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                        {
                            sendMsg(update.getMessage().getChatId().toString(), "Вы еще не добавляли план на этой неделе!");
                        }
                }
            }

        }
        else if(message.startsWith("/update"))
        {sendMsg(update.getMessage().getChatId().toString(), "Еще рано, подожди пока начнется сбор планов.");}

        if(message.equals("/history"))
        {
            String history = "";
            try {
                history = DbController.selectHistory(update.getMessage().getFrom().getId().toString());
                if(history.equals(""))history = "Вы не загружали еще ни одного плана\n";
            } catch (SQLException e) {
                e.printStackTrace();
            }
            sendMsg(update.getMessage().getChatId().toString(), "All your plans belong to you:\n"+
                    history);
        }
        else if(message.equals("/help"))
        { sendMsg(update.getMessage().getChatId().toString(), _help);}

        if(update.getMessage().getChatId().toString().equals(adminId))
        {
            if(message.startsWith("/start_plan_collecting"))
            {
                if(botState==false)
                {
                    startPlanCollecting();
                    sendMsg(myChatId, "Пора отсылать свои планы!");
                    sendMsg(adminId, "Сбор запущен!");
                }else {sendMsg(adminId, "Сбор уже запущен");}
            }
            else if(message.startsWith("/stop_plan_collecting"))
            {
                if(botState==true)
                {
                    stopPlanCollecting();
                    sendMsg(myChatId, "Время на отправку плана закончилось!");
                    sendMsg(adminId, "Сбор остановлен!");
                }else {sendMsg(adminId, "Сбор еще не начался!");}
            }
            else if(message.startsWith("/plans_status"))
            {
                if(botState==true)
                {
                    String msg =  planCheck();
                    sendMsg(adminId, "Я подготовил небольшую статистику:\n"+msg );
                    sendMsg(myChatId,msg );
                }else {sendMsg(adminId, "Сбор еще не начался!");}
            }

        }


    }

    public synchronized  void refreshUserList() throws SQLException {
        userlist = DbController.getAllUsersList();
    }
    /**
     * Метод для настройки сообщения и его отправки.
     * @param chatId id чата, куда отправится сообщение
     * @param string Строка, которую необходимот отправить в качестве сообщения.
     */
    public synchronized void sendMsg(String chatId, String string) {
        SendMessage s = new SendMessage();
        s.enableMarkdown(true);
        s.setChatId(chatId);
        s.setText(string);
        try {
            if(!chatId.equals(myChatId))
            {
                if(chatId.equals(adminId))setAdminButtons(s);
                else {setUserButtons(s);}
            }
            execute(s);

        } catch (TelegramApiException e)
        {
            e.printStackTrace();
        }
    }

    public synchronized void setUserButtons(SendMessage sendMessage)
    {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        // Создаем список строк клавиатуры
        List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();

        // Первая строчка клавиатуры
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        // Добавляем кнопки в первую строчку клавиатуры
        keyboardFirstRow.add(new KeyboardButton("/history"));
        keyboardSecondRow.add(new KeyboardButton("/help"));
        keyboardSecondRow.add(new KeyboardButton("/unreg"));

        // Добавляем все строчки клавиатуры в список
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        // и устанваливаем этот список нашей клавиатуре
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    public synchronized void setAdminButtons(SendMessage sendMessage)
    {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        // Создаем список строк клавиатуры
        List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();

        // Первая строчка клавиатуры
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();

        if(botState == false)
        {
            keyboardFirstRow.add(new KeyboardButton("/start_plan_collecting"));
            keyboardSecondRow.add(new KeyboardButton("/help"));
            keyboardSecondRow.add(new KeyboardButton("/history"));
        }else
        {
            keyboardFirstRow.add(new KeyboardButton("/stop_plan_collecting"));
            keyboardSecondRow.add(new KeyboardButton("/help"));
            keyboardSecondRow.add(new KeyboardButton("/plans_status"));

        }

        // Добавляем все строчки клавиатуры в список
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        // и устанваливаем этот список нашей клавиатуре
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    /**
     * Метод возвращает имя бота, указанное при регистрации.
     * @return имя бота
     */
    @Override
    public String getBotUsername() {
        return "littleplanobot";
    }

    /**
     * Метод возвращает token бота для связи с сервером Telegram
     * @return token для бота
     */
    @Override
    public String getBotToken() {
        return "1022086012:AAGvz2l5OXVT6-Zyh_Lp9T-8MAVo61EaJwY";
    }

    private synchronized  void startPlanCollecting()
    {
        if(botState==false)
        {
            botState = true;
        try {
            refreshUserList();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        }
    }

    private synchronized void stopPlanCollecting()
    {
        if(botState==true)
        {
            botState = false;
        }
    }

    private synchronized String planCheck()
    {
        String result = "";
        String checked = "";
        String uncheked = "";
        int che = 0;
        int unche = 0;
        for (User u: userlist)
        {
            if(u.getReady()==true)
            {
                che++;
                checked+=u.getUsername()+"\n";
            }else
            {
                unche++;
                uncheked+=u.getUsername()+"\n";
            }
        }
        result = "Отправили: "+che+" Не отправили: "+unche+"\n"+
                "Отправившие : "+checked+"\nНеотправившие: "+uncheked;
        result = result.replace("null","anon");
        result = result.replace('_','-');
        System.out.println(result);
        return result;
    }
}
