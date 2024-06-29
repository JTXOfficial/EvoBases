package me.jtx.evobases.events;

import com.google.gson.JsonObject;
import me.jtx.evobases.EvoBases;
import me.jtx.evobases.commands.Command;
import me.jtx.evobases.commands.CommandContext;
import me.jtx.evobases.commands.impl.QueueList;
import me.jtx.evobases.utils.Global;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.awt.*;
import java.util.List;

public class EventListener extends ListenerAdapter {

    private final EvoBases bot;
    private final int ORDERS_PER_PAGE = 15;

    public EventListener(final EvoBases bot) {
        this.bot = bot;
    }

    @Override
    public final void onSlashCommandInteraction(final SlashCommandInteractionEvent event) {
        for (Command command : bot.getCommandManager().getCommands()) {
            if (command.getName().equalsIgnoreCase(event.getName())) {
                handleSlashCommand(event, command);
                break;
            }
            if (command.getAliases() != null) {
                for (String alias : command.getAliases()) {
                    if (alias.equalsIgnoreCase(event.getName())) {
                        handleSlashCommand(event, command);
                    }
                }
            }
        }
    }

    private void handleSlashCommand(SlashCommandInteractionEvent event, Command command) {
        if (!event.getMember().hasPermission(command.getRequiredPermission())) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Error")
                    .setDescription("You do not have permission!")
                    .setColor(Color.RED)
                    .setFooter(Global.footer);
            ;
            event.replyEmbeds(eb.build()).setEphemeral(true).queue();
            return;
        }
        CommandContext ctx = CommandContext.fromSlash(event);
        command.execute(ctx);
    }

    @Override
    public final void onButtonInteraction(final ButtonInteractionEvent event) {
        if (event.getComponentId().equals("startOrderFormModal")) {
            TextInput nameInput = TextInput.create("name", "What do you want your base to say?", TextInputStyle.SHORT)
                    .setPlaceholder("Enter a message for your base (minimum 2 characters)")
                    .setRequired(true)
                    .build();

            TextInput baseStyle = TextInput.create("baseStyle", "Base Style", TextInputStyle.SHORT)
                    .setPlaceholder("Include the name of the base style you want")
                    .setRequired(true)
                    .build();

            TextInput townhallLevel = TextInput.create("townHallLevel", "TownHall Level", TextInputStyle.SHORT)
                    .setPlaceholder("Supports Town Halls " + bot.getTownhall().displaySupportedLevels())
                    .setRequired(true)
                    .build();

            Modal modal = Modal.create("orderForm", "Base Order Form")
                    .addComponents(ActionRow.of(nameInput), ActionRow.of(baseStyle), ActionRow.of(townhallLevel))
                    .build();

            event.replyModal(modal).queue();
        } else if (event.getComponentId().equals("startOrderModal")) {

            if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                EmbedBuilder noPermEB = new EmbedBuilder();
                noPermEB.setTitle("Error")
                        .setDescription("You do not have permission!")
                        .setColor(Color.RED)
                        .setFooter(Global.footer);
                ;
                event.replyEmbeds(noPermEB.build()).setEphemeral(true).queue();
                return;
            }
            event.deferEdit().queue();

            String messageId = event.getMessage().getId();
            String userId = bot.getOrderDetail().getUserIdByMessageId(messageId);

            event.getJDA().getUserById(userId).openPrivateChannel().flatMap(privateChannel ->
                    privateChannel.sendMessage("Your order has been started!")).queue();

            event.getMessage().editMessageComponents(ActionRow.of(Button.success("startOrderModal", "Start Order").asDisabled())).queue();
        } else if (event.getComponentId().equals("deleteOrderModal")) {
            boolean moderationRole = event.getGuild().getMember(event.getUser()).getRoles().stream()
                    .anyMatch(role -> role.getId().equals(bot.getModerationRoleId()));
            if (!moderationRole) {
                EmbedBuilder noPermEB = new EmbedBuilder();
                noPermEB.setTitle("Error")
                        .setDescription("You do not have permission!")
                        .setColor(Color.RED)
                        .setFooter(Global.footer);

                event.replyEmbeds(noPermEB.build()).setEphemeral(true).queue();
                return;
            }

            String messageId = event.getMessageId();

            bot.getOrderDetail().removeOrder(messageId);
            bot.getOrderDetail().saveOrder();

            event.reply("Order has been deleted!").setEphemeral(true).queue();

            event.getMessage().delete().queue();

        }

        CommandContext ctx = CommandContext.fromButton(event);
        QueueList queueList = (QueueList) bot.getCommandManager().getCommand("queue");
        if (queueList != null) {
            int currentPage = queueList.getPageState(event.getUser().getId());
            if (event.getComponentId().equals("next")) {
                queueList.handleFullQueue(ctx, bot.getOrderDetail().getIncompleteOrders(), currentPage + 1);
            } else if (event.getComponentId().equals("prev")) {
                queueList.handleFullQueue(ctx, bot.getOrderDetail().getIncompleteOrders(), currentPage - 1);
            }
        }

    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("orderForm")) {
            String userId = event.getUser().getId();
            boolean hasSpecialRole = event.getGuild().getMember(event.getUser()).getRoles().stream()
                    .anyMatch(role -> role.getId().equals(bot.getSpecialRoleId()));

            if (bot.getCooldown().isOnCooldown(userId) && !hasSpecialRole){
                long remainingTime = bot.getCooldown().getRemainingCooldownTime(userId);
                String formattedTime = bot.getCooldown().formatCooldownTime(remainingTime);

                event.reply("You are currently on cooldown. Please wait " + formattedTime+ " before placing another order.").setEphemeral(true).queue();

                return;
            }


            String name = event.getValue("name") != null ? event.getValue("name").getAsString() : null;
            String baseStyle = event.getValue("baseStyle") != null ? event.getValue("baseStyle").getAsString() : null;
            String townHallLevelString = event.getValue("townHallLevel") != null ? event.getValue("townHallLevel").getAsString() : null;

            if (name == null || baseStyle == null || townHallLevelString == null) {
                event.reply("Please fill out all required fields.").setEphemeral(true).queue();
                return;
            }

            if (name.length() < 2) {
                event.reply("The base message must be at least 2 characters long.").setEphemeral(true).queue();
                return;
            }

            int townHallLevel;
            try {
                townHallLevel = Integer.parseInt(townHallLevelString);
                if (!bot.getTownhall().isSupportedTownHall(townHallLevel)) {
                    event.reply("Unsupported Town Hall level. Please choose one of the following: " + bot.getTownhall().displaySupportedLevels() + ".").setEphemeral(true).queue();
                    return;
                }
            } catch (NumberFormatException e) {
                event.reply("Please enter a valid number for the Town Hall level.").setEphemeral(true).queue();
                return;
            }


            int orderNum =  bot.getOrderDetail().getHighestOrderNum() + 1;

            EmbedBuilder eb = new EmbedBuilder();

            if (!hasSpecialRole) {
                eb.setTitle("Order #" + orderNum)
                        .addField("Name", name, true)
                        .addField("Base Style", baseStyle, true)
                        .addField("Town Hall Level", townHallLevelString, true)
                        .addField("User", event.getUser().getAsMention(), true)
                        .addField("User ID", userId, true)
                        .addField("Completed", String.valueOf(bot.getOrderDetail().isCompleted()), true)
                        .setColor(Color.WHITE)
                        .setFooter(Global.footer + " | " + event.getMessage().getId());

                event.deferReply().setEphemeral(true).queue();
            } else {
                eb.setTitle("Order #" + orderNum)
                        .addField("Name", name, true)
                        .addField("Base Style", baseStyle, true)
                        .addField("Town Hall Level", townHallLevelString, true)
                        .addField("User", event.getUser().getAsMention(), true)
                        .addField("User ID", userId, true)
                        .addField("Completed", String.valueOf(bot.getOrderDetail().isCompleted()), true)
                        .setColor(Color.YELLOW)
                        .setFooter(Global.footer + " | " + event.getMessage().getId());

                event.deferReply().setEphemeral(true).queue();
            }


            event.getJDA().getTextChannelById(bot.getOrderChannelId()).sendMessageEmbeds(eb.build()).setActionRow(
                    Button.success("startOrderModal", "Start Order"),
                    Button.danger("deleteOrderModal", "Delete Order")
            ).queue(message -> {
                bot.getOrderDetail().addOrder(userId, message.getId(), hasSpecialRole);
                bot.getOrderDetail().saveOrder();

                event.getJDA().getUserById(bot.getOrderDetail().getUserId()).openPrivateChannel().flatMap(privateChannel ->
                        privateChannel.sendMessage("Your order has been placed.")
                ).queue();
            });

            /**
             * TODO: fix board
             */
            //createQueueEmbed(event);

            bot.getCooldown().addCooldown(userId);
            bot.getCooldown().saveCooldown();
        }
    }



    @Override
    public final void onReady(ReadyEvent event) {
        System.out.println("Ready event called.");
        CommandListUpdateAction commands = event.getJDA().updateCommands();
        for (Command cmd : bot.getCommandManager().getCommands()) {
            commands.addCommands(cmd.toCommand());
            System.out.println("Registered a new command: " + cmd.getName());
        }
        commands.queue();
    }

    private MessageEmbed createQueueEmbed(ModalInteractionEvent e) {
        List<JsonObject> incompleteOrders = bot.getOrderDetail().getIncompleteOrders();

        EmbedBuilder embed = new EmbedBuilder();
        StringBuilder stringBuilder = new StringBuilder();

        if (incompleteOrders.isEmpty()) {
            embed.setTitle("Current Queue")
                    .setColor(Color.WHITE)
                    .setDescription("The queue is currently empty.")
                    .setFooter(Global.footer);
            return embed.build();
        }

        for (JsonObject order : incompleteOrders) {
            String userId = order.get("userId").getAsString();
            int queueNum = order.get("queueNum").getAsInt();
            User user = e.getUser().getJDA().getUserById(userId);

            stringBuilder.append("**#").append(queueNum).append("** ").append("<@")
                    .append(userId).append("> [").append(user.getName()).append("]\n");
        }

        embed.setTitle("Current Queue")
                .setColor(Color.WHITE)
                .setDescription(stringBuilder.toString())
                .setFooter(Global.footer);

        return embed.build();
    }
}
