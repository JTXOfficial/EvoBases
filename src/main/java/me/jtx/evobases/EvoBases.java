package me.jtx.evobases;

import io.github.cdimascio.dotenv.Dotenv;
import me.jtx.evobases.commands.CommandManager;
import me.jtx.evobases.commands.impl.QueueList;
import me.jtx.evobases.events.EventListener;
import me.jtx.evobases.utils.Cooldown;
import me.jtx.evobases.utils.OrderDetail;
import me.jtx.evobases.utils.Townhall;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class EvoBases {
    public static EvoBases instance = null;

    private CommandManager commandManager;
    private OrderDetail orderDetail;
    private Cooldown cooldown;
    private Townhall townhall;
    private Dotenv dotenv = Dotenv.load();

    private final String token = dotenv.get("TOKEN");
    private final String baseShowcaseChannelId = dotenv.get("BASE_SHOWCASE_CHANNEL_ID");
    private final String orderChannelId = dotenv.get("ORDER_CHANNEL_ID");
    private final String customActivityMessage = dotenv.get("CUSTOM_STATUS_MESSAGE");
    private final String menuTitle = dotenv.get("MENU_TITLE");
    private final String menuDescription = dotenv.get("MENU_DESCRIPTION");
    private final String menuStartOrderButtonMessage = dotenv.get("MENU_START_ORDER_BUTTON_MESSAGE");
    private final String specialRoleId = dotenv.get("SPECIAL_ROLE_ID");
    private final String moderationRoleId = dotenv.get("MODERATION_ROLE_ID");

    private String queueMessageId;
    private String queueChannelId = "1256009683085557790";

    public String getQueueMessageId() {
        return queueMessageId;
    }

    public void setQueueMessageId(String queueMessageId) {
        this.queueMessageId = queueMessageId;
    }

    public String getQueueChannelId() {
        return queueChannelId;
    }

    public EvoBases() {
        instance = this;

        commandManager = new CommandManager(this);
        orderDetail = new OrderDetail();
        cooldown = new Cooldown();
        townhall = new Townhall();


        commandManager.initialize();


        JDABuilder.createLight(getToken(), GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_PRESENCES).setStatus(OnlineStatus.ONLINE)
                .enableCache(CacheFlag.CLIENT_STATUS, CacheFlag.ACTIVITY).
                setMemberCachePolicy(MemberCachePolicy.ALL).enableCache(CacheFlag.MEMBER_OVERRIDES)
                .addEventListeners(new EventListener(this))
                .setActivity(Activity.customStatus(customActivityMessage)).build();

    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public Cooldown getCooldown() {
        return cooldown;
    }

    public OrderDetail getOrderDetail() {
        return orderDetail;
    }

    public Townhall getTownhall() {
        return townhall;
    }

    public String getToken() {
        return token;
    }

    public String getBaseShowcaseChannelId() {
        return baseShowcaseChannelId;
    }

    public String getOrderChannelId() {
        return orderChannelId;
    }

    public String getMenuStartOrderButtonMessage() {
        return menuStartOrderButtonMessage;
    }

    public String getMenuDescription() {
        return menuDescription;
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public String getSpecialRoleId() {
        return specialRoleId;
    }

    public String getModerationRoleId() {
        return moderationRoleId;
    }

    public static void main(String[] args) {
        new EvoBases();
    }

    public QueueList getQueueListCommand() {
        return (QueueList) this.getCommandManager().getCommand("queue");
    }
}
