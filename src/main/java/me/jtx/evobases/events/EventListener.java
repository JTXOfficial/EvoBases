package me.jtx.evobases.events;

import me.jtx.evobases.EvoBases;
import me.jtx.evobases.commands.Command;
import me.jtx.evobases.commands.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
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
                    .setColor(Color.RED);
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
                    .setPlaceholder("Only supporting Town Halls 8, 9, 10, 12, 13, 14, 15, 16")
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
                        .setColor(Color.RED);
                event.replyEmbeds(noPermEB.build()).setEphemeral(true).queue();
                return;
            }

            event.deferEdit().queue();

            event.getJDA().getUserById(bot.getOrderDetail().getUserId()).openPrivateChannel().flatMap(privateChannel ->
                    privateChannel.sendMessage("Your order has been started!")).queue();

            event.getMessage().editMessageComponents(ActionRow.of(Button.success("startOrderModal", "Start Order").asDisabled())).queue();
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
                if (!isSupportedTownHall(townHallLevel)) {
                    event.reply("Unsupported Town Hall level. Please choose one of the following: 8, 9, 10, 12, 13, 14, 15, 16.").setEphemeral(true).queue();
                    return;
                }
            } catch (NumberFormatException e) {
                event.reply("Please enter a valid number for the Town Hall level.").setEphemeral(true).queue();
                return;
            }


            int orderNum =  bot.getOrderDetail().getHighestOrderNum() + 1;

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Order #" + orderNum)
                    .addField("Name", name, true)
                    .addField("Base Style", baseStyle, true)
                    .addField("Town Hall Level", townHallLevelString, true)
                    .addField("User", event.getUser().getAsMention(), true)
                    .addField("User ID", userId, true)
                    .addField("Completed", String.valueOf(bot.getOrderDetail().isCompleted()), true)
                    .setColor(Color.WHITE)
                    .setFooter(event.getMessage().getId());

            event.deferReply().setEphemeral(true).queue();


            event.getJDA().getTextChannelById(bot.getOrderChannelId()).sendMessageEmbeds(eb.build()).addActionRow(
                    Button.success("startOrderModal", "Start Order")).queue(message -> {

                bot.getOrderDetail().addOrder(userId, message.getId());
                bot.getOrderDetail().saveOrder();

                event.getJDA().getUserById(bot.getOrderDetail().getUserId()).openPrivateChannel().flatMap(privateChannel ->
                        privateChannel.sendMessage("Your order has been placed.")).queue();

            });


            bot.getCooldown().addCooldown(userId);
            bot.getCooldown().saveCooldown();
        }
    }

    private boolean isSupportedTownHall(int level) {
        return level == 8 || level == 9 || level == 10 || level == 12 || level == 13 || level == 14 || level == 15 || level == 16;
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
}
