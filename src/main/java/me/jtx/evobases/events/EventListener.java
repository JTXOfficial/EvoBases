package me.jtx.evobases.events;

import me.jtx.evobases.EvoBases;
import me.jtx.evobases.commands.Command;
import me.jtx.evobases.commands.CommandContext;
import me.jtx.evobases.commands.impl.QueueList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EventListener extends ListenerAdapter {

    private final EvoBases bot;

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
                    .setFooter(bot.getEmbedDetails().footer);
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

            TextInput townhallLevel = TextInput.create("townHallLevel", "TownHall Level", TextInputStyle.SHORT)
                    .setPlaceholder("Supports Town Halls " + bot.getGuildSettings().displaySupportedLevels())
                    .setRequired(true)
                    .build();


            TextInput baseStyleInput = TextInput.create("baseStyle", "Base Style", TextInputStyle.SHORT)
                    .setPlaceholder("Enter the base style (e.g., Sausage, Evoxq)")
                    .setRequired(true)
                    .build();

            TextInput additionalNotes = TextInput.create("additionalNotes", "Additional Notes", TextInputStyle.PARAGRAPH)
                    .setRequired(false)
                    .build();



            Modal modal = Modal.create("orderForm", "Base Order Form")
                    .addComponents(ActionRow.of(nameInput), ActionRow.of(townhallLevel), ActionRow.of(baseStyleInput), ActionRow.of(additionalNotes))
                    .build();

            event.replyModal(modal).queue();
        } else if (event.getComponentId().equals("startOrderModal")) {
            if (!event.getGuild().getMember(event.getUser()).getRoles().stream()
                    .anyMatch(role -> role.getId().equals(bot.getBaseDesignerRoleId()))) {
                EmbedBuilder noPermEB = new EmbedBuilder();
                noPermEB.setTitle("Error")
                        .setDescription("You do not have permission!")
                        .setColor(Color.RED)
                        .setFooter(bot.getEmbedDetails().footer);
                ;
                event.replyEmbeds(noPermEB.build()).setEphemeral(true).queue();
                return;
            }

            event.deferEdit().queue();

            String messageId = event.getMessage().getId();
            String userId = bot.getOrderDetail().getUserIdByMessageId(messageId);

            if (event.getJDA().getUserById(bot.getOrderDetail().getUserId()) != null) {
                event.getJDA().getUserById(userId).openPrivateChannel().flatMap(privateChannel ->
                        privateChannel.sendMessage("Your order has been started!")).queue();
            }

            event.getChannel().retrieveMessageById(messageId).queue(message -> {
                MessageEmbed embed = message.getEmbeds().get(0);
                EmbedBuilder updatedEmbed = new EmbedBuilder(embed)
                        .addField("Claimed By", event.getMember().getAsMention(), false);

                message.editMessageEmbeds(updatedEmbed.build()).queue();
            });

            event.getMessage().editMessageComponents(ActionRow.of(Button.success("startOrderModal", "Start Order").asDisabled())).queue();
        } else if (event.getComponentId().equals("deleteOrderModal")) {
            TextInput deletedReason = TextInput.create("reason", "What's the reason for removing this order?", TextInputStyle.SHORT)
                    .setPlaceholder("Invalid base.")
                    .setRequired(true)
                    .build();

            Modal modal = Modal.create("deleteForm", "Removing Order Form")
                    .addComponents(ActionRow.of(deletedReason))
                    .build();

            event.replyModal(modal).queue();
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

                event.reply(bot.getCooldownMessage().replace("%time-left%", formattedTime)).setEphemeral(true).queue();

                return;
            }


            String name = event.getValue("name") != null ? event.getValue("name").getAsString() : null;
            String baseStyle = event.getValue("baseStyle") != null ? event.getValue("baseStyle").getAsString() : null;
            String townHallLevelString = event.getValue("townHallLevel") != null ? event.getValue("townHallLevel").getAsString() : null;
            String additionalNotes = event.getValue("additionalNotes") != null ? event.getValue("additionalNotes").getAsString() : null;

            Set<String> validBaseStyles = bot.getGuildSettings().getBaseTypes();


            if (name == null || baseStyle == null || townHallLevelString == null) {
                event.reply("Please fill out all required fields.").setEphemeral(true).queue();
                return;
            }

            if (name.length() < 2) {
                event.reply("The base message must be at least 2 characters long.").setEphemeral(true).queue();
                return;
            }

            if (!validBaseStyles.contains(baseStyle.toLowerCase())) {

                event.reply("Invalid base style. Please enter one of the following: " + bot.getGuildSettings().displayBaseTypes() + ".").setEphemeral(true).queue();
                return;
            }

            int townHallLevel;
            try {
                townHallLevel = Integer.parseInt(townHallLevelString);
                if (!bot.getGuildSettings().isSupportedTownHall(townHallLevel)) {
                    event.reply("Unsupported Town Hall level. Please choose one of the following: " + bot.getGuildSettings().displaySupportedLevels() + ".").setEphemeral(true).queue();
                    return;
                }
            } catch (NumberFormatException e) {
                event.reply("Please enter a valid number for the Town Hall level.").setEphemeral(true).queue();
                return;
            }


            if (bot.getDailyOrderLimit().isResetTimePassed()) {
                bot.getDailyOrderLimit().resetDailyLimit(bot.getGlobal().todayDate());
                bot.getDailyOrderLimit().setResetTime(LocalDateTime.now().plusDays(1));
            }

            if (!hasSpecialRole && bot.getDailyOrderLimit().getCurrentOrderCount(bot.getGlobal().todayDate()) >= bot.getDailyOrderMaxLimit()) {
                EmbedBuilder limitReachedEB = new EmbedBuilder();
                limitReachedEB.setTitle("Error")
                        .setDescription("Sorry, the daily limit has been reached " + bot.getDailyOrderLimit().getCurrentOrderCount(bot.getGlobal().todayDate())
                                + "/" + (bot.getDailyOrderMaxLimit()) + ". The daily limit resets in " + bot.getDailyOrderLimit().getResetTime() + ".")
                        .setColor(Color.RED)
                        .setFooter(bot.getEmbedDetails().footer);
                event.replyEmbeds(limitReachedEB.build()).setEphemeral(true).queue();
                return;
            }

            int orderNum =  bot.getOrderDetail().getHighestOrderNum() + 1;

            EmbedBuilder eb = new EmbedBuilder();

            if (!hasSpecialRole) {
                eb.setTitle("Order #" + orderNum)
                        .addField("Name", name, true)
                        .addField("Base Style", baseStyle, true)
                        .addField("Town Hall Level", townHallLevelString, true)
                        .addField("Additional Notes", additionalNotes, true)
                        .addField("User", event.getUser().getAsMention(), true)
                        .addField("User ID", userId, true)
                        .addField("Completed", String.valueOf(bot.getOrderDetail().isCompleted()), true)
                        .setColor(Color.WHITE)
                        .setFooter(bot.getEmbedDetails().footer + " | " + event.getMessage().getId());

                event.deferReply().setEphemeral(true).queue();
            } else {
                eb.setTitle("Order #" + orderNum)
                        .addField("Name", name, true)
                        .addField("Base Style", baseStyle, true)
                        .addField("Town Hall Level", townHallLevelString, true)
                        .addField("Additional Notes", additionalNotes, false)
                        .addField("User", event.getUser().getAsMention(), true)
                        .addField("User ID", userId, true)
                        .addField("Completed", String.valueOf(bot.getOrderDetail().isCompleted()), true)
                        .setColor(Color.YELLOW)
                        .setFooter(bot.getEmbedDetails().footer + " | " + event.getMessage().getId());

                event.deferReply().setEphemeral(true).queue();
            }


            event.getJDA().getTextChannelById(bot.getOrderChannelId()).sendMessageEmbeds(eb.build()).setActionRow(
                    Button.success("startOrderModal", "Start Order"),
                    Button.danger("deleteOrderModal", "Delete Order")
            ).queue(message -> {
                bot.getOrderDetail().addOrder(userId, message.getId(), hasSpecialRole);

                if (!hasSpecialRole) {
                    bot.getDailyOrderLimit().incrementOrderCount(bot.getGlobal().todayDate());
                }

                bot.getOrderDetail().saveOrder();



                if (event.getJDA().getUserById(bot.getOrderDetail().getUserId()) != null) {
                    event.getJDA().getUserById(bot.getOrderDetail().getUserId()).openPrivateChannel().flatMap(privateChannel ->
                            privateChannel.sendMessage("Your order has been placed.")
                    ).queue();
                }

                updateOrderMenuEmbed(event.getChannel().asTextChannel(), bot.getOrderMenuMessageId());
            });

            /**
             * TODO: fix board
             */
            //createQueueEmbed(event);

            bot.getCooldown().addCooldown(userId);
            bot.getCooldown().saveCooldown();
        }

        if (event.getModalId().equals("deleteForm")) {
            String reason = event.getValue("reason") != null ? event.getValue("reason").getAsString() : null;
            String messageId = event.getMessage().getId();

            boolean moderationRole = event.getGuild().getMember(event.getUser()).getRoles().stream()
                    .anyMatch(role -> role.getId().equals(bot.getModerationRoleId()));
            boolean baseDesignRole = event.getGuild().getMember(event.getUser()).getRoles().stream()
                    .anyMatch(role -> role.getId().equals(bot.getBaseDesignerRoleId()));

            if (!moderationRole && !baseDesignRole) {
                EmbedBuilder noPermEB = new EmbedBuilder();
                noPermEB.setTitle("Error")
                        .setDescription("You do not have permission!")
                        .setColor(Color.RED)
                        .setFooter(bot.getEmbedDetails().footer);

                event.replyEmbeds(noPermEB.build()).setEphemeral(true).queue();
                return;
            }


            // Retrieve the message to get the user ID
            event.getChannel().retrieveMessageById(messageId).queue(message -> {
                List<MessageEmbed> embeds = message.getEmbeds();
                if (embeds.isEmpty()) {
                    event.reply("Order not found.").setEphemeral(true).queue();
                    return;
                }

                MessageEmbed embed = embeds.get(0);
                String userId = null;
                for (MessageEmbed.Field field : embed.getFields()) {
                    if (field.getName().equalsIgnoreCase("User ID")) {
                        userId = field.getValue();
                        break;
                    }
                }

                if (userId == null) {
                    event.reply("User ID not found in the order embed.").setEphemeral(true).queue();
                    return;
                }

                User user = event.getJDA().getUserById(userId);

                if (user != null) {
                    user.openPrivateChannel().flatMap(privateChannel ->
                            privateChannel.sendMessage("Your order has been deleted for the following reason: " + reason)
                    ).queue();

                    boolean hasSpecialRole = event.getGuild().getMemberById(userId).getRoles().stream()
                            .anyMatch(role -> role.getId().equals(bot.getSpecialRoleId()));

                    if (!hasSpecialRole) {
                        bot.getDailyOrderLimit().decrementOrderCount(bot.getGlobal().todayDate());
                    }
                }


                bot.getOrderDetail().removeOrder(messageId);
                bot.getOrderDetail().saveOrder();
                message.delete().queue();
                event.reply("Order has been deleted!").setEphemeral(true).queue();
                bot.getCooldown().removeCooldown(userId);

                updateOrderMenuEmbed(event.getJDA().getTextChannelById(bot.getOrderMenuChannelId()), bot.getOrderMenuMessageId());
            });
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

        if (bot.getDailyOrderLimit().getResetTime() == null) {
            System.out.println("I was reset good or bad???");
            bot.getDailyOrderLimit().setResetTime(LocalDateTime.now().plusDays(1));
        }
    }

    private void updateOrderMenuEmbed(TextChannel textChannel, String messageId) {
        String todayDate = bot.getGlobal().todayDate();
        int currentCount = bot.getDailyOrderLimit().getCurrentOrderCount(todayDate);
        int maxLimit = bot.getDailyOrderMaxLimit();

        textChannel.retrieveMessageById(messageId).queue(message -> {
            MessageEmbed embed = message.getEmbeds().get(0);
            EmbedBuilder updatedEmbed = new EmbedBuilder(embed)
                    .clearFields()
                    .addField("Current Count", currentCount + "/" + maxLimit, false);

            message.editMessageEmbeds(updatedEmbed.build()).queue();
        });
    }

}
