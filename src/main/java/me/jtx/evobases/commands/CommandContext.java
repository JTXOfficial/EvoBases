package me.jtx.evobases.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.Collections;

public class CommandContext {
    private final Guild guild;
    private final TextChannel textChannel;
    private final JDA jda;
    private final User user;
    private final Member member;
    private boolean isSlash;
    private SlashCommandInteractionEvent slashEvent;
    private ButtonInteractionEvent buttonEvent;
    private String[] args = new String[]{};

    public CommandContext(Guild guild, TextChannel textChannel, JDA jda, User user, Member member) {
        this.guild = guild;
        this.textChannel = textChannel;
        this.jda = jda;
        this.user = user;
        this.member = member;
        this.isSlash = false;
        this.slashEvent = null;
        this.buttonEvent = null;
    }

    public static CommandContext fromSlash(final SlashCommandInteractionEvent e) {
        CommandContext ctx = new CommandContext(e.getGuild(), e.getChannel().asTextChannel(), e.getJDA(), e.getUser(), e.getMember());
        ctx.slashEvent = e;
        ctx.isSlash = true;
        return ctx;
    }

    public static CommandContext fromButton(final ButtonInteractionEvent e) {
        CommandContext ctx = new CommandContext(e.getGuild(), e.getChannel().asTextChannel(), e.getJDA(), e.getUser(), e.getMember());
        ctx.buttonEvent = e;
        ctx.isSlash = false;
        return ctx;
    }

    public RestAction<?> reply(String a) {
        if (this.isSlash) {
            return this.slashEvent.reply(a).setAllowedMentions(Collections.emptySet());
        } else {
            return this.textChannel.sendMessage(a).setAllowedMentions(Collections.emptySet());
        }
    }

    public RestAction<?> reply(MessageCreateData a) {
        if (this.isSlash) {
            return this.slashEvent.reply(a).setAllowedMentions(Collections.emptySet());
        } else {
            return this.textChannel.sendMessage(a).setAllowedMentions(Collections.emptySet());
        }
    }

    public RestAction<?> reply(MessageEmbed a) {
        if (this.isSlash) {
            return this.slashEvent.replyEmbeds(a);
        } else {
            return this.textChannel.sendMessageEmbeds(a);
        }
    }

    public boolean hasInput(String input) {
        if (isSlash) {
            for (OptionMapping option : slashEvent.getOptions()) {
                if (option.getName().equalsIgnoreCase(input)) {
                    if (slashEvent.getOption(input) != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String textInput(String optionName) {
        if (isSlash) {
            for (OptionMapping option : slashEvent.getOptions()) {
                if (option.getName().equalsIgnoreCase(optionName)) {
                    if (slashEvent.getOption(optionName) == null) {
                        return null;
                    }
                    return slashEvent.getOption(optionName).getAsString();
                }
            }
        }
        return null;
    }

    public float intInput(String optionName) {
        if (isSlash) {
            for (OptionMapping option : slashEvent.getOptions()) {
                if (option.getName().equalsIgnoreCase(optionName)) {
                    if (slashEvent.getOption(optionName) == null) {
                        return -1;
                    }
                    return (int) slashEvent.getOption(optionName).getAsLong();
                }
            }
        }
        return -1;
    }

    public String fullArgs() {
        if (getArgs().length == 0) return null;
        StringBuilder builder = new StringBuilder(getArgs()[0]);
        for (int i = 1; i < args.length; i++) {
            builder.append(" ").append(getArgs()[i]);
        }
        return builder.toString();
    }

    public String argsOrOption(String optionName) {
        if (!isSlash) {
            return fullArgs();
        }
        for (OptionMapping option : slashEvent.getOptions()) {
            if (option.getName().equalsIgnoreCase(optionName)) {
                if (slashEvent.getOption(optionName) == null) {
                    return null;
                }
                return slashEvent.getOption(optionName).getAsString();
            }
        }
        return null;
    }

    public Guild getGuild() {
        return guild;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public Message getMessage() {
        return null;
    }

    public SlashCommandInteractionEvent getSlashEvent() {
        return slashEvent;
    }

    public void setSlashEvent(SlashCommandInteractionEvent slashEvent) {
        this.slashEvent = slashEvent;
    }

    public boolean isSlash() {
        return isSlash;
    }

    public void setSlash(boolean slash) {
        isSlash = slash;
    }

    public User getUser() {
        return user;
    }

    public JDA getJDA() {
        return jda;
    }

    public TextChannel getTextChannel() {
        return textChannel;
    }

    public Member getMember() {
        return member;
    }

    public ButtonInteractionEvent getButtonEvent() {
        return buttonEvent;
    }

    public void setButtonEvent(ButtonInteractionEvent buttonEvent) {
        this.buttonEvent = buttonEvent;
    }
}
