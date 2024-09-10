package me.jtx.evobases;

import io.github.cdimascio.dotenv.Dotenv;
import me.jtx.evobases.commands.CommandManager;
import me.jtx.evobases.events.EventListener;
import me.jtx.evobases.utils.*;
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
    private GuildSettings guildSettings;
    private DailyOrderLimit dailyOrderLimit;
    private Dotenv dotenv = Dotenv.load();
    private Global global;
    private Msg embedDetails;

    private final String token = dotenv.get("TOKEN");
    private final String baseShowcaseChannelId = dotenv.get("BASE_SHOWCASE_CHANNEL_ID");
    private final String orderChannelId = dotenv.get("ORDER_CHANNEL_ID");
    private final String customActivityMessage = dotenv.get("CUSTOM_STATUS_MESSAGE");
    private final String menuTitle = dotenv.get("MENU_TITLE");
    private final String menuDescription = dotenv.get("MENU_DESCRIPTION");
    private final String menuStartOrderButtonMessage = dotenv.get("MENU_START_ORDER_BUTTON_MESSAGE");
    private final String specialRoleId = dotenv.get("SPECIAL_ROLE_ID");
    private final String moderationRoleId = dotenv.get("MODERATION_ROLE_ID");
    private final int dailyOrderMaxLimit = Integer.parseInt(dotenv.get("DAILY_ORDER_MAX_LIMIT"));
    private final String orderMenuMessageId= dotenv.get("ORDER_MENU_MESSAGE_ID");
    private final String orderMenuChannelId = dotenv.get("ORDER_MENU_CHANNEL_ID");
    private final String baseDesignerRoleId = dotenv.get("BASE_DESIGNER_ROLE_ID");
    private final String orderCompletedTitle= dotenv.get("ORDER_COMPLETED_TITLE");
    private final String orderCompletedMessage = dotenv.get("ORDER_COMPLETED_MESSAGE");
    private final String orderCompletedEmbedImage = dotenv.get("ORDER_COMPLETED_EMBED_IMAGE");
    private final String orderCompletedEmbedColorHex = dotenv.get("ORDER_COMPLETED_EMBED_COLOR_HEX");
    private final String cooldownMessage = dotenv.get("COOLDOWN_MESSAGE");
    private final String orderCreatedMessage = dotenv.get("ORDER_CREATED_MESSAGE");
    private final String orderDeletedMessage = dotenv.get("ORDER_DELETED_MESSAGE");
    private final String orderCreatedTitle = dotenv.get("ORDER_CREATED_TITLE");
    private final String orderCreatedColor = dotenv.get("ORDER_CREATED_COLOR");
    private final String orderDeletedTitle = dotenv.get("ORDER_DELETED_TITLE");
    private final String orderDeletedColor = dotenv.get("ORDER_DELETED_COLOR");
    private final String updateOrderMessageId = dotenv.get("UPDATE_ORDER_MESSAGE_ID");
    private final String orderStartedTitle = dotenv.get("ORDER_STARTED_TITLE");
    private final String orderStartedMessage = dotenv.get("ORDER_STARTED_MESSAGE");
    private final String orderStartedColor = dotenv.get("ORDER_STARTED_COLOR");
    private final String reviewChannelID = dotenv.get("REVIEW_CHANNEL_ID");



    public EvoBases() {
        instance = this;

        commandManager = new CommandManager(this);
        orderDetail = new OrderDetail();
        cooldown = new Cooldown();
        guildSettings = new GuildSettings();
        dailyOrderLimit = new DailyOrderLimit(this);
        global = new Global();
        embedDetails = new Msg();


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

    public GuildSettings getGuildSettings() {
        return guildSettings;
    }

    public DailyOrderLimit getDailyOrderLimit() {
        return dailyOrderLimit;
    }

    public Msg getEmbedDetails() {
        return embedDetails;
    }

    public Global getGlobal() {
        return global;
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

    public int getDailyOrderMaxLimit() {
        return dailyOrderMaxLimit;
    }

    public String getOrderMenuMessageId() {
        return orderMenuMessageId;
    }

    public String getOrderMenuChannelId() {
        return orderMenuChannelId;
    }

    public String getBaseDesignerRoleId() {
        return baseDesignerRoleId;
    }

    public String getOrderCompletedMessage() {
        return orderCompletedMessage;
    }

    public String getOrderCompletedEmbedImage() {
        return orderCompletedEmbedImage;
    }

    public String getOrderCompletedEmbedColorHex() {
        return orderCompletedEmbedColorHex;
    }

    public String getCooldownMessage() {
        return cooldownMessage;
    }

    public String getOrderDeletedMessage() {
        return orderDeletedMessage;
    }

    public String getOrderCreatedMessage() {
        return orderCreatedMessage;
    }

    public String getOrderDeletedTitle() {
        return orderDeletedTitle;
    }

    public String getOrderCreatedColor() {
        return orderCreatedColor;
    }

    public String getOrderCreatedTitle() {
        return orderCreatedTitle;
    }

    public String getOrderDeletedColor() {
        return orderDeletedColor;
    }

    public String getUpdateOrderMessageId() {
        return updateOrderMessageId;
    }

    public String getOrderStartedMessage() {
        return orderStartedMessage;
    }

    public String getOrderStartedTitle() {
        return orderStartedTitle;
    }

    public String getOrderStartedColor() {
        return orderStartedColor;
    }

    public String getOrderCompletedTitle() {
        return orderCompletedTitle;
    }

    public String getReviewChannelID() {
        return reviewChannelID;
    }

    public static void main(String[] args) {
        new EvoBases();
    }

}
