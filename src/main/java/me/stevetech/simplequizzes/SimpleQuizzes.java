package me.stevetech.simplequizzes;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class SimpleQuizzes extends JavaPlugin implements Listener {
    int task; // Repeating Task ID
    List<Map<String,String>> questions; // List of questions from the config
    int question; // Current Question Index
    String prefix; // Prefix of the plugin in chat
    Random rand = new Random(); // Used when selecting questions
    boolean answered; // Whether question has been answered

    @Override
    public void onEnable() {
        // Setup Config
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Start Quizzes & Get data from Config
        setup();

        getLogger().info(getDescription().getName() + ' ' + getDescription().getVersion() + " has been Enabled");
    }

    @Override
    public void onDisable() {
        // Save Configs
        saveConfig();

        getLogger().info(getDescription().getName() + ' ' + getDescription().getVersion() + " has been Disabled");
    }

    @SuppressWarnings("unchecked")
    //Suppress Warnings for questions which returns List<capture<?>> when it should return List<Map<String, String>>
    private void setup() {
        // Register Events
        if (getConfig().getBoolean("check-chat")) {
            getServer().getPluginManager().registerEvents(this, this);
        } else {
            HandlerList.unregisterAll((Listener) this);
        }

        // Set prefix
        prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.prefix"));

        // Set Questions
        questions = (List<Map<String, String>>) getConfig().getList("questions");

        // Setup the Repeating Message
        task = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            // Say the answer if no-one answered
            if (!answered) {
                TextComponent answerMessage = new TextComponent(prefix +
                        replacePlaceholders(getConfig().getString("messages.answer")));
                answerMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/quiz "));

                getServer().spigot().broadcast(answerMessage);
            }

            answered = false; // Question hasn't been answered yet
            question = rand.nextInt(questions.size()); // Get random question index

            // Using TextComponents so that a Click Event can be set
            TextComponent questionMessage = new TextComponent(prefix +
                    replacePlaceholders(getConfig().getString("messages.new-question")));
            questionMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/quiz "));

            TextComponent tipMessage = new TextComponent(prefix +
                    replacePlaceholders(getConfig().getString("messages.tip")));
            tipMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/quiz "));

            // Send message to all players
            getServer().spigot().broadcast(questionMessage);

            if (!getConfig().getString("messages.tip").isEmpty())
                getServer().spigot().broadcast(tipMessage);

        }, 0, getConfig().getInt("delay") * 20); // Convert seconds to ticks and repeat every amount of ticks
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // When a player types /quiz or /answer and has permission
        if (cmd.getName().equalsIgnoreCase("quiz") && sender.hasPermission("simplequizzes.answer")) {
            if (args.length > 0) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (!answered) {
                        if (String.join(" ", args).equalsIgnoreCase(questions.get(question).get("answer"))) {
                            getServer().broadcastMessage(prefix + replacePlaceholders(getConfig().getString("messages.correct-broadcast"), player));
                            for (String command: getConfig().getStringList("rewards"))
                                getServer().dispatchCommand(getServer().getConsoleSender(), replacePlaceholders(command, player));
                            answered = true;
                        } else {
                            sender.sendMessage(prefix + replacePlaceholders(getConfig().getString("messages.incorrect"), player));
                        }
                    } else {
                        sender.sendMessage(prefix + replacePlaceholders(getConfig().getString("messages.late"), player));
                    }
                } else getLogger().warning("You need to be a player to run this command.");
            } else {
                TextComponent questionMessage = new TextComponent(prefix +
                        replacePlaceholders(getConfig().getString("messages.new-question")));
                questionMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/quiz "));

                sender.spigot().sendMessage(questionMessage);
            }
        }
        // When a player types /SimpleQuizzes and has permission
        if (cmd.getName().equalsIgnoreCase("SimpleQuizzes") &&
                (sender.hasPermission("simplequizzes.reload") || sender.hasPermission("simplequizzes.start") || sender.hasPermission("simplequizzes.stop"))) {
            if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("simplequizzes.reload")) {
                getServer().getScheduler().cancelTask(task); // Don't want 2 tasks
                reloadConfig();
                setup(); // Setup with new configuration
                getLogger().info("Reloaded Config");
                sender.sendMessage(prefix + ChatColor.YELLOW + "Reloaded Config");
            } else if (args[0].equalsIgnoreCase("start") && sender.hasPermission("simplequizzes.start")) {
                setup();
            } else if (args[0].equalsIgnoreCase("stop") && sender.hasPermission("simplequizzes.start")) {
                getServer().getScheduler().cancelTask(task);
            } else return false; // Missing reload show the default usage message in plugin.yml
        }
        return true;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (event.getMessage().equalsIgnoreCase(questions.get(question).get("answer")) && player.hasPermission("simplequizzes.answer")) {
            getServer().getScheduler().runTaskLater(this, () -> {
                // Send the message
                if (!answered) {
                    getServer().broadcastMessage(prefix + replacePlaceholders(getConfig().getString("messages.correct-broadcast"), player));
                    for (String command: getConfig().getStringList("rewards"))
                        getServer().dispatchCommand(getServer().getConsoleSender(), replacePlaceholders(command, player));
                    answered = true;
                } else {
                    player.sendMessage(prefix + replacePlaceholders(getConfig().getString("messages.late"), player));
                }
            }, 1);
        }
    }

    private String replacePlaceholders(String input) {
        return ChatColor.translateAlternateColorCodes('&',
                input.replace("%question%", questions.get(question).get("question"))
                        .replace("%answer%", questions.get(question).get("answer")));
    }

    private String replacePlaceholders(String input, Player player) {
        return replacePlaceholders(input).replace("%player%", player.getDisplayName());
    }
}
